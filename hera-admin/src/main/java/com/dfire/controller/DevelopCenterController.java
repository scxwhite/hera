package com.dfire.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:34 2018/1/13
 * @desc 开发中心页面控制器
 */
@Controller
@RequestMapping("/developCenter")
public class DevelopCenterController {

    @RequestMapping
    public String dev() {
        return "developCenter/developCenter.index";
    }
}
