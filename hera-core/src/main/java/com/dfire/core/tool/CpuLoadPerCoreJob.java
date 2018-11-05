package com.dfire.core.tool;

import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.netty.worker.WorkContext;

import java.io.IOException;

/**
 * @author xiaosuda
 * @date 2018/8/6
 */
public class CpuLoadPerCoreJob {

    private float loadPerCore = 100f;

    private final String loadCommand = "uptime";
    private final String keys = "load average:";
    private final Integer keysLen = keys.length();

    public void run() {
        if (!HeraGlobalEnvironment.isLinuxSystem()) {
            return ;
        }
        RunShell runShell = new RunShell(loadCommand);

        Integer exitCode = runShell.run();
        if (exitCode == 0) {
            try {
                String result = runShell.getResult();
                loadPerCore = getCpuLoad(result) / WorkContext.cpuCoreNum;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
