package com.dfire.controller;

import com.alibaba.fastjson.JSONArray;
import com.dfire.common.entity.*;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.entity.vo.HeraGroupVo;
import com.dfire.common.entity.vo.HeraJobTreeNodeVo;
import com.dfire.common.entity.vo.HeraJobVo;
import com.dfire.common.entity.vo.PageHelper;
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
import com.dfire.core.message.Protocol.ExecuteKind;
import com.dfire.core.netty.worker.WorkClient;
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

    private final String JOB = "job";
    private final String GROUP = "group";
    private final String ERROR_MSG = "抱歉，您没有权限进行此操作";


    @RequestMapping()
    public String login() {
        return "scheduleCenter/scheduleCenter.index";
    }

    @RequestMapping(value = "/init", method = RequestMethod.POST)
    @ResponseBody
    public List<HeraJobTreeNodeVo> initJobTree() {
        return heraJobService.buildJobTree();
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
        focusUsers.append("]");
        heraJobVo.setUIdS(getuIds(jobId));
        heraJobVo.setFocusUser(focusUsers.toString());
        return heraJobVo;
    }

    @RequestMapping(value = "/getGroupMessage", method = RequestMethod.GET)
    @ResponseBody
    public HeraGroupVo getGroupMessage(Integer groupId) {
        HeraGroup group = heraGroupService.findById(groupId);
        HeraGroupVo groupVo = BeanConvertUtils.convert(group);
        groupVo.setInheritConfig(getInheritConfig(groupVo.getParent()));
        groupVo.setUIdS(getuIds(groupId));
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
            for (int i = 0; i < uIdS.size(); i++) {
                HeraPermission heraPermission = new HeraPermission();
                heraPermission.setType(typeStr);
                heraPermission.setGmtModified(date);
                heraPermission.setGmtCreate(date);
                heraPermission.setTargetId(targetId);
                heraPermission.setUid((String) uIdS.get(i));
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
    public JsonResponse getJobOperator(Integer jobId, boolean type) {

        if (!hasPermission(jobId, type ? GROUP : JOB)) {
            return new JsonResponse(false, ERROR_MSG);
        }

        List<HeraPermission> permissions = heraPermissionService.findByTargetId(jobId);
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

        TriggerTypeEnum triggerTypeEnum = null;
        if (triggerType == 1) {
            triggerTypeEnum = TriggerTypeEnum.MANUAL;
        } else if (triggerType == 2) {
            triggerTypeEnum = TriggerTypeEnum.MANUAL_RECOVER;
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
        actionHistory.setActionId(heraAction.getId());
        actionHistory.setTriggerType(triggerTypeEnum.getId());
        actionHistory.setOperator(owner);
        actionHistory.setIllustrate(owner);
        actionHistory.setStatus(StatusEnum.RUNNING.toString());
        actionHistory.setStatisticEndTime(heraAction.getStatisticEndTime());
        actionHistory.setHostGroupId(heraAction.getHostGroupId());
        actionHistory.setProperties(configs);
        heraJobHistoryService.insert(actionHistory);
        heraAction.setScript(heraJob.getScript());
        heraJobActionService.update(heraAction);

        return new WebAsyncTask<>(3000, () -> {
            try {

                workClient.executeJobFromWeb(ExecuteKind.ManualKind, actionHistory.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new RestfulResponse(true, actionId);
        });
    }

    @RequestMapping(value = "/getJobVersion", method = RequestMethod.GET)
    @ResponseBody
    public List<HeraAction> getJobVersion(String jobId) {
        List<HeraAction> list = heraJobActionService.findByJobId(jobId);
        list.sort((x1, x2) -> -Long.compare(Long.parseLong(x1.getId()), Long.parseLong(x2.getId())));
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
    public RestfulResponse updateGroupMessage(HeraGroupVo groupVo) {
        if (!hasPermission(groupVo.getId(), GROUP)) {
            return new RestfulResponse(false, ERROR_MSG);
        }
        HeraGroup heraGroup = BeanConvertUtils.convert(groupVo);
        boolean res = heraGroupService.update(heraGroup) > 0;
        return new RestfulResponse(res, res ? "更新成功" : "系统异常,请联系管理员");
    }

    @RequestMapping(value = "/deleteJob", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse deleteJob(Integer id, Boolean isGroup) {
        if (!hasPermission(id, isGroup ? GROUP : JOB)) {
            return new RestfulResponse(false, ERROR_MSG);
        }
        boolean res;
        String check = checkDependencies(id, isGroup);
        if (StringUtils.isNotBlank(check)) {
            return new RestfulResponse(false, check);
        }

        if (isGroup) {
            res = heraGroupService.delete(id) > 0;
            return new RestfulResponse(res, res ? "删除成功" : "系统异常,请联系管理员");

        }
        res = heraJobService.delete(id) > 0;
        updateJobToMaster(res, id);
        return new RestfulResponse(res, res ? "删除成功" : "系统异常,请联系管理员");
    }

    @RequestMapping(value = "/addJob", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse addJob(HeraJob heraJob) {
        if (!hasPermission(heraJob.getGroupId(), GROUP)) {
            return new RestfulResponse(false, ERROR_MSG);
        }

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
    public RestfulResponse addJob(HeraGroup heraGroup) {
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
        return new WebAsyncTask<>(3000, () ->
                workClient.generateActionFromWeb(ExecuteKind.ManualKind, jobId));
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
    public List<Integer> getHostGroupIds() {
        List<HeraHostGroup> all = heraHostGroupService.getAll();

        List<Integer> res = new ArrayList<>();

        for (HeraHostGroup hostGroup : all) {
            res.add(hostGroup.getId());
        }
        return res;
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
        ExecuteKind kind;
        if (TriggerTypeEnum.parser(history.getTriggerType()) == TriggerTypeEnum.MANUAL) {
            kind = ExecuteKind.ManualKind;
        } else {
            kind = ExecuteKind.ScheduleKind;
        }
        ExecuteKind finalKind = kind;
        return new WebAsyncTask<>(3000, () ->
                workClient.cancelJobFromWeb(finalKind, historyId));
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
        return execute(actions.get(actions.size() - 1).getId(), 2, owner);

    }

    private void updateJobToMaster(boolean result, Integer id) {
        if (result) {
            ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(
                    1, 1, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new NamedThreadFactory("updateJob"), new ThreadPoolExecutor.AbortPolicy());
            poolExecutor.execute(() -> {
                try {
                    workClient.updateJobFromWeb(String.valueOf(id));
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            poolExecutor.shutdown();
        }
    }


    private Map<String, String> getInheritConfig(Integer groupId) {
        HeraGroup group;
        Map<String, String> configMap = new HashMap<>();
        while (groupId != null && groupId != 0) {
            group = heraGroupService.findConfigById(groupId);
            if (group.getConfigs() != null) {
                configMap.putAll(StringUtil.convertStringToMap(group.getConfigs()));
            }
            groupId = group.getParent();
        }
        return configMap;
    }

    private boolean hasPermission(Integer id, String type) {
        String owner = getOwner();
        if (owner == null || id == null || type == null) {
            return false;
        }
        if (HeraGlobalEnvironment.getAdmin().equals(owner)) {
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
        List<HeraJob> jobList = new ArrayList<>();

        if (isGroup) {
            jobList = heraJobService.findByPid(id);
        } else {
            jobList.add(heraJobService.findById(id));
        }
        StringBuilder openJob = new StringBuilder("任务处于开启状态:[ ");
        boolean canDelete = true;
        for (HeraJob job : jobList) {
            if (job.getAuto() == 1) {
                openJob.append(job.getId()).append(" ");
                if (canDelete) {
                    canDelete = false;
                }
            }
        }
        openJob.append("]");
        if (!canDelete) {
            return openJob.toString();
        }
        canDelete = true;
        boolean isFirst;
        StringBuilder dependenceJob = new StringBuilder("任务依赖: ");
        for (HeraJob job : jobList) {
            isFirst = true;
            for (HeraJob allJob : allJobs) {
                if (StringUtils.isNotBlank(allJob.getDependencies())) {
                    if (allJob.getDependencies().contains(String.valueOf(job.getId())) || allJob.getDependencies().contains("," + job.getId()) || allJob.getDependencies().contains(job.getId() + ",")) {
                        if (canDelete) {
                            canDelete = false;
                        }
                        if (isFirst) {
                            isFirst = false;
                            dependenceJob.append("[").append(job.getId()).append(" -> ").append(allJob.getId()).append(" ");
                        } else {
                            dependenceJob.append(allJob.getId()).append(" ");
                        }
                    }
                }
            }
            if (!isFirst) {
                dependenceJob.append("]").append("\n");
            }
        }

        if (!canDelete) {
            return dependenceJob.toString();
        }
        return null;
    }


    @RequestMapping(value = "/getJobImpactOrProgress", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse getJobImpactOrProgress(Integer jobId, Integer type) {
        return heraJobService.findCurrentJobGraph(jobId, type);
    }

}
