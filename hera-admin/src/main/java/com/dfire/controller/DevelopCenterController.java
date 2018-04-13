package com.dfire.controller;

import com.alibaba.fastjson.JSONArray;
import com.dfire.common.util.HeraFileUtil;
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

    @RequestMapping(value = "/init", method = RequestMethod.POST)
    @ResponseBody
    public String initFileTree() {

        JSONArray json = HeraFileUtil.parseHeraToJson();
        String result = json.toJSONString().replaceAll("\\\\","").replaceAll("\\\"","");
        return result;
    }
}
