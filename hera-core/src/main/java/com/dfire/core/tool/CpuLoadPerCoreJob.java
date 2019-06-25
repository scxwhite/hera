package com.dfire.core.tool;

import com.dfire.config.HeraGlobalEnv;
import com.dfire.core.netty.worker.WorkContext;
import com.dfire.logs.ErrorLog;

import java.io.IOException;

/**
 * @author xiaosuda
 * @date 2018/8/6
 */
public class CpuLoadPerCoreJob extends RunShell {

    private float loadPerCore = 1f;
    public CpuLoadPerCoreJob() {
        super("uptime | awk '{print $(NF-2)}'");
    }

    @Override
    public Integer run() {
        if (!HeraGlobalEnv.isLinuxSystem()) {
            return -1;
        }
        Integer exitCode = super.run();
        if (exitCode == 0) {
            try {
                String result = super.getResult();
                loadPerCore = getCpuLoad(result) / WorkContext.cpuCoreNum;
            } catch (IOException e) {
                ErrorLog.error("获取负载信息失败", e);
            }
        }
        return exitCode;
    }


    private Float getCpuLoad(String result) {
        return Float.parseFloat(result.replace(",", ""));
    }

    public float getLoadPerCore() {
        return loadPerCore;
    }

}
