package com.dfire.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.dfire.common.constants.Constants;
import com.dfire.common.entity.*;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.entity.vo.*;
import com.dfire.common.enums.JobScheduleTypeEnum;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.service.*;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.common.util.NamedThreadFactory;
import com.dfire.common.util.StringUtil;
import com.dfire.common.vo.RestfulResponse;
import com.dfire.config.UnCheckLogin;
import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.netty.worker.WorkClient;
import com.dfire.protocol.JobExecuteKind;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.WebAsyncTask;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:50 2018/1/13
 * @desc 调度中心视图管理器
 */
@Controller
@RequestMapping("/scheduleCenter")
public class ScheduleCenterController extends BaseHeraController {

    @Autowired
    private HeraJobService heraJobService;
    @Autowired
    private HeraJobActionService heraJobActionService;
    @Autowired
    private HeraGroupService heraGroupService;
    @Autowired
    private HeraJobHistoryService heraJobHistoryService;
    @Autowired
    private HeraJobMonitorService heraJobMonitorService;
    @Autowired
    private HeraUserService heraUserService;
    @Autowired
    private HeraPermissionService heraPermissionService;
    @Autowired
    private WorkClient workClient;
    @Autowired
    private HeraHostGroupService heraHostGroupService;

    private ThreadPoolExecutor poolExecutor;

    {
        poolExecutor = new ThreadPoolExecutor(
                1, Runtime.getRuntime().availableProcessors() * 4, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new NamedThreadFactory("updateJobThread"), new ThreadPoolExecutor.AbortPolicy());
        poolExecutor.allowCoreThreadTimeOut(true);
    }

    private final String JOB = "job";
    private final String GROUP = "group";
    private final String ERROR_MSG = "抱歉，您没有权限进行此操作";


    @RequestMapping()
    public String login() {
        return "scheduleCenter/scheduleCenter.index";
    }

    @RequestMapping(value = "/init", method = RequestMethod.POST)
    @ResponseBody
    public Map<String,List<HeraJobTreeNodeVo>> initJobTree() {
        return heraJobService.buildJobTree(getOwner());
    }

    @RequestMapping(value = "/getJobMessage", method = RequestMethod.GET)
    @ResponseBody
    public HeraJobVo getJobMessage(Integer jobId) {
        HeraJob job = heraJobService.findById(jobId);
        HeraJobVo heraJobVo = BeanConvertUtils.convert(job);
        heraJobVo.setInheritConfig(getInheritConfig(job.getGroupId()));
        HeraJobMonitor monitor = heraJobMonitorService.findByJobId(jobId);
        StringBuilder focusUsers = new StringBuilder("[ ");
        if (monitor != null && StringUtils.isNotBlank(monitor.getUserIds())) {
            String ownerId = getOwnerId();
            String[] ids = monitor.getUserIds().split(",");
            Arrays.stream(ids).forEach(id -> {
                if (ownerId.equals(id)) {
                    heraJobVo.setFocus(true);
                }
                HeraUser heraUser = heraUserService.findById(HeraUser.builder().id(Integer.valueOf(id)).build());
                focusUsers.append(heraUser.getName());
            });
        }

        HeraHostGroup hostGroup = heraHostGroupService.findById(job.getHostGroupId());
        focusUsers.append("]");
        if (hostGroup != null) {
            heraJobVo.setHostGroupName(hostGroup.getName());
        }
        heraJobVo.setUIdS(getuIds(jobId));
        heraJobVo.setFocusUser(focusUsers.toString());
        return heraJobVo;
    }

    @RequestMapping(value = "/getGroupMessage", method = RequestMethod.GET)
    @ResponseBody
    public HeraGroupVo getGroupMessage(String groupId) {
        Integer id = getGroupId(groupId);
        HeraGroup group = heraGroupService.findById(id);
        HeraGroupVo groupVo = BeanConvertUtils.convert(group);
        groupVo.setInheritConfig(getInheritConfig(groupVo.getParent()));
        groupVo.setUIdS(getuIds(id));
        return groupVo;
    }


