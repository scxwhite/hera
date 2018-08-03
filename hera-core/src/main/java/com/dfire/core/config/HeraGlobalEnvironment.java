package com.dfire.core.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author xiaosuda
 * @date 2018/4/16
 */
@Component
public class HeraGlobalEnvironment {

    @Getter
    public static String excludeFile;

    public static int defaultWorkerGroup;

    public static String preemptionMasterGroup;

    @Getter
    private static String env;

    @Getter
    private static Float maxMemRate = 0.8F;
    @Getter
    private static Float maxCpuLoadPerCore = 3F;
    @Getter
    private static Integer scanRate = 3000;
    @Getter
    private static Integer scanExceptionRate = 3000;
    @Getter
    private static Integer connectPort;
    @Getter
    private static String downloadDir;
    @Getter
    private static Integer maxParallelNum;
    @Getter
    private static Integer heartBeat = 5;
    @Getter
    private static String admin;


    @Value("${hera.dos2unix-exclude-file}")
    public void setExcludeFile(String excludeFile) {
        HeraGlobalEnvironment.excludeFile = excludeFile;
    }

    @Value("${hera.default-worker-group-id}")
    public void setDefaultWorkerGroup(int defaultWorkerGroup) {
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

    @Value("${hera.connect.port}")
    public void setConnectPort(Integer port) {
        HeraGlobalEnvironment.connectPort = port;
    }

    @Value("${hera.download-dir}")
    public void setDownloadDir(String dir) {
        HeraGlobalEnvironment.downloadDir = dir;
    }

    @Value("${hera.max-parallel-num}")
    public void setMaxParallelNum(Integer maxParallelNum) {
        HeraGlobalEnvironment.maxParallelNum = maxParallelNum;
    }

    @Value("${hera.admin}")
    public void setAdmin(String admin) {
        HeraGlobalEnvironment.admin = admin;
    }

    /**
     * 判断是否是linux 环境，有些命令不一样
     */
    private static boolean linuxSystem = true;

    static {
        String os = System.getProperties().getProperty("os.name");
        if (os != null) {
            if (os.toLowerCase().startsWith("win") || os.toLowerCase().startsWith("mac")) {
                linuxSystem = false;
            }
        }
    }

    public static boolean isLinuxSystem() {
        return linuxSystem;
    }
}
