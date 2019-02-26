package com.dfire.monitor.service;

/**
 *
 * @author xiaosuda
 * @date 2019/2/25
 */
public interface JobFailAlarm {


    /**
     *  任务失败告警接口，自己可以自定义实现 默认:com.dfire.monitor.service.impl.EmailJobFailAlarm
     * @param actionId  hera_action.id字段
     */
    void alarm(String actionId);
}
