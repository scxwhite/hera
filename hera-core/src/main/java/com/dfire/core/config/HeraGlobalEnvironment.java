package com.dfire.core.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author xiaosuda
 * @date 2018/4/16
 */
@Component
public class HeraGlobalEnvironment {

    public static String excludeFile;

    public static String defaultWorkerGroup;

    public static String preemptionMasterGroup;

    public static String env;

    @Getter
    private static Float maxMemRate = Float.valueOf(0.8F);
    @Getter
    private static Float maxCpuLoadPerCore = Float.valueOf(3F);
    @Getter
    private static Integer scanRate = Integer.valueOf(3000);
    @Getter
    private static Integer scanExceptionRate = Integer.valueOf(3000);


    @Value("${hera.exclude-file}")
    public void setExcludeFile(String excludeFile) {
        HeraGlobalEnvironment.excludeFile = excludeFile;
    }
    @Value("${hera.default-worker-group-id}")
    public void setDefaultWorkerGroup(String defaultWorkerGroup) {
        HeraGlobalEnvironment.defaultWorkerGroup = defaultWorkerGroup;
    }
    @Value("${hera.preemption-master-group-id}")
    public void setPreemptionMasterGroup(String preemptionMasterGroup) {
        HeraGlobalEnvironment.preemptionMasterGroup = preemptionMasterGroup;
    }
    @Value("${hera.env}")
    public void setEnv(String env) {
        HeraGlobalEnvironment.env = env;
    }

    @Value("${hera.scanExceptionRate}")
    public void setScanExceptionRate(Integer scanExceptionRate) {
        HeraGlobalEnvironment.scanExceptionRate = scanExceptionRate;
    }
    @Value("${hera.maxMemRate}")
    public void setMaxMemRate(Float maxMemRate) {
        HeraGlobalEnvironment.maxMemRate = maxMemRate;
    }
    @Value("${hera.cpuLoadPerCore}")
    public void setCpuLoadPerCore(Float cpuLoadPerCore) {
        HeraGlobalEnvironment.maxCpuLoadPerCore = cpuLoadPerCore;
    }
    @Value("${hera.scanRate}")
    public void setScanRate(Integer scanRate) {
        HeraGlobalEnvironment.scanExceptionRate = scanRate;
    }

    /**
     * 判断是否是linux 环境，有些命令不一样
     */
    private static boolean linuxSystem = true;

    static {
        String os = System.getProperties().getProperty("os.name");
        if (os != null) {
            if(os.toLowerCase().startsWith("win") || os.toLowerCase().startsWith("mac")){
                linuxSystem = false;
            }
        }
    }

    public static boolean isLinuxSystem() {
        return linuxSystem;
    }
}
