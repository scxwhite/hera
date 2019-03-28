package com.dfire.api;

import java.util.Map;
import java.util.Properties;

/**
 * desc:
 * 变量
 *
 * @author scx
 * @create 2019/03/21
 */
public class SystemEnv {
    public static void main(String[] args) {

        Properties properties = System.getProperties();
        System.out.println("系统变量是:");
        for (Map.Entry<Object, Object> objectObjectEntry : properties.entrySet()) {
            System.out.println(objectObjectEntry.getKey() + " " + objectObjectEntry.getValue());
        }


        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("用户变量为:");
        Map<String, String> getenv = System.getenv();
        for (Map.Entry<String, String> entry : getenv.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
    }
}