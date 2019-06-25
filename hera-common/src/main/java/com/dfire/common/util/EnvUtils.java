package com.dfire.common.util;

import com.dfire.common.constants.Constants;
import com.dfire.config.HeraGlobalEnv;

/**
 * desc:
 *
 * @author scx
 * @create 2019/05/29
 */
public class EnvUtils {


    private static String env = HeraGlobalEnv.getEnv() == null ? "pre" : HeraGlobalEnv.getEnv();
    private static String area = HeraGlobalEnv.getArea();

    public static boolean isDaily() {
        return env.equals(Constants.DAILY_ENV);
    }

    public static boolean isPre() {
        return env.equals(Constants.PRE_ENV);
    }


    public static boolean isPro() {
        return env.equals(Constants.PUB_ENV);
    }


    public static boolean isEurope() {
        return area.equals(Constants.AREA_EUROPE);
    }

    public static boolean isUs() {
        return area.equals(Constants.AREA_US);
    }

    public static boolean isIndia() {
        return area.equals(Constants.AREA_INDIA);
    }


}