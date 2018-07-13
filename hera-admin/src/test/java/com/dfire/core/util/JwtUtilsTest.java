package com.dfire.core.util;

import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;

/**
 * Created by xiaosuda on 2018/7/13.
 */
public class JwtUtilsTest {

    @Test
    public void createToken() {

    }

    @Test
    public void verifyToken() {
        String token = JwtUtils.createToken("小苏打");
        assertTrue(JwtUtils.verifyToken(token));
    }

    @Test
    public void getObjectFromToken() {
        String token = JwtUtils.createToken("小苏打");
        assertEquals(JwtUtils.getObjectFromToken(token, "username"), "小苏打");
    }
}