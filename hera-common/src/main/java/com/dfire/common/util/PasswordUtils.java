package com.dfire.common.util;


import com.dfire.logs.ErrorLog;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * desc:
 *
 * @author scx
 * @create 2019/04/19
 */
public class PasswordUtils {


    private final static String SALT = "www.hera.cn/19960811";

    public static String aesEncryption(String content) {
        try {
            SecretKey key = buildKey();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return new BASE64Encoder().encode(cipher.doFinal(content.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            ErrorLog.error("加密失败", e);
        }
        return null;
    }


    private static SecretKey buildKey() {
        //1.构造密钥生成器，指定为AES算法,不区分大小写
        KeyGenerator keygen;
        try {
            keygen = KeyGenerator.getInstance("AES");
            //2.根据ecnodeRules规则初始化密钥生成器
            //生成一个128位的随机源,根据传入的字节数组
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(SALT.getBytes());
            keygen.init(128, random);
            //3.产生原始对称密钥
            SecretKey originalKey = keygen.generateKey();
            //4.获得原始对称密钥的字节数组
            byte[] raw = originalKey.getEncoded();
            //5.根据字节数组生成AES密钥
            return new SecretKeySpec(raw, "AES");
        } catch (NoSuchAlgorithmException e) {
            ErrorLog.error("算法不存在", e);
        }
        return null;
    }

    public static String aesDecrypt(String content) {
        try {
            SecretKey key = buildKey();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(new BASE64Decoder().decodeBuffer(content)), StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException | IllegalBlockSizeException | BadPaddingException e) {
            ErrorLog.error("解密失败", e);
        }
        return null;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        String content = "964;hadoop";
        String secret = aesEncryption(content);
        System.out.println(secret);


        // U%2BrVkiAacaEQLm09g%2BmoyQ%3D%3D
        // System.out.println(aesDecrypt("HTj//o8xi7V4rbatMpyjGw=="));
    }
}