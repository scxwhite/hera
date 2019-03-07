package com.dfire.monitor.config;

import com.dfire.monitor.service.JobFailAlarm;
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        setAlarms(applicationContext);
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
                throw new UnsupportedOperationException("不支持的告警类型:" + bean.getClass().getName() + ",@Alarm注解只能放在" + JobFailAlarm.class + "的实现类上");
            }
            ServiceLoader.alarms.add((JobFailAlarm) bean);
        }
    }

    public static List<JobFailAlarm> getAlarms() {
        return alarms;
    }
}
