package com.dfire.controller;

import com.dfire.common.entity.HeraUser;
import com.dfire.common.enums.HttpCode;
import com.dfire.common.service.HeraUserService;
import com.dfire.common.util.StringUtil;
import com.dfire.common.vo.RestfulResponse;
import com.dfire.config.WebSecurityConfig;
import com.dfire.core.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
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
    public String toLogin() {
        return "redirect:/login";
    }

    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping("/home")
    public String index() {
        return "home";
    }

    @RequestMapping(value = "/loginCheck", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse toLogin(String userName, String password, HttpServletResponse response) {
        HeraUser user = heraUserService.findByName(userName);

        if (user == null) {
            return RestfulResponse.builder().code(HttpCode.USER_NOT_LOGIN.getCode()).build();
        }
        String pwd = user.getPassword();
        RestfulResponse restfulResponse = new RestfulResponse();
        if (!StringUtils.isEmpty(password)) {
            password = StringUtil.EncoderByMd5(password);
            if (pwd.equals(password)) {
                restfulResponse.setHttpCode(HttpCode.REQUEST_SUCCESS);
                Cookie cookie = new Cookie(WebSecurityConfig.TOKEN_NAME, JwtUtils.createToken(userName));
                cookie.setMaxAge(3 * 60 * 60 * 24);
                response.addCookie(cookie);
            } else {
                restfulResponse.setHttpCode(HttpCode.USER_NOT_LOGIN);
            }
        }
        return restfulResponse;
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse register(HeraUser user) {
        HeraUser check = heraUserService.findByName(user.getName());
        if (check != null) {
            return new RestfulResponse(false, "用户名已存在");
        }
        int res = heraUserService.insert(user);
        return new RestfulResponse(res > 0, res > 0 ? "注册成功，等待管理员审核" : "注册失败,请联系管理员");
    }


}
