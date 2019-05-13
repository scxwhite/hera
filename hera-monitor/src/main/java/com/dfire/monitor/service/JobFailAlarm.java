package com.dfire.monitor.service;

import com.dfire.event.HeraJobFailedEvent;

/**
 *
 * @author xiaosuda
 * @date 2019/2/25
 */
public interface JobFailAlarm {


    /**
     *  任务失败告警接口，自己可以自定义实现 默认:com.dfire.monitor.service.impl.EmailJobFailAlarm
     *  一定要把实现类使用spring管理
     * @param failedEvent  HeraJobFailedEvent
     */
    void alarm(HeraJobFailedEvent failedEvent);
    
}
