package com.dfire.core.tool;

import com.dfire.config.HeraGlobalEnvironment;
import com.dfire.core.netty.worker.WorkContext;

import java.io.IOException;

/**
 * @author xiaosuda
 * @date 2018/8/6
 */
public class CpuLoadPerCoreJob extends RunShell {

    private float loadPerCore = 1f;
    private final String keys = "load average:";
    private final Integer keysLen = keys.length();

    public CpuLoadPerCoreJob() {
        super("uptime");
    }

    @Override
    public Integer run() {
        if (!HeraGlobalEnvironment.isLinuxSystem()) {
            return -1;
        }
        Integer exitCode = super.run();
        if (exitCode == 0) {
            try {
                String result = super.getResult();
                loadPerCore = getCpuLoad(result) / WorkContext.cpuCoreNum;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return exitCode;
    }


    private Float getCpuLoad(String result) {
        String loadStr = result.substring(result.indexOf(keys) + keysLen);
        loadStr = loadStr.replace(",", " ").trim();
        String[] split = loadStr.split(" ");
        return Float.parseFloat(split[0]);
    }

    public float getLoadPerCore() {
        return loadPerCore;
    }

}
