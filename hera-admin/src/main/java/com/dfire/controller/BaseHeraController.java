package com.dfire.controller;

import com.dfire.config.WebSecurityConfig;
import com.dfire.core.util.JwtUtils;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;

/**
 * @author xiaosuda
 * @date 2018/7/19
 */
public abstract class BaseHeraController {


    private static ThreadLocal<HttpServletRequest> requestThread = new ThreadLocal<>();


    @ModelAttribute
    protected void setRequest(HttpServletRequest request) {
        requestThread.set(request);
    }

    protected HttpServletRequest getRequest() {
        return requestThread.get();
    }


    protected String getOwner() {
        return JwtUtils.getObjectFromToken(WebSecurityConfig.TOKEN_NAME, requestThread.get(), WebSecurityConfig.SESSION_USERNAME);
    }

    protected String getOwnerId() {
        return JwtUtils.getObjectFromToken(WebSecurityConfig.TOKEN_NAME, requestThread.get(), WebSecurityConfig.SESSION_USER_ID);
    }

    public static void remove() {
        requestThread.remove();
    }

}
