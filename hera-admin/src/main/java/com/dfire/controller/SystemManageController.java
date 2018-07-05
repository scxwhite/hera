package com.dfire.controller;

import com.dfire.common.service.HeraUserService;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.queue.JobElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:52 2018/1/13
 * @desc 系统管理
 */
@Controller
public class SystemManageController {

    @Autowired
    HeraUserService heraUserService;

    @Autowired
    MasterContext masterContext;

    @RequestMapping("userManage")
    public String userManage() {
        return "systemManage/userManage.index";
    }

    @RequestMapping("hostGroupManage")
    public String hostGroupManage() {
        return "systemManage/hostGroupManage.index";
    }

    @RequestMapping("/getTaskQueueStatus")
    @ResponseBody
    public Map getTaskQueueStatus() {
        Map<String, Queue<JobElement>> res = new HashMap<>(4);
        res.put("schedule", masterContext.getScheduleQueue());
        return res;
    }


}
