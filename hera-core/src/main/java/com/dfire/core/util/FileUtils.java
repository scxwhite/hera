package com.dfire.core.util;

import com.dfire.logs.HeraLog;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * desc:
 *
 * @author scx
 * @create 2020/09/22
 */
public class FileUtils {

    private static Properties properties;

    static {
        InputStream stream = NetUtils.class.getClassLoader().getResourceAsStream("config/hera.properties");

        properties = new Properties();
        try {
            properties.load(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            HeraLog.info(String.format("load hera properties: %s=%s", entry.getKey(), entry.getValue()));
        }
    }


    public static String getKey(String key) {
        return properties.getProperty(key);
    }


}
