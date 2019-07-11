package com.dfire.controller;

import com.dfire.common.constants.Constants;
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

    protected String getOwner() {
        return JwtUtils.getObjectFromToken(Constants.TOKEN_NAME, requestThread.get(), Constants.SESSION_USERNAME);
    }

    protected String getSsoName() {
        return JwtUtils.getObjectFromToken(Constants.TOKEN_NAME, requestThread.get(), Constants.SESSION_SSO_NAME);
    }

    protected String getOwnerId() {
        return JwtUtils.getObjectFromToken(Constants.TOKEN_NAME, requestThread.get(), Constants.SESSION_USER_ID);
    }

    protected String getSsoId() {
        return JwtUtils.getObjectFromToken(Constants.TOKEN_NAME, requestThread.get(), Constants.SESSION_SSO_ID);
    }


    public static void remove() {
        requestThread.remove();
    }



}
