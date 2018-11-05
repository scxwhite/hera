package com.dfire.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author xiaosuda
 * @date 2018/8/1
 */
@Slf4j
@Aspect
@Component
public class HeraAspect {


    @Pointcut("execution(* com.dfire.controller..*(..))")
    private void auth() {

    }

    @Around("auth()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Long start = System.currentTimeMillis();
        Object res = joinPoint.proceed();
        Long end = System.currentTimeMillis();
        if (start - end >= 10 * 1000L) {
            log.info("方法名:{},参数:{},耗时:{}ms", joinPoint.getSignature().getName(), Arrays.asList(joinPoint.getArgs()), end - start);
        }
        return res;
    }
}
