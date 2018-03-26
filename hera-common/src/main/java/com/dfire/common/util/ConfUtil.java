package com.dfire.common.util;

import org.apache.hadoop.conf.Configuration;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 15:41 2018/3/24
 * @desc
 */
public class ConfUtil {

    public static String getHadoopHome() {
        return "";
    }

    public static String getHiveHome() {
        return "";
    }

    public static String getHadoopConfDir() {
        return "";
    }

    public static String getHiveConfDir() {
        return "";
    }

    public static Configuration getDefaultHiveSie() {
        return null;
    }

    public static Configuration getDefaultCoreSite() {
        return null;
    }

    public static Configuration getDefaultHdfsSite() {
        return null;
    }

    public static Configuration getDefaultMapredSite(){
        return null;
    }

    public static Configuration getDefaultCoreAndHdfsSite(){
        return null;
    }

    public static Configuration getDefaultYarnSite(){
        return null;
    }
}
