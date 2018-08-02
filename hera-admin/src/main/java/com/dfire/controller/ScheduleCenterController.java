package com.dfire.controller;

import com.dfire.common.entity.*;
import com.dfire.common.entity.vo.HeraGroupVo;
import com.dfire.common.entity.vo.HeraJobTreeNodeVo;
import com.dfire.common.entity.vo.HeraJobVo;
import com.dfire.common.entity.vo.PageHelper;
import com.dfire.common.enums.HttpCode;
import com.dfire.common.enums.JobScheduleTypeEnum;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.service.*;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.common.util.NamedThreadFactory;
import com.dfire.common.util.StringUtil;
import com.dfire.common.vo.RestfulResponse;
import com.dfire.config.UnCheckLogin;
import com.dfire.core.message.Protocol.ExecuteKind;
import com.dfire.core.netty.worker.WorkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.WebAsyncTask;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private WorkClient workClient;

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
        List<HeraJobTreeNodeVo> list = heraJobService.buildJobTree();
        return list;
    }

    @RequestMapping(value = "/getJobMessage", method = RequestMethod.GET)
    @ResponseBody
    public HeraJobVo getJobMessage(Integer jobId) {
        HeraJob job = heraJobService.findById(jobId);
        HeraJobVo heraJobVo = BeanConvertUtils.convert(job);
        heraJobVo.setInheritConfig(getInheritConfig(job.getGroupId()));
        HeraJobMonitor monitor = heraJobMonitorService.findByJobId(jobId);
        if (monitor != null) {
            heraJobVo.setFocus(monitor.getUserIds().contains(getOwnerId()));
        }
        return heraJobVo;
    }

    @RequestMapping(value = "/getGroupMessage", method = RequestMethod.GET)
    @ResponseBody
    public HeraGroupVo getGroupMessage(Integer groupId) {
        HeraGroup group = heraGroupService.findById(groupId);
        HeraGroupVo groupVo = BeanConvertUtils.convert(group);
        groupVo.setInheritConfig(getInheritConfig(groupVo.getParent()));
        return groupVo;
    }

    /**
     * 手动执行任务
     *
     * @param actionId
     * @return
     */
    @RequestMapping(value = "/manual", method = RequestMethod.GET)
    @ResponseBody
    public WebAsyncTask<String> execute(String actionId, Integer triggerType, @RequestParam(required = false) String owner) {
        if (!hasPermission(Integer.parseInt(actionId.substring(actionId.length() - 4)), JOB)) {
            return new WebAsyncTask<>(() -> ERROR_MSG);
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
        actionHistory.setHostGroupId(heraAction.getHistoryId());
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
            return actionId;
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

    /**
     * 取消正在执行的任务
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/cancelJob", method = RequestMethod.GET)
    @ResponseBody
    public WebAsyncTask<String> cancelJob(String id) {
        if (!hasPermission(Integer.parseInt(id), JOB)) {
            return new WebAsyncTask<>(() -> ERROR_MSG);
        }

        HeraJobHistory history = heraJobHistoryService.findById(id);
        ExecuteKind kind;
        if (TriggerTypeEnum.parser(history.getTriggerType()) == TriggerTypeEnum.MANUAL) {
            kind = ExecuteKind.ManualKind;
        } else {
            kind = ExecuteKind.ScheduleKind;
        }
        ExecuteKind finalKind = kind;
        return new WebAsyncTask<>(3000, () ->
                workClient.cancelJobFromWeb(finalKind, id));
    }

    @RequestMapping(value = "getLog", method = RequestMethod.GET)
    @ResponseBody
    public HeraJobHistory getJobLog(Integer id) {
        return heraJobHistoryService.findLogById(id);
    }


    @RequestMapping(value = "/execute", method = RequestMethod.GET)
    @ResponseBody
    @UnCheckLogin
    public WebAsyncTask<String> zeusExecute(Integer id, String owner) {
        List<HeraAction> actions = heraJobActionService.findByJobId(String.valueOf(id));
        return execute(actions.get(0).getId(), 2, owner);

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
        if (JOB.equals(type)) {
            HeraJob job = heraJobService.findById(id);
            return job != null && owner.equals(job.getOwner());
        } else if (GROUP.equals(type)) {
            HeraGroup group = heraGroupService.findById(id);
            return group != null && owner.equals(group.getOwner());
        }

        return false;
    }


}
