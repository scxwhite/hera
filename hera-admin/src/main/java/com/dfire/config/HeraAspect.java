package com.dfire.config;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraGroup;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.HeraPermission;
import com.dfire.common.entity.vo.HeraGroupVo;
import com.dfire.common.entity.vo.HeraJobVo;
import com.dfire.common.enums.RunAuthType;
import com.dfire.common.exception.NoPermissionException;
import com.dfire.common.service.HeraGroupService;
import com.dfire.common.service.HeraJobService;
import com.dfire.common.service.HeraPermissionService;
import com.dfire.common.util.StringUtil;
import com.dfire.core.util.JwtUtils;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.HeraLog;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author xiaosuda
 * @date 2018/8/1
 */
@Aspect
@Component
public class HeraAspect {


    @Autowired
    @Qualifier("heraJobMemoryService")
    private HeraJobService heraJobService;

    @Autowired
    private HeraPermissionService heraPermissionService;

    @Autowired
    private HeraGroupService heraGroupService;

    @Pointcut("execution(* com.dfire.controller..*(..)) || @annotation(RunAuth)")
    private void auth() {

    }

    @Around("auth()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = getMethod(joinPoint);
        if (method != null) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            if (method.isAnnotationPresent(AdminCheck.class)) {
                checkAdmin(request);
            }
            if (method.isAnnotationPresent(RunAuth.class)) {
                checkRunAuth(method, request, joinPoint);
            }
        }
        Long start = System.currentTimeMillis();
        Object res;
        res = joinPoint.proceed();
        Long end = System.currentTimeMillis();
        if (start - end >= 10 * 1000L) {
            HeraLog.warn("方法名:{},参数:{},耗时:{}ms", joinPoint.getSignature().getName(), Arrays.asList(joinPoint.getArgs()), end - start);
        }
        return res;
    }

    /**
     * 解析方法上的@RunAuth注解参数
     *
     * @param method    包含RunAuth注解的方法
     * @param request   request
     * @param joinPoint 切面
     * @throws NoPermissionException    无权限
     * @throws IllegalArgumentException 参数错误
     */
    private void checkRunAuth(Method method, HttpServletRequest request, ProceedingJoinPoint joinPoint) throws NoPermissionException, IllegalArgumentException {
        if (!method.isAnnotationPresent(RunAuth.class)) {
            throw new IllegalArgumentException("方法" + method.getName() + "不含@RunAuth注解");
        }
        RunAuth runAuth = method.getAnnotation(RunAuth.class);
        Integer runId = null;
        RunAuthType runAuthType;
        if (runAuth.typeIndex() != -1) {
            runAuthType = (RunAuthType) getRunId(joinPoint, runAuth.typeIndex());
        } else {
            runAuthType = runAuth.authType();
        }
        // -1表示参数是个vo,并且在首位0
        if (runAuth.idIndex() == -1) {
            Object param = getRunId(joinPoint, 0);
            if (runAuthType == RunAuthType.JOB) {
                if (param instanceof HeraJobVo) {
                    runId = ((HeraJobVo) param).getId();
                } else if (param instanceof HeraJob) {
                    runId = ((HeraJob) param).getId();
                }
            } else {
                if (param instanceof HeraGroup) {
                    runId = ((HeraGroup) param).getId();
                } else if (param instanceof HeraGroupVo) {
                    runId = ((HeraGroupVo) param).getId();
                }
            }
        } else {
            Object param = getRunId(joinPoint, runAuth.idIndex());

            if (param == null) {
                throw new IllegalArgumentException("参数格式错误");
            }
            if (runAuthType == RunAuthType.GROUP && param instanceof String) {
                runId = StringUtil.getGroupId((String) param);
            } else {
                if (param instanceof Integer) {
                    runId = (Integer) param;
                } else {
                    runId = Integer.parseInt(String.valueOf(param));
                }
            }
        }
        if (runId == null) {
            throw new IllegalArgumentException("参数格式错误");
        }
        checkPermission(getOwner(request), runId, runAuthType);
    }

    private void checkPermission(String owner, Integer id, RunAuthType runAuthType) throws NoPermissionException {
        String errorMsg = "抱歉，您没有权限进行此操作";
        if (owner == null || id == null) {
            if (owner == null) {
                ErrorLog.warn("owner为null，无权限执行");
            } else {
                ErrorLog.warn("id为null,无权限执行");
            }
            throw new NoPermissionException(errorMsg);
        }
        if (isAdmin(owner)) {
            return;
        }
        if (runAuthType == null) {
            throw new IllegalArgumentException("RunAuthType 参数有误");
        }
        if (RunAuthType.JOB == runAuthType) {
            HeraJob job = heraJobService.findMemById(id);
            if (job != null && !job.getOwner().equals(owner)) {
                HeraPermission permission = heraPermissionService.findByCond(id, owner, runAuthType.getName());
                if (permission == null) {
                    ErrorLog.warn(owner + "无权限操作任务:" + id);
                    throw new NoPermissionException(errorMsg);
                }
            }
        } else if (RunAuthType.GROUP == runAuthType) {
            HeraGroup group = heraGroupService.findById(id);
            if (group != null && !owner.equals(group.getOwner())) {
                if (heraPermissionService.findByCond(id, owner, runAuthType.getName()) == null) {
                    ErrorLog.warn(owner + "无权限操作组:" + id);
                    throw new NoPermissionException(errorMsg);
                }
            }
        }
    }

    private Object getRunId(ProceedingJoinPoint joinPoint, int argIndex) {
        if (joinPoint.getArgs() == null || joinPoint.getArgs().length < argIndex) {
            throw new IllegalArgumentException(joinPoint.getSignature().toShortString() + ":@RunAuth参数下标越界");
        }
        return joinPoint.getArgs()[argIndex];
    }


    /**
     * 检测用户是否为超级用户
     *
     * @param request request
     */
    private void checkAdmin(HttpServletRequest request) throws NoPermissionException {
        if (!isAdmin(getOwner(request))) {
            throw new NoPermissionException("操作无权限,请使用超级用户执行该操作");
        }
    }

    private boolean isAdmin(String owner) {
        return owner.equals(HeraGlobalEnv.getAdmin());
    }


    private String getOwner(HttpServletRequest request) {
        return JwtUtils.getObjectFromToken(Constants.TOKEN_NAME, request, Constants.SESSION_USERNAME);
    }

    /**
     * 根据切面获得当前执行的方法
     *
     * @param jp 切面
     * @return 方法
     */
    private Method getMethod(JoinPoint jp) {
        Method proxyMethod = ((MethodSignature) jp.getSignature()).getMethod();
        try {
            return jp.getTarget().getClass().getMethod(proxyMethod.getName(), proxyMethod.getParameterTypes());
        } catch (NoSuchMethodException | SecurityException ignored) {
        }
        return null;
    }
}
