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
    private static Float maxMemRate;
    @Getter
    private static Float maxCpuLoadPerCore;
    @Getter
    private static Float perTaskUseMem;
    @Getter
    private static Float systemMemUsed;
    @Getter
    private static Integer scanRate;
    @Getter
    private static Integer connectPort;
    @Getter
    private static String downloadDir;
    @Getter
    private static Integer maxParallelNum;
    @Getter
    private static Integer heartBeat;
    @Getter
    private static String admin;
    @Getter
    private static Integer taskTimeout;
    @Getter
    private static String sparkAddress;
    @Getter
    private static String sparkUser;
    @Getter
    private static String sparkPassword;
    @Getter
    private static String sparkConfig;

    @Value("${hera.excludeFile")
    public void setExcludeFile(String excludeFile) {
        HeraGlobalEnvironment.excludeFile = excludeFile;
    }

    @Value("${hera.defaultWorkerGroup}")
    public void setDefaultWorkerGroup(int defaultWorkerGroup) {
        HeraGlobalEnvironment.defaultWorkerGroup = defaultWorkerGroup;
    }

    @Value("${hera.preemptionMasterGroup}")
    public void setPreemptionMasterGroup(String preemptionMasterGroup) {
        HeraGlobalEnvironment.preemptionMasterGroup = preemptionMasterGroup;
    }

    @Value("${hera.env}")
    public void setEnv(String env) {
        HeraGlobalEnvironment.env = env;
    }

    @Value("${hera.maxMemRate}")
    public void setMaxMemRate(Float maxMemRate) {
        HeraGlobalEnvironment.maxMemRate = maxMemRate;
    }

    @Value("${hera.maxCpuLoadPerCore}")
    public void setCpuLoadPerCore(Float maxCpuLoadPerCore) {
        HeraGlobalEnvironment.maxCpuLoadPerCore = maxCpuLoadPerCore;
    }

    @Value("${hera.scanRate}")
    public void setScanRate(Integer scanRate) {
        HeraGlobalEnvironment.scanRate = scanRate;
    }

    @Value("${hera.connectPort}")
    public void setConnectPort(Integer connectPort) {
        HeraGlobalEnvironment.connectPort = connectPort;
    }

    @Value("${hera.downloadDir}")
    public void setDownloadDir(String downloadDir) {
        HeraGlobalEnvironment.downloadDir = downloadDir;
    }

    @Value("${hera.maxParallelNum}")
    public void setMaxParallelNum(Integer maxParallelNum) {
        HeraGlobalEnvironment.maxParallelNum = maxParallelNum;
    }
    @Value("${hera.admin}")
    public void setAdmin(String admin) {
        HeraGlobalEnvironment.admin = admin;
    }
    @Value("${hera.taskTimeout}")
    public void setTaskTimeout(Integer taskTimeout) {
        HeraGlobalEnvironment.taskTimeout = taskTimeout;
    }
    @Value("${hera.heartBeat}")
    public void setHeartBeat(Integer heartBeat) {
        HeraGlobalEnvironment.heartBeat = heartBeat;
    }
    @Value("${hera.perTaskUseMem}")
    public void setPerTaskUseMem(Float perTaskUseMem) {
        HeraGlobalEnvironment.perTaskUseMem = perTaskUseMem;
    }
    @Value("${hera.systemMemUsed}")
    public void setSystemMemUsed(Float systemMemUsed) {
        HeraGlobalEnvironment.systemMemUsed = systemMemUsed;
    }
    @Value("${hera.maxCpuLoadPerCore}")
    public void setMaxCpuLoadPerCore(Float maxCpuLoadPerCore) {
        HeraGlobalEnvironment.maxCpuLoadPerCore = maxCpuLoadPerCore;
    }
    @Value("${hera.spark.address")
    public void setSparkAddress(String sparkAddress) {
        HeraGlobalEnvironment.sparkAddress = sparkAddress;
    }
    @Value("${hera.spark.username")
    public void setSparkUser(String sparkUser) {
        HeraGlobalEnvironment.sparkUser = sparkUser;
    }
    @Value("${hera.spark.password")
    public void setSparkPassword(String sparkPassword) {
        HeraGlobalEnvironment.sparkPassword = sparkPassword;
    }
    @Value("${hera.spark.config")
    public void setSparkConfig(String sparkConfig) {
        HeraGlobalEnvironment.sparkConfig = sparkConfig;
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
