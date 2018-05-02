package com.dfire.common.util;


import com.alibaba.fastjson.JSONObject;
import com.dfire.common.processor.DownPorcessor;
import com.dfire.common.processor.Processor;
import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 18:06 2018/1/7
 * @desc 字符串处理工具类
 */
@Slf4j
public class StringUtil {

    /**
     * @param sourceStr
     * @return
     * @desc 登陆密码md5加密
     */
    public static String EncoderByMd5(String sourceStr) {
        String result = "";
        int i;
        StringBuffer buf = new StringBuffer("");
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5 错误");
        }
        md.update(sourceStr.getBytes());
        byte b[] = md.digest();
        for (int offset = 0; offset < b.length; offset++) {
            i = b[offset];
            if (i < 0)
                i += 256;
            if (i < 16)
                buf.append("0");
            buf.append(Integer.toHexString(i));
        }
        return buf.toString();
    }

    public static Map<String, String> convertStringToMap(String config) {
        Map<String, String> map = new HashMap();
        JSONObject jsonObject = JSONObject.parseObject(config);
        for (Object key : jsonObject.keySet()) {
            map.put(key.toString(), jsonObject.getString(key.toString()));
        }
        return map;
    }

    public static String convertMapToString(Map<String, String> config) {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, String> entry : config.entrySet()) {
            jsonObject.put(entry.getKey().toLowerCase(), entry.getValue().toLowerCase());
        }
        return jsonObject.toString();
    }

    public Processor convertProcessorToList(String processor) {
        Processor result = null;
        JSONObject jsonObject = JSONObject.parseObject(processor);
        String id = jsonObject.getString("id");
        if (id.equals("download")) {
            result = new DownPorcessor();
        }
        return result;
    }
}
