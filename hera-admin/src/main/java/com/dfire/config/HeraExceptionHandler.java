package com.dfire.config;

import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.entity.model.TableResponse;
import com.dfire.common.exception.NoPermissionException;
import com.dfire.logs.ErrorLog;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * desc:
 *
 * @author scx
 * @create 2019/06/18
 */
@ControllerAdvice
public class HeraExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Object handlerException(HttpServletRequest request, Exception ex) {
        ErrorLog.error("请求" + request.getRequestURI() + "异常:", ex);
        return getReturn(getReturnType(request), HttpStatus.INTERNAL_SERVER_ERROR, "请求异常,请联系管理员", "当您看到这个页面,表示该应用出错,请联系管理员进行解决");
    }


    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Object handlerMissParameterException(HttpServletRequest request) {
        return getReturn(getReturnType(request), HttpStatus.BAD_GATEWAY, "参数不完整,请核查", "当你看到这个页面，说明你请求的参数不正确，请核查");
    }

    @ExceptionHandler(NoPermissionException.class)
    public ModelAndView handlerNoPermissionException(HttpServletRequest request) {
        return getReturn(getReturnType(request), HttpStatus.FORBIDDEN, "操作无权限", "当你看到这个页面，说明你访问了你无权限访问的页面或者请求，请向管理员申请相关权限");
    }


    @ExceptionHandler(NoHandlerFoundException.class)
    public ModelAndView handlerNotFoundException(HttpServletRequest request) {
        ErrorLog.error("请求" + request.getRequestURI() + "404异常");
        return getReturn(NoHandlerFoundException.class, HttpStatus.NOT_FOUND, "page not found", "当您看到这个页面,表示您的访问出错,这个错误是您打开的页面不存在,请确认您输入的地址是正确的,如果是在本站点击后出现这个页面,请联系管理员进行处理感谢您的支持!");
    }

    private ModelAndView getReturn(Class<?> returnType, HttpStatus internalServerError, String errorMsg, String content) {
        if (returnType == JsonResponse.class) {
            ModelAndView json = new ModelAndView(new MappingJackson2JsonView());
            json.addAllObjects(new JsonResponse(false, errorMsg).toMap());
            return json;
        } else if (returnType == TableResponse.class) {
            ModelAndView json = new ModelAndView(new MappingJackson2JsonView());
            json.addAllObjects(new TableResponse(-1, errorMsg).toMap());
            return json;
        } else { //String 、ModelAndView都返回错误页面
            ModelAndView modelAndView = new ModelAndView("/error.index");
            modelAndView.addObject("msg", errorMsg);
            modelAndView.addObject("code", internalServerError.value());
            modelAndView.addObject("content", content);
            return modelAndView;
        }
    }

    private Class<?> getReturnType(HttpServletRequest request) {
        WebApplicationContext context = RequestContextUtils.findWebApplicationContext(request);
        Map<String, HandlerMapping> mappingMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
        HandlerExecutionChain chain;
        for (HandlerMapping handlerMapping : mappingMap.values()) {
            try {
                if (handlerMapping instanceof RequestMappingHandlerMapping && (chain = handlerMapping.getHandler(request)) != null) {
                    Method method = ((HandlerMethod) chain.getHandler()).getMethod();
                    return method.getReturnType();
                }
            } catch (Exception e) {
                ErrorLog.error("获取返回类型失败", e);
            }
        }
        return null;
    }


}
