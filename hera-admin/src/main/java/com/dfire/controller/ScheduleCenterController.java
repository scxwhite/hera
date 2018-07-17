package com.dfire.controller;

import com.cloudera.org.jets3t.service.model.WebsiteConfig;
import com.dfire.common.entity.*;
import com.dfire.common.entity.vo.HeraGroupVo;
import com.dfire.common.entity.vo.HeraJobTreeNodeVo;
import com.dfire.common.entity.vo.HeraJobVo;
import com.dfire.common.enums.HttpCode;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.service.HeraGroupService;
import com.dfire.common.service.HeraJobActionService;
import com.dfire.common.service.HeraJobHistoryService;
import com.dfire.common.service.HeraJobService;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.common.util.NamedThreadFactory;
import com.dfire.common.vo.RestfulResponse;
import com.dfire.config.WebSecurityConfig;
import com.dfire.core.message.Protocol.ExecuteKind;
import com.dfire.core.netty.worker.WorkClient;
import com.dfire.core.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.WebAsyncTask;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Objects;
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
public class ScheduleCenterController {

    @Autowired
    HeraJobService heraJobService;
    @Autowired
    HeraJobActionService heraJobActionService;
    @Autowired
    HeraGroupService heraGroupService;
    @Autowired
    HeraJobHistoryService heraJobHistoryService;
    @Autowired
    WorkClient workClient;


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
        return heraJobVo;
    }

    @RequestMapping(value = "/getGroupMessage", method = RequestMethod.GET)
    @ResponseBody
    public HeraGroupVo getGroupMessage(Integer groupId) {
        HeraGroup group = heraGroupService.findById(groupId);
        HeraGroupVo heraGroupVo = BeanConvertUtils.convert(group);
        return heraGroupVo;
    }

    /**
     * 手动执行任务
     *
     * @param actionId
     * @return
     */
    @RequestMapping(value = "/manual", method = RequestMethod.GET)
    @ResponseBody
    public WebAsyncTask<String> manual(String actionId, Integer triggerType) {
        ExecuteKind kind = null;
        TriggerTypeEnum triggerTypeEnum = null;
        if (triggerType == 1) {
            triggerTypeEnum = TriggerTypeEnum.MANUAL;
        } else if (triggerType == 2) {
            triggerTypeEnum = TriggerTypeEnum.MANUAL_RECOVER;
        }
        //todo 权限判定

        HeraAction heraAction = heraJobActionService.findById(actionId);
        HeraJob heraJob = heraJobService.findById(Integer.parseInt(heraAction.getJobId()));
        String configs = heraJob.getConfigs();
        HeraJobHistory actionHistory = HeraJobHistory.builder().build();
        actionHistory.setJobId(heraAction.getJobId());
        actionHistory.setActionId(heraAction.getId());
        actionHistory.setTriggerType(triggerTypeEnum.getId());
        actionHistory.setOperator(heraAction.getOwner());
        actionHistory.setIllustrate(heraJob.getOwner());
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
            return "";
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
        HeraJob heraJob = BeanConvertUtils.convertToHeraJob(heraJobVo);
        RestfulResponse response = heraJobService.checkAndUpdate(heraJob);
        updateJobToMaster(response.isSuccess(), heraJob.getId());
        return response;
    }

    @RequestMapping(value = "/updateGroupMessage", method = RequestMethod.POST)
    @ResponseBody
    public boolean updateGroupMessage(HeraGroupVo groupVo) {
        HeraGroup heraGroup = BeanConvertUtils.convert(groupVo);
        return heraGroupService.update(heraGroup) > 0;
    }

    @RequestMapping(value = "/deleteJob", method = RequestMethod.POST)
    @ResponseBody
    public boolean deleteJob(Integer id, Boolean isGroup) {
        if (isGroup) {
            return heraGroupService.delete(id) > 0;
        }
        boolean res = heraJobService.delete(id) > 0;
        updateJobToMaster(res, id);
        return res;
    }

    @RequestMapping(value = "/addJob", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse addJob(HeraJob heraJob, HttpServletRequest request) {
        heraJob.setOwner(JwtUtils.getObjectFromToken(WebSecurityConfig.TOKEN_NAME, request, WebSecurityConfig.SESSION_KEY));
        return new RestfulResponse(heraJobService.insert(heraJob) > 0 ? HttpCode.REQUEST_SUCCESS : HttpCode.REQUEST_FAIL);
    }

    @RequestMapping(value = "/changeSwitch", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse changeSwitch(Integer id) {
        boolean result = heraJobService.changeSwitch(id);
        updateJobToMaster(result, id);
        return new RestfulResponse(result ? HttpCode.REQUEST_SUCCESS : HttpCode.REQUEST_FAIL);
    }

    @RequestMapping(value = "/generateVersion", method = RequestMethod.POST)
    @ResponseBody
    public WebAsyncTask<String> generateVersion(String jobId) {
        return new WebAsyncTask<>(3000, () ->
                workClient.generateActionFromWeb(ExecuteKind.ManualKind, jobId));
    }

    /**
     * 获取任务历史版本
     *
     * @param jobId
     * @return
     */
    @RequestMapping(value = "/getJobHistory", method = RequestMethod.GET)
    @ResponseBody
    public List<HeraJobHistory> getJobHistory(String jobId) {
        List<HeraJobHistory> list = heraJobHistoryService.findByJobId(jobId);
        return list;

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
        HeraJobHistory history = heraJobHistoryService.findById(id);
        ExecuteKind kind = null;
        if (TriggerTypeEnum.parser(history.getTriggerType()) == TriggerTypeEnum.MANUAL) {
            kind = ExecuteKind.ManualKind;
        } else {
            kind = ExecuteKind.ScheduleKind;
        }
        ExecuteKind finalKind = kind;
        return new WebAsyncTask<String>(3000, () ->
                workClient.cancelJobFromWeb(finalKind, id));

    }

    @RequestMapping(value = "getLog", method = RequestMethod.GET)
    @ResponseBody
    public HeraJobHistory getJobLog(Integer id) {
        return heraJobHistoryService.findLogById(id);
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

}
