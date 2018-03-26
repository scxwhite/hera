package com.dfire.common.util;


import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
}
