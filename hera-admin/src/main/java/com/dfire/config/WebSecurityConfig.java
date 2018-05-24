package com.dfire.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

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

    @Bean
    public SecurityInterceptor getSecurityInterceptor() {
        return new SecurityInterceptor();
    }

    public void addInterceptors(InterceptorRegistry interceptorRegistry) {
        InterceptorRegistration addRegistry = interceptorRegistry.addInterceptor(getSecurityInterceptor());
        addRegistry.excludePathPatterns("/error");
        addRegistry.excludePathPatterns("/login**");
        addRegistry.excludePathPatterns("/**");
    }


    private class SecurityInterceptor extends HandlerInterceptorAdapter {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            HttpSession session = request.getSession();
            if(session.getAttribute(SESSION_KEY) != null) {
                return true;
            }
            String url = "/login";
            response.sendRedirect(url);
            return false;
        }
    }
}
