package com.dfire.core.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:17 2018/1/12
 * @desc 心跳中传递的机器信息
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HeartBeatInfo {

    public Float memRate;

    public List<String> running;

    public List<String> manualRunning;

    public List<String> debugRunning;

    public Date timestamp;

    public String host;

    /**
     * cpu load per core等于最近1分钟系统的平均cpu负载÷cpu核心数量
     *
     */
    public Float cpuLoadPerCore;

    /**
     * 每个机器的总内存数
     *
     */
    public Float memTotal;
}
