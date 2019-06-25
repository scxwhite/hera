package com.dfire.common.config;

import com.dfire.common.exception.UnsupportedTypeException;
import com.dfire.common.service.JobFailAlarm;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author xiaosuda
 * @date 2019/2/26
 */
@Component
public class ServiceLoader implements ApplicationContextAware {

    private static List<JobFailAlarm> alarms = new ArrayList<>();

    private static List<ExecuteFilter> filters = new ArrayList<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        setAlarms(applicationContext);
        setFilters(applicationContext);
    }

    /**
     * 通过@Alarms 注解 找到所有需要告警的实现类
     *
     * @param applicationContext applicationContext
     */
    private void setAlarms(ApplicationContext applicationContext) {
        Map<String, Object> alarmBeans = applicationContext.getBeansWithAnnotation(Alarm.class);
        for (Object bean : alarmBeans.values()) {
            if (!(bean instanceof JobFailAlarm)) {
                throw new UnsupportedTypeException("不支持的告警类型:" + bean.getClass().getName() + ",@Alarm注解只能放在" + JobFailAlarm.class + "的实现类上");
            }
            ServiceLoader.alarms.add((JobFailAlarm) bean);
        }
    }

    /**
     * 通过@Filter 注解 找到所有需要告警的实现类
     *
     * @param applicationContext applicationContext
     */
    private void setFilters(ApplicationContext applicationContext) {
        Map<String, Object> alarmBeans = applicationContext.getBeansWithAnnotation(Filter.class);
        for (Object bean : alarmBeans.values()) {
            if (!(bean instanceof ExecuteFilter)) {
                throw new UnsupportedTypeException("不支持的拦截器类型:" + bean.getClass().getName() + ",@Filter注解只能放在" + ExecuteFilter.class + "的实现类上");
            }
            ServiceLoader.filters.add((ExecuteFilter) bean);
        }
    }


    public static List<JobFailAlarm> getAlarms() {
        return alarms;
    }

    public static List<ExecuteFilter> getFilters() {
        return filters;
    }
}
