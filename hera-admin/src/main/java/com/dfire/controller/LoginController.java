package com.dfire.controller;

import com.alibaba.fastjson.JSONObject;
import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraSso;
import com.dfire.common.entity.HeraUser;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.enums.RecordTypeEnum;
import com.dfire.common.service.HeraSsoService;
import com.dfire.common.service.HeraUserService;
import com.dfire.common.util.StringUtil;
import com.dfire.config.UnCheckLogin;
import com.dfire.core.util.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @author: 凌霄
 * @time: Created in 16:34 2018/1/1
 * @desc 登陆控制器
 */

@Api("登陆控制器")
@Controller
public class LoginController extends BaseHeraController {

    @Autowired
    private HeraUserService heraUserService;

    @Autowired
    private HeraSsoService heraSsoService;


    @RequestMapping(value = "/",method = RequestMethod.GET)
    public String toLogin() {
        return "redirect:/login";
    }

    @RequestMapping(value = "/login",method = RequestMethod.GET)
    public String login() {
        return "/login";
    }

    @RequestMapping(value = "/login/admin",method = RequestMethod.GET)
    public String admin() {
        return "/admin";
    }

    @RequestMapping(value = "/home",method = RequestMethod.GET)
    public String index() {
        return "home";
    }

    @UnCheckLogin
    @ResponseBody
    @RequestMapping(value = "admin/login", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("用户组登陆接口")
    public JsonResponse adminLogin(@ApiParam(value = "sso对象",required = true)String userName
            , @ApiParam(value = "密码，32为md5",required = true)String password
            , @ApiIgnore HttpServletResponse response) {
        HeraUser user = heraUserService.findByName(userName.split("@")[0]);
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
                Cookie cookie = new Cookie(Constants.TOKEN_NAME, JwtUtils.createToken(userName, String.valueOf(user.getId()), Constants.DEFAULT_ID, userName));
                cookie.setMaxAge(Constants.LOGIN_TIME_OUT);
                cookie.setPath("/");
                response.addCookie(cookie);
                addUserRecord(user.getId(), "组账户", RecordTypeEnum.LOGIN, userName, String.valueOf(user.getId()));
                return new JsonResponse(true, "登录成功");
            } else {
                return new JsonResponse(false, "密码错误，请重新输入");
            }
        }
        return new JsonResponse(false, "请输入密码");
    }

    @ResponseBody
    @UnCheckLogin
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "admin/register", method = RequestMethod.POST)
    @ApiOperation("用户组注册接口")
    public JsonResponse adminRegister(@ApiParam(value = "用户组对象",required = true)HeraUser user) {
        HeraUser check = heraUserService.findByName(user.getName());
        if (check != null) {
            return new JsonResponse(false, "用户名已存在，请更换用户名");
        }
        int res = heraUserService.insert(user);
        return new JsonResponse(res > 0, res > 0 ? "注册成功，等待管理员审核" : "注册失败,请联系管理员");
    }

    @ResponseBody
    @UnCheckLogin
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "sso/login", method = RequestMethod.POST)
    @ApiOperation("用户登陆接口")
    public JsonResponse ssoLogin(@ApiParam(value = "sso对象",required = true) String userName
            ,@ApiParam(value = "密码，32为md5",required = true) String password
            ,@ApiIgnore HttpServletResponse response) {
        HeraSso heraSso = heraSsoService.findSsoByName(userName);
        if (heraSso == null) {
            return new JsonResponse(false, "用户不存在");
        }
        String pwd = heraSso.getPassword();
        if (!StringUtils.isEmpty(password)) {
            password = StringUtil.EncoderByMd5(password);
            if (pwd.equals(password)) {
                if (heraSso.getIsValid() == 0) {
                    return new JsonResponse(false, "审核未通过,请联系管理员");
                }

                String owner = Optional.of(heraUserService.findById(heraSso.getGid())).orElse(HeraUser.builder().name("read_only").build()).getName();
                Cookie cookie = new Cookie(Constants.TOKEN_NAME, JwtUtils.createToken(owner
                        , String.valueOf(heraSso.getGid())
                        , String.valueOf(heraSso.getId())
                        , userName));
                cookie.setMaxAge(Constants.LOGIN_TIME_OUT);
                cookie.setPath("/");
                response.addCookie(cookie);
                addUserRecord(heraSso.getId(), "sso账户", RecordTypeEnum.LOGIN, userName, String.valueOf(heraSso.getGid()));
                return new JsonResponse(true, "登录成功");
            } else {
                return new JsonResponse(false, "密码错误，请重新输入");
            }
        }
        return new JsonResponse(false, "请输入密码");
    }

    @ResponseBody
    @UnCheckLogin
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "sso/register", method = RequestMethod.POST)
    @ApiOperation("用户注册接口")
    public JsonResponse ssoRegister(@ApiParam(value = "sso对象",required = true) HeraSso heraSso) {
        heraSso.setName(heraSso.getEmail().substring(0, heraSso.getEmail().indexOf("@")));
        boolean exist = heraSsoService.checkByName(heraSso.getName());
        if (exist) {
            return new JsonResponse(false, "用户名已存在，请更换用户名");
        }
        boolean res = heraSsoService.addSso(heraSso);
        return new JsonResponse(res, res ? "注册成功，等待管理员审核" : "注册失败,请联系管理员");
    }

    @ResponseBody
    @UnCheckLogin
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "sso/groups", method = RequestMethod.GET)
    @ApiOperation("查询用户组列表")
    public JsonResponse ssoGroups() {
        List<HeraUser> users = heraUserService.getAll();
        if (users == null || users.size() == 0) {
            return new JsonResponse(false, "找不到部门,请联系管理员");
        }
        List<JSONObject> groups = users.stream().filter(user -> user.getIsEffective() == 1).map(x -> {
            JSONObject group = new JSONObject();
            group.put("id", x.getId());
            group.put("name", x.getName());
            return group;
        }).collect(Collectors.toList());
        return new JsonResponse(true, "查询成功", groups);
    }
}
