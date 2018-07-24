package com.dfire.common.util;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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
        Map<String, String> map = StringUtil.convertStringToMap(str);


    }

}