package com.dfire.controller;

import com.dfire.common.entity.HeraUser;
import com.dfire.common.service.HeraHostGroupService;
import com.dfire.common.service.HeraJobService;
import com.dfire.common.service.HeraUserService;
import com.dfire.common.util.StringUtil;
import com.dfire.common.vo.RestfulResponse;
import com.dfire.config.WebSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;


/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:34 2018/1/1
 * @desc 登陆控制器
 */

@Controller
public class LoginController {

    @Autowired
    private HeraUserService heraUserService;

    @RequestMapping("/")
    public String login() {
        return "login";
    }

    @RequestMapping("/home")
    public String index() {
        return "home";
    }

    @RequestMapping(value = "/toLogin", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse toLogin(String userName, String password, HttpSession session) {
        HeraUser user = heraUserService.findByName(userName);

        if (user == null) {
            return RestfulResponse.builder().code("400").build();
        } else {
            session.setAttribute(WebSecurityConfig.SESSION_KEY, user);
        }
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

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse register(HeraUser user) {
        HeraUser check = heraUserService.findByName(user.getName());
        if (check != null) {
            return new RestfulResponse(false, "用户名已存在");
        }
        int res = heraUserService.insert(user);
        return new RestfulResponse( res > 0, res > 0 ? "注册成功，等待管理员审核" : "注册失败,请联系管理员");
    }


}
