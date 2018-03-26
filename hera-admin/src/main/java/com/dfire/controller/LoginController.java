package com.dfire.controller;

import com.dfire.common.entity.ZeusUser;
import com.dfire.common.util.StringUtil;
import com.dfire.common.vo.RestfulResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.dfire.common.service.ZeusHostGroupService;
import com.dfire.common.service.ZeusJobService;
import com.dfire.common.service.ZeusUserService;


/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:34 2018/1/1
 * @desc 登陆控制器
 */

@Controller
public class LoginController {

    @Autowired
    private ZeusUserService zeusUserService;
    @Autowired
    private ZeusJobService zeusJobService;
    @Autowired
    private ZeusHostGroupService hostGroupService;

    @RequestMapping("/")
    public String login() {
        return "login";
    }

    @RequestMapping("/home")
    public String index() {
        return "home";
    }

    @RequestMapping(value="/toLogin", method=RequestMethod.POST)
    @ResponseBody
    public RestfulResponse toLogin(String userName, String password) {
        zeusJobService.findByName(182);
        ZeusUser user = zeusUserService.findByName(userName);
        String pwd = user.getPassword();
        RestfulResponse response = RestfulResponse
                .builder().build();
        if (!StringUtils.isEmpty(password)) {
            password = StringUtil.EncoderByMd5(password);
            if (pwd.equals(password)) {
                response.setCode("200");
                response.setSuccess(true);
            } else {
                response.setCode("400");
            }
        }
        return response;
    }


}
