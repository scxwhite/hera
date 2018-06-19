package com.dfire.controller;

import com.alibaba.fastjson.JSONObject;
import com.dfire.common.entity.*;
import com.dfire.common.entity.vo.HeraGroupVo;
import com.dfire.common.entity.vo.HeraJobTreeNodeVo;
import com.dfire.common.entity.vo.HeraJobVo;
import com.dfire.common.service.HeraGroupService;
import com.dfire.common.service.HeraJobActionService;
import com.dfire.common.service.HeraJobService;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.config.WebSecurityConfig;
import com.dfire.core.message.Protocol.*;
import com.dfire.core.netty.worker.WorkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.WebAsyncTask;

import javax.servlet.http.HttpSession;
import java.util.List;

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
    WorkClient workClient;


    @RequestMapping()
    public String login() {
        return "scheduleCenter/scheduleCenter.index";
    }

    @RequestMapping(value = "/init", method = RequestMethod.POST)
    @ResponseBody
    public List<HeraJobTreeNodeVo> initJobTree(HttpSession session) {
        List<HeraJobTreeNodeVo> list = heraJobService.buildJobTree();
        HeraUser user = (HeraUser) session.getAttribute(WebSecurityConfig.SESSION_KEY);
        if(user != null) {
            String name = user.getName();

        }
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
    public WebAsyncTask<String> manual(String actionId) {

        return new WebAsyncTask<>(3000, () -> {
            HeraAction heraAction = heraJobActionService.findById(actionId);
            try {
                workClient.executeJobFromWeb(ExecuteKind.ManualKind, actionId);
            } catch (Exception e) {

            }
            return "";
        });
    }

    @RequestMapping(value = "/getJobVersion", method = RequestMethod.GET)
    @ResponseBody
    public List<HeraAction> getJobVersion(String jobId) {
        List<HeraAction> list = heraJobActionService.findByJobId(jobId);
        return list;

    }
    @RequestMapping(value = "/updateJobMessage", method = RequestMethod.POST)
    @ResponseBody
    public boolean updateJobMessage(HeraJobVo heraJobVo) {
        HeraJob heraJob = BeanConvertUtils.convertToHeraJob(heraJobVo);
        return heraJobService.update(heraJob) > 0;
    }

    @RequestMapping(value = "/updateGroupMessage", method = RequestMethod.POST)
    @ResponseBody
    public boolean updateGroupMessage(HeraGroupVo groupVo) {
        HeraGroup heraGroup = BeanConvertUtils.convert(groupVo);
        System.out.println(JSONObject.toJSONString(heraGroup));
        return heraGroupService.update(heraGroup) > 0 ;
    }

    @RequestMapping(value = "/deleteJob", method = RequestMethod.POST)
    @ResponseBody
    public boolean deleteJob(Integer id, Boolean isGroup) {
        if (isGroup) {
            return heraGroupService.delete(id) > 0;
        }
        return  heraJobService.delete(id) > 0;
    }

}
