package com.dfire.core.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by xiaosuda on 2018/7/13.
 */
public class CronParseTest {

    @Test
    public void parser() {
        List<String> x = new ArrayList<>();
        CronParse.Parser("0 0 0/1 * * ?", "2018-07-18", x);
        x.forEach(System.out::println);
    }
}