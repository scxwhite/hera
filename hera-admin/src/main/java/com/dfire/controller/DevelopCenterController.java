package com.dfire.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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

    @RequestMapping(value = "/init", method = RequestMethod.GET)
    @ResponseBody
    public String initFileTree() {

        String result = "[" +
                " { name:文档中心, open:true," +
                " children: [" +
                "{ name:个人文档," +
                "                    children: [" +
                "                        { name:文档111}," +
                "                        { name:叶子节点112}," +
                "                        { name:叶子节点113}," +
                "                        { name:叶子节点114}" +
                "                    ]}," +
                "                { name:共享文档," +
                "                    children: [" +
                "                        { name:叶子节点121}," +
                "                        { name:叶子节点122}," +
                "                        { name:叶子节点123}," +
                "                        { name:叶子节点124}" +
                "                    ]}" +
                "            ]}" +
                "    ]";

        JSONObject object = new JSONObject();
        object.put("name","文档中心");
        object.put("open","true");
        object.put("children","个人文档");

        return  object.toJSONString();
    }
}
