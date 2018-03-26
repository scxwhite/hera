package com.dfire.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:52 2018/1/13
 * @desc 系统管理
 */
@Controller
public class SystemManageController {

    @RequestMapping("userManage")
    public String userManage() {
        return "systemManage/userManage.index";
    }

    @RequestMapping("hostGroupManage")
    public String hostGroupManage() {
        return "systemManage/hostGroupManage.index";
    }

}
