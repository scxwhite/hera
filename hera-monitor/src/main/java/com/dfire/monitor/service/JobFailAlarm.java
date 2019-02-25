package com.dfire.monitor.service;

/**
 *
 * @author xiaosuda
 * @date 2019/2/25
 */
public interface JobFailAlarm {


    /**
     *  任务失败告警接口，自己可以自定义实现
     *  实现后在  META-INF/services/com.dfire.monitor.service.JobFailAlarm 下增加该实现类的全权限定类名即可
     * @param actionId  hera_action.id字段
     */
    void alarm(String actionId);
}
