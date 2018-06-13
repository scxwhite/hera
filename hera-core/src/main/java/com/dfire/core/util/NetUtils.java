package com.dfire.core.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 *
 * @author xiaosuda
 * @date 2018/6/13
 */
public class NetUtils {

    /**
     * 获得局域网IP
     * @return
     */
    public static String getLocalAddress() {
        String secondAddress = null;
        try {
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
            if (secondAddress != null) {
                return secondAddress;
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
