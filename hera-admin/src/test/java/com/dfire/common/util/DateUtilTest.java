package com.dfire.common.util;

import com.dfire.common.kv.Tuple;
import org.junit.Test;

import java.util.Date;

/**
 * Created by xiaosuda on 2018/7/17.
 */
public class DateUtilTest {

    @Test
    public void getNextDayString() {
        Tuple<String, Date> dayString = DateUtil.getNextDayString();

        System.out.println(dayString.getSource());
        System.out.println(dayString.getTarget());
    }
}