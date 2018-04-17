package com.dfire.core.config;

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
