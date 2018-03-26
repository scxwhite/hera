package com.dfire.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:50 2018/1/13
 * @desc 调度中心视图管理器
 */
@Controller
public class ScheduleCenterController {

    @RequestMapping("/scheduleCenter")
    public String login() {
        return "scheduleCenter/scheduleCenter.index";
    }

}
