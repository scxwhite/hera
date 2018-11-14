package com.dfire.common.vo;

import lombok.Data;

import java.util.List;

/**
 *
 * @author xiaosuda
 * @date 2018/11/14
 */
@Data
public class WorkInfoVo {

    /**
     * 机器信息
     */
    private List<MachineInfoVo> machineInfo;
    /**
     * 系统信息
     */
    private OSInfoVo osInfo;
    /**
     * 进程监控
     */
    private List<ProcessMonitorVo> processMonitor;

}
