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

    private static List<JobFailAlarm> alarms;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, JobFailAlarm> alarmMap = applicationContext.getBeansOfType(JobFailAlarm.class);
        if (alarmMap != null && alarmMap.size() > 0) {
            alarms = new ArrayList<>(alarmMap.size());
            alarms.addAll(alarmMap.values());
        }
    }

    public static List<JobFailAlarm> getAlarms() {
        return alarms;
    }
}
