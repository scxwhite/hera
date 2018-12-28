package com.dfire.controller;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraUser;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.service.HeraUserService;
import com.dfire.common.util.StringUtil;
import com.dfire.config.UnCheckLogin;
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
        return "/login";
    }

    @RequestMapping("/home")
    public String index() {
        return "home";
    }

    @RequestMapping(value = "/loginCheck", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse toLogin(String userName, String password, HttpServletResponse response) {
        HeraUser user = heraUserService.findByName(userName);

        if (user == null) {
            return new JsonResponse(false, "用户不存在");
        }

        String pwd = user.getPassword();
        if (!StringUtils.isEmpty(password)) {
            password = StringUtil.EncoderByMd5(password);
            if (pwd.equals(password)) {
                if (user.getIsEffective() == 0) {
                    return new JsonResponse(false, "审核未通过,请联系管理员");
                }
                Cookie cookie = new Cookie(WebSecurityConfig.TOKEN_NAME, JwtUtils.createToken(userName, String.valueOf(user.getId())));
                cookie.setMaxAge(Constants.LOGIN_TIME_OUT);
                response.addCookie(cookie);
                return new JsonResponse(true, "登录成功");
            } else {
                return new JsonResponse(false, "密码错误，请重新输入");
            }
        }
        return new JsonResponse(false, "请输入密码");
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody
    @UnCheckLogin
    public JsonResponse register(HeraUser user) {
        HeraUser check = heraUserService.findByName(user.getName());
        if (check != null) {
            return new JsonResponse(false, "用户名已存在，请更换用户名");
        }
        int res = heraUserService.insert(user);
        return new JsonResponse(res > 0, res > 0 ? "注册成功，等待管理员审核" : "注册失败,请联系管理员");
    }


}
