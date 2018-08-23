package com.dfire.common.util;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by xiaosuda on 2018/6/26.
 */
public class StringUtilTest {

    @Test
    public void convertMapToString() {
        Map<String, String> map  = new HashMap<>();
        map.put("1223", "456");
        map.put("1233", "456");
        map.put("1234", "456");
        System.out.println(StringUtil.convertMapToString(map));
    }

    @Test
    public void convertStringToMap() {
        String str = "{\"roll.back.times\":\"3\",\"columns\":\"\"id,name,package_url,url,code_url,qr_code_memo,icon_path,app_name,publish_time,version_code,version,size,guide,attention,blong_to,release_sataus,app_code,is_valid,create_time,op_time,update_property,update_version,update_guide,hash_url,force_update_version,channel\"\",\"roll.back.wait.time\":\"10\",\"run.priority.level\":\"3\",\"table\":\"as_app\",\"zeus.dependency.cycle\":\"sameday\"}";
        Map<String, String> map = StringUtil.convertStringToMap("{\"run.priority.level\":\"1\",\"roll.back.wait.time\":\"1\",\"roll.back.times\":\"0\"}");


    }


    @Test
    public void streamTest() {
        Stream.of("one", "two", "three", "four")
                .filter(e -> e.length() > 3)
                .peek(e -> System.out.println("Filtered value: " + e))
                .map(String::toUpperCase)
                .peek(e -> System.out.println("Mapped value: " + e))
                .collect(Collectors.toList());
    }

    @Test
    public void md5Test() {
        System.out.println(StringUtil.EncoderByMd5("baseimport"));
    }
}