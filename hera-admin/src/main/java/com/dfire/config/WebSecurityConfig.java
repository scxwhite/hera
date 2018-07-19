package com.dfire.config;

import com.dfire.controller.BaseHeraController;
import com.dfire.core.util.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午11:09 2018/5/22
 * @desc
 */
@Configuration
public class WebSecurityConfig extends WebMvcConfigurerAdapter {

    public final static String SESSION_KEY = "username";
    public final static String TOKEN_NAME = "HERA_Token";

    @Bean
    public SecurityInterceptor getSecurityInterceptor() {
        return new SecurityInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry interceptorRegistry) {
        InterceptorRegistration addRegistry = interceptorRegistry.addInterceptor(getSecurityInterceptor());
        addRegistry.excludePathPatterns("/error").excludePathPatterns("/login**");
    }


    private class SecurityInterceptor extends HandlerInterceptorAdapter {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            String heraToken = JwtUtils.getValFromCookies(TOKEN_NAME, request);
            if (StringUtils.isNotBlank(heraToken) && JwtUtils.verifyToken(heraToken)) {
                return true;
            }
            response.sendRedirect("login");
            return false;
        }


        @Override
        public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
            BaseHeraController.remove();
            super.postHandle(request, response, handler, modelAndView);
        }
    }


}
