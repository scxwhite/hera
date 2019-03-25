package com.dfire.controller;


import com.dfire.config.UnCheckLogin;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * desc:
 *
 * @author scx
 * @create 2019/03/25
 */
@Controller
public class HealthController {


    private static String flag = "not_ok";

    @RequestMapping("/health.do")
    @ResponseBody
    @UnCheckLogin
    String healthCheck() {
        return "ok";
    }

    @RequestMapping("/hc.do")
    @ResponseBody
    @UnCheckLogin
    String doHC(@RequestParam String a) {
        // SLB 心跳
        if ("heartbeat".equals(a)) {
            return flag;
        }
        // 下线
        else if ("offline".equals(a)) {
            flag = "not_ok";
            return "ok";
        }
        // 上线
        else if ("online".equals(a)) {
            flag = "ok";
            return "ok";

        }
        // 健康检查
        else if ("healthCheck".equals(a)) {
            return "ok";
        } else {
            return "not_ok";
        }
    }


}