    @RequestMapping(value = "/updatePermission", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public JsonResponse updatePermission(@RequestParam("id") Integer id,
                                         @RequestParam("type") boolean type,
                                         @RequestParam("uIdS") String names) {

        if (!hasPermission(id, type ? GROUP : JOB)) {
            return new JsonResponse(false, ERROR_MSG);
        }
        JSONArray uIdS = JSONArray.parseArray(names);
        Integer integer = heraPermissionService.deleteByTargetId(id);

        if (integer == null) {
            return new JsonResponse(false, "修改失败");
        }
        if (uIdS != null && uIdS.size() > 0) {
            String typeStr = type ? "group" : "job";
            Date date = new Date();
            Long targetId = Long.parseLong(String.valueOf(id));
            List<HeraPermission> permissions = new ArrayList<>(uIdS.size());
            for (Object uId : uIdS) {
                HeraPermission heraPermission = new HeraPermission();
                heraPermission.setType(typeStr);
                heraPermission.setGmtModified(date);
                heraPermission.setGmtCreate(date);
                heraPermission.setTargetId(targetId);
                heraPermission.setUid((String) uId);
                permissions.add(heraPermission);
            }

            Integer res = heraPermissionService.insertList(permissions);
            if (res == null || res != uIdS.size()) {
                return new JsonResponse(false, "修改失败");
            }
        }

        return new JsonResponse(true, "修改成功");
    }


    @RequestMapping(value = "/getJobOperator", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse getJobOperator(String jobId, boolean type) {

        Integer groupId = getGroupId(jobId);
        if (!hasPermission(groupId, type ? GROUP : JOB)) {
            return new JsonResponse(false, ERROR_MSG);
        }

        List<HeraPermission> permissions = heraPermissionService.findByTargetId(groupId);
        List<HeraUser> all = heraUserService.findAllName();

        if (all == null || permissions == null) {
            return new JsonResponse(false, "发生错误，请联系管理员");
        }
        Map<String, Object> res = new HashMap<>(2);

        res.put("allUser", all);
        res.put("admin", permissions);
        return new JsonResponse(true, "查询成功", res);
    }

    /**
     * 手动执行任务
     *
     * @param actionId
     * @return
     */
    @RequestMapping(value = "/manual", method = RequestMethod.GET)
    @ResponseBody
    public WebAsyncTask<RestfulResponse> execute(String actionId, Integer triggerType, @RequestParam(required = false) String owner) {

        if (owner == null && !hasPermission(Integer.parseInt(actionId.substring(actionId.length() - 4)), JOB)) {
            return new WebAsyncTask<>(() -> new RestfulResponse(false, ERROR_MSG));
        }

        TriggerTypeEnum triggerTypeEnum;
        if (triggerType == 2) {
            triggerTypeEnum = TriggerTypeEnum.MANUAL_RECOVER;
        } else {
            triggerTypeEnum = TriggerTypeEnum.MANUAL;
        }

        HeraAction heraAction = heraJobActionService.findById(actionId);
        HeraJob heraJob = heraJobService.findById(Integer.parseInt(heraAction.getJobId()));

        if (owner == null) {
            owner = super.getOwner();
        }
        if (owner == null) {
            throw new IllegalArgumentException("任务执行人为空");
        }
        String configs = heraJob.getConfigs();
        HeraJobHistory actionHistory = HeraJobHistory.builder().build();
        actionHistory.setJobId(heraAction.getJobId());
        actionHistory.setActionId(heraAction.getId().toString());
        actionHistory.setTriggerType(triggerTypeEnum.getId());
        actionHistory.setOperator(owner.equals(HeraGlobalEnvironment.getAdmin()) ? heraJob.getOwner() : owner);
        actionHistory.setIllustrate(owner);
        actionHistory.setStatus(StatusEnum.RUNNING.toString());
        actionHistory.setStatisticEndTime(heraAction.getStatisticEndTime());
        actionHistory.setHostGroupId(heraAction.getHostGroupId());
        actionHistory.setProperties(configs);
        heraJobHistoryService.insert(actionHistory);
        heraAction.setScript(heraJob.getScript());
        heraAction.setHistoryId(actionHistory.getId());
        heraJobActionService.update(heraAction);

        WebAsyncTask<RestfulResponse> webAsyncTask = new WebAsyncTask<>(HeraGlobalEnvironment.getRequestTimeout(), () -> {
            try {
                workClient.executeJobFromWeb(JobExecuteKind.ExecuteKind.ManualKind, actionHistory.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new RestfulResponse(true, actionId);
        });
        webAsyncTask.onTimeout(() -> new RestfulResponse(false, "执行任务操作请求中，请稍后"));
        return webAsyncTask;
    }

    @RequestMapping(value = "/getJobVersion", method = RequestMethod.GET)
    @ResponseBody
    public List<HeraActionVo> getJobVersion(String jobId) {
        List<HeraActionVo> list = new ArrayList<>();
        List<String> idList = heraJobActionService.getActionVersionByJobId(Long.parseLong(jobId));
        for(String id : idList){
            list.add(HeraActionVo.builder().id(id).build());
        }
        return list;

    }

    @RequestMapping(value = "/updateJobMessage", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse updateJobMessage(HeraJobVo heraJobVo) {
        if (!hasPermission(heraJobVo.getId(), JOB)) {
            return new RestfulResponse(false, ERROR_MSG);
        }
        HeraJob heraJob = BeanConvertUtils.convertToHeraJob(heraJobVo);
        RestfulResponse response = heraJobService.checkAndUpdate(heraJob);
        updateJobToMaster(response.isSuccess(), heraJob.getId());
        return response;
    }

    @RequestMapping(value = "/updateGroupMessage", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse updateGroupMessage(HeraGroupVo groupVo, String groupId) {
        groupVo.setId(getGroupId(groupId));
        if (!hasPermission(groupVo.getId(), GROUP)) {
            return new RestfulResponse(false, ERROR_MSG);
        }
        HeraGroup heraGroup = BeanConvertUtils.convert(groupVo);
        boolean res = heraGroupService.update(heraGroup) > 0;
        return new RestfulResponse(res, res ? "更新成功" : "系统异常,请联系管理员");
    }

    @RequestMapping(value = "/deleteJob", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse deleteJob(String id, Boolean isGroup) {
        Integer xId = getGroupId(id);
        if (!hasPermission(xId, isGroup ? GROUP : JOB)) {
            return new RestfulResponse(false, ERROR_MSG);
        }
        boolean res;
        String check = checkDependencies(xId, isGroup);
        if (StringUtils.isNotBlank(check)) {
            return new RestfulResponse(false, check);
        }

        if (isGroup) {
            res = heraGroupService.delete(xId) > 0;
            return new RestfulResponse(res, res ? "删除成功" : "系统异常,请联系管理员");

        }
        res = heraJobService.delete(xId) > 0;
        updateJobToMaster(res, xId);
        return new RestfulResponse(res, res ? "删除成功" : "系统异常,请联系管理员");
    }

    @RequestMapping(value = "/addJob", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse addJob(HeraJob heraJob, String parentId) {
        heraJob.setGroupId(getGroupId(parentId));
        if (!hasPermission(heraJob.getGroupId(), GROUP)) {
            return new RestfulResponse(false, ERROR_MSG);
        }
        heraJob.setHostGroupId(HeraGlobalEnvironment.defaultWorkerGroup);
        heraJob.setOwner(getOwner());
        heraJob.setScheduleType(JobScheduleTypeEnum.Independent.getType());
        return new RestfulResponse(heraJobService.insert(heraJob) > 0, String.valueOf(heraJob.getId()));
    }

    @RequestMapping(value = "/addMonitor", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse updateMonitor(Integer id) {
        boolean res = heraJobMonitorService.addMonitor(getOwnerId(), id);
        return new RestfulResponse(res, res ? "关注成功" : "系统异常，请联系管理员");
    }

    @RequestMapping(value = "/delMonitor", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse deleteMonitor(Integer id) {
        boolean res = heraJobMonitorService.removeMonitor(getOwnerId(), id);
        return new RestfulResponse(res, res ? "取关成功" : "系统异常，请联系管理员");
    }

    @RequestMapping(value = "/addGroup", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse addJob(HeraGroup heraGroup, String parentId) {
        heraGroup.setParent(getGroupId(parentId));
        if (!hasPermission(heraGroup.getParent(), GROUP)) {
            return new RestfulResponse(false, ERROR_MSG);
        }

        Date date = new Date();
        heraGroup.setGmtModified(date);
        heraGroup.setGmtCreate(date);
        heraGroup.setOwner(getOwner());
        heraGroup.setExisted(1);
        return new RestfulResponse(heraGroupService.insert(heraGroup) > 0, String.valueOf(heraGroup.getId() == null ? -1 : heraGroup.getId()));
    }

    @RequestMapping(value = "/updateSwitch", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse updateSwitch(Integer id) {
        if (!hasPermission(id, JOB)) {
            return new RestfulResponse(false, ERROR_MSG);
        }
        boolean result = heraJobService.changeSwitch(id);
        updateJobToMaster(result, id);
        return new RestfulResponse(result, result ? "开启成功" : "开启失败");
    }

    @RequestMapping(value = "/generateVersion", method = RequestMethod.POST)
    @ResponseBody
    public WebAsyncTask<String> generateVersion(String jobId) {
        if (!hasPermission(Integer.parseInt(jobId), JOB)) {
            return new WebAsyncTask<>(() -> ERROR_MSG);
        }
        WebAsyncTask<String> asyncTask = new WebAsyncTask<>(HeraGlobalEnvironment.getRequestTimeout(), () ->
                workClient.generateActionFromWeb(JobExecuteKind.ExecuteKind.ManualKind, jobId));
        asyncTask.onTimeout(() -> "版本生成时间较长，请耐心等待下");
        return asyncTask;
    }


    @RequestMapping(value = "/generateAllVersion", method = RequestMethod.GET)
    @ResponseBody
    public WebAsyncTask<String> generateAllVersion() {
        if (!isAdmin(getOwner())) {
            return new WebAsyncTask<>(() -> ERROR_MSG);
        }
        WebAsyncTask<String> asyncTask = new WebAsyncTask<>(HeraGlobalEnvironment.getRequestTimeout(), () ->
                workClient.generateActionFromWeb(JobExecuteKind.ExecuteKind.ManualKind, Constants.ALL_JOB_ID));
        asyncTask.onTimeout(() -> "全量版本生成时间较长，请耐心等待下");
        return asyncTask;
    }

    /**
     * 获取任务历史版本
     *
     * @param pageHelper
     * @return
     */
    @RequestMapping(value = "/getJobHistory", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getJobHistory(PageHelper pageHelper) {
        return heraJobHistoryService.findLogByPage(pageHelper);
    }

    @RequestMapping(value = "/getHostGroupIds", method = RequestMethod.GET)
    @ResponseBody
    public List<HeraHostGroup> getHostGroupIds() {
        return heraHostGroupService.getAll();
    }

    /**
     * 取消正在执行的任务
     *
     * @param jobId
     * @param historyId
     * @return
     */
    @RequestMapping(value = "/cancelJob", method = RequestMethod.GET)
    @ResponseBody
    public WebAsyncTask<String> cancelJob(String historyId, String jobId) {
        if (!hasPermission(Integer.parseInt(jobId), JOB)) {
            return new WebAsyncTask<>(() -> ERROR_MSG);
        }

        HeraJobHistory history = heraJobHistoryService.findById(historyId);
        JobExecuteKind.ExecuteKind kind;
        if (TriggerTypeEnum.parser(history.getTriggerType()) == TriggerTypeEnum.MANUAL) {
            kind = JobExecuteKind.ExecuteKind.ManualKind;
        } else {
            kind = JobExecuteKind.ExecuteKind.ScheduleKind;
        }
        JobExecuteKind.ExecuteKind finalKind = kind;

        WebAsyncTask<String> webAsyncTask = new WebAsyncTask<>(HeraGlobalEnvironment.getRequestTimeout(), () ->
                workClient.cancelJobFromWeb(finalKind, historyId));
        webAsyncTask.onTimeout(() -> "任务取消执行中，请耐心等待");
        return webAsyncTask;
    }

    @RequestMapping(value = "getLog", method = RequestMethod.GET)
    @ResponseBody
    public HeraJobHistory getJobLog(Integer id) {
        return heraJobHistoryService.findLogById(id);
    }


    @RequestMapping(value = "/execute", method = RequestMethod.GET)
    @ResponseBody
    @UnCheckLogin
    public WebAsyncTask<RestfulResponse> zeusExecute(Integer id, String owner) {
        List<HeraAction> actions = heraJobActionService.findByJobId(String.valueOf(id));
        if (actions == null) {
            return new WebAsyncTask<>(() -> new RestfulResponse(false, "action为空"));
        }
        return execute(actions.get(actions.size() - 1).getId().toString(), 2, owner);

    }

    private void updateJobToMaster(boolean result, Integer id) {
        if (result) {
            poolExecutor.execute(() -> {
                try {
                    workClient.updateJobFromWeb(String.valueOf(id));
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }


    private Map<String, String> getInheritConfig(Integer groupId) {
        HeraGroup group = heraGroupService.findConfigById(groupId);
        Map<String, String> configMap = new HashMap<>(64);
        while (group != null && groupId != null && groupId != 0) {
            configMap.putAll(StringUtil.convertStringToMap(group.getConfigs()));
            groupId = group.getParent();
            group = heraGroupService.findConfigById(groupId);
        }
        return configMap;
    }

    private boolean hasPermission(Integer id, String type) {
        String owner = getOwner();
        if (owner == null || id == null || type == null) {
            return false;
        }
        if (isAdmin(owner)) {
            return true;
        }
        if (JOB.equals(type)) {
            HeraJob job = heraJobService.findById(id);
            if (!(job != null && owner.equals(job.getOwner()))) {
                HeraPermission permission = heraPermissionService.findByCond(id, owner);
                if (permission == null) {
                    permission = heraPermissionService.findByCond(job.getGroupId(), owner);
                    if (permission == null) {
                        return false;
                    }
                }
            }
        } else if (GROUP.equals(type)) {
            HeraGroup group = heraGroupService.findById(id);
            if (!(group != null && owner.equals(group.getOwner()))) {
                HeraPermission permission = heraPermissionService.findByCond(id, owner);
                if (permission == null) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isAdmin(String owner) {
        return HeraGlobalEnvironment.getAdmin().equals(owner);
    }


    private String getuIds(Integer id) {
        List<HeraPermission> permissions = heraPermissionService.findByTargetId(id);
        StringBuilder uids = new StringBuilder("[ ");
        if (permissions != null && permissions.size() > 0) {
            permissions.forEach(x -> uids.append(x.getUid()).append(" "));
        }
        uids.append("]");

        return uids.toString();
    }

    private String checkDependencies(Integer id, boolean isGroup) {
        List<HeraJob> allJobs = heraJobService.findAllDependencies();

        if (isGroup) {
            List<HeraJob> jobList = heraJobService.findByPid(id);
            StringBuilder openJob = new StringBuilder("无法删除存在任务的目录:[ ");
            for (HeraJob job : jobList) {
                openJob.append(job.getId()).append(" ");
            }
            openJob.append("]");
            if (jobList.size() > 0) {
                return openJob.toString();
            }
            return null;
        } else {
            HeraJob job = heraJobService.findById(id);
            if (job.getAuto() == 1) {
                return "无法删除正在开启的任务";
            }

            boolean canDelete = true;
            boolean isFirst = true;

            String deleteJob = String.valueOf(job.getId());
            StringBuilder dependenceJob = new StringBuilder("任务依赖: ");
            String[] dependenceJobs;
            for (HeraJob allJob : allJobs) {
                if (StringUtils.isNotBlank(allJob.getDependencies())) {
                    dependenceJobs = allJob.getDependencies().split(",");
                    for (String jobId : dependenceJobs) {
                        if (jobId.equals(deleteJob)) {
                            if (canDelete) {
                                canDelete = false;
                            }
                            if (isFirst) {
                                isFirst = false;
                                dependenceJob.append("[").append(job.getId()).append(" -> ").append(allJob.getId()).append(" ");
                            } else {
                                dependenceJob.append(allJob.getId()).append(" ");
                            }
                            break;
                        }
                    }
                }
            }
            dependenceJob.append("]").append("\n");
            if (!canDelete) {
                return dependenceJob.toString();
            }
            return null;
        }
    }


    @RequestMapping(value = "/getJobImpactOrProgress", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse getJobImpactOrProgress(Integer jobId, Integer type) {
        return heraJobService.findCurrentJobGraph(jobId, type);
    }

    private Integer getGroupId(String group) {
        String groupNum = group;
        if (group.startsWith(Constants.GROUP_PREFIX)) {
            groupNum = group.split("_")[1];
        }
        Integer res;
        try {
            res = Integer.parseInt(groupNum);
        } catch (Exception e) {
            throw new IllegalArgumentException("无法识别的groupId：" + group);
        }

        return res;
    }

}
