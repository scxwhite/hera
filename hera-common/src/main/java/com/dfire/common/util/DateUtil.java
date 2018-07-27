package com.dfire.common.util;

import com.dfire.common.kv.Tuple;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
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

    public static int getCurrentHour(Calendar now) {
        return now.get(Calendar.HOUR_OF_DAY);
    }

    public static int getCurrentMinute(Calendar now) {
        return now.get(Calendar.MINUTE);
    }


    public static String getNowStringForAction() {
        SimpleDateFormat dfDateTime = new SimpleDateFormat("yyyyMMddHHmmss0000");
        return dfDateTime.format(new Date());

    }

    public static String getTodayStringForAction() {
        SimpleDateFormat dfDateTime = new SimpleDateFormat("yyyyMMdd0000000000");
        return dfDateTime.format(new Date());

    }

    public static Tuple<String, Date> getNextDayString() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, +1);
        SimpleDateFormat dfNextDate = new SimpleDateFormat("yyyyMMdd0000000000");
        String next = dfNextDate.format(cal.getTime());
        Tuple<String, Date> tuple = new Tuple<>();
        tuple.setSource(next);
        tuple.setTarget(cal.getTime());
        return tuple;

    }

    public static long string2Timestamp(String dateString, String timezone)
            throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if ((timezone != null) && (!timezone.equals(""))) {
            df.setTimeZone(TimeZone.getTimeZone(timezone));
        }
        Date date1 = df.parse(dateString);
        long temp = date1.getTime();
        return temp;
    }

    public static Date longToDate(Long time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(default_formatter);
        String tmp = simpleDateFormat.format(time);
        Date result = null;
        try {
            result = simpleDateFormat.parse(tmp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean isNow(String actionId) {
        return DateUtil.getNowStringForAction().compareTo(actionId) <= 0;
    }

    public static boolean isToday(String actionId) {
        return DateUtil.getTodayStringForAction().compareTo(actionId) <= 0;
    }
}
