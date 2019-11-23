package com.dfire.common.util;

import com.dfire.common.constants.TimeFormatConstant;
import com.dfire.logs.ErrorLog;
import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:49 2018/3/22
 * @desc Hera日期工具类, 用于界面配置项的动态替换
 */
public class HeraDateTool {

    private Calendar calendar = Calendar.getInstance();

    public HeraDateTool() {
        this(new Date());
    }

    public HeraDateTool(Date date) {
        this.calendar.setTime(date);
    }

    public HeraDateTool addDay(int amount) {
        calendar.add(Calendar.DAY_OF_YEAR, amount);
        return this;
    }

    public HeraDateTool add(int field, int amount) {
        calendar.add(field, amount);
        return this;
    }

    public HeraDateTool set(int field, int amount) {
        calendar.set(field, amount);
        return this;
    }

    public long getTime() {
        return calendar.getTime().getTime() / 1000;
    }

    public long getMillis() {
        return calendar.getTime().getTime();
    }


    public long getNowMillis() {
        return System.currentTimeMillis();
    }


    public int get(int x) {
        return calendar.get(x);
    }

    public String format(String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(calendar.getTime());
    }


    /**
     * @param dateStr   需要转换的字符串
     * @param formatStr 需要格式的目标字符串  举例 yyyy-MM-dd
     * @return outFormatStr 需要格式的输出字符串  举例 yyyy-MM-dd
     * @throws ParseException 转换异常
     * @desc 日期格式字符串互相转换
     */
    public static String StringToDateStr(String dateStr, String formatStr, String outFormatStr) {
        DateFormat sdf = new SimpleDateFormat(formatStr);
        SimpleDateFormat outDateFormat = new SimpleDateFormat(outFormatStr);
        Date date;
        String outDateStr = "";
        try {
            date = sdf.parse(dateStr);
            outDateStr = outDateFormat.format(date);
        } catch (ParseException e) {
            ErrorLog.error("解析日期异常", e);
        }
        return outDateStr;
    }

    /**
     * @param dateStr   需要转换的字符串
     * @param formatStr 需要格式的目标字符串  举例 yyyy-MM-dd
     * @return Date 返回转换后的时间
     * @throws ParseException 转换异常
     * @desc 字符串转换到时间格式
     */
    public static Date StringToDate(String dateStr, String formatStr) {
        DateFormat sdf = new SimpleDateFormat(formatStr);
        Date date = null;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            ErrorLog.error("转换日期格式异常", e);
        }
        return date;
    }

    public static String getToday() {
        return new DateTime().toString(TimeFormatConstant.YYYY_MM_DD);
    }


}
