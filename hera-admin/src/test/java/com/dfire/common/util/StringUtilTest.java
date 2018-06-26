package com.dfire.common.util;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

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
}