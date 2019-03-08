package com.dfire.core.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * @author xiaosuda
 * @date 2018/6/13
 */
public class NetUtils {

    private static final String LOCAL_HOST = "127.0.0.1";

    /**
     * 获得局域网IP
     *
     * @return
     */
    public static String getLocalAddress() {
        String secondAddress;
        try {
            secondAddress = InetAddress.getLocalHost().getHostAddress();
            if (!LOCAL_HOST.equals(secondAddress)) {
                return secondAddress;
            }
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface anInterface = en.nextElement();
                for (Enumeration<InetAddress> addresses = anInterface.getInetAddresses(); addresses.hasMoreElements(); ) {
                    InetAddress inetAddress = addresses.nextElement();
                    // 排除loopback类型地址
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress.isSiteLocalAddress()) {
                            return inetAddress.getHostAddress();
                        } else if (secondAddress == null) {
                            secondAddress = inetAddress.getHostAddress();
                        }
                    }
                }
            }
            return secondAddress;
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new NullPointerException("----------------- Not FOUND REAL IP -------------------");
    }

}
