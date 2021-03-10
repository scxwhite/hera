package com.dfire.config;

import com.dfire.common.constants.Constants;
import com.dfire.controller.BaseHeraController;
import com.dfire.core.util.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午11:09 2018/5/22
 * @desc
 */
@Configuration
public class WebSecurityConfig extends WebMvcConfigurerAdapter {

    @Bean
    public SecurityInterceptor getSecurityInterceptor() {
        return new SecurityInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry interceptorRegistry) {
        InterceptorRegistration addRegistry = interceptorRegistry.addInterceptor(getSecurityInterceptor());
        addRegistry.excludePathPatterns("/error**").excludePathPatterns("/login/**");
    }

    private class SecurityInterceptor extends HandlerInterceptorAdapter {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            if (!(handler instanceof HandlerMethod)) {
                return true;
            }
            HandlerMethod method = (HandlerMethod) handler;
            UnCheckLogin methodAnnotation = method.getMethodAnnotation(UnCheckLogin.class);
            if (methodAnnotation != null) {
                return true;
            }
            UnCheckLogin declaredAnnotation = method.getBeanType().getDeclaredAnnotation(UnCheckLogin.class);
            if (declaredAnnotation != null) {
                return true;
            }
            String heraToken = JwtUtils.getValFromCookies(Constants.TOKEN_NAME, request);
            if (StringUtils.isNotBlank(heraToken) && JwtUtils.verifyToken(heraToken)) {
                return true;
            }
            request.getRequestDispatcher("/login").forward(request, response);
            return false;
        }


        @Override
        public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
            BaseHeraController.remove();
            super.postHandle(request, response, handler, modelAndView);
        }
    }


}
