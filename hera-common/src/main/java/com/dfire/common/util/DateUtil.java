package com.dfire.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author xiaosuda
 * @date 2018/4/20
 */
public class DateUtil {

    private static final String default_formatter = "yyyy-MM-dd HH:mm:ss";



    public static String getTodayString() {
        SimpleDateFormat sdf = new SimpleDateFormat(default_formatter);
        return sdf.format(new Date());
    }

    public static String getFormatterDate(String formatter, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(formatter);
        return sdf.format(date);
    }

    public static String getDefaultFormatterDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(default_formatter);
        return sdf.format(date);
    }
}
