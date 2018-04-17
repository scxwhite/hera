package com.dfire.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 *
 * @author xiaosuda
 * @date 2018/4/16
 */
@Data
@Component
@ConfigurationProperties("hera")
public class HeraGlobalEnvironment {

    private String excludeFile;

    private String defaultWorkerGroup;

    private String preemptionMasterGroup;

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
