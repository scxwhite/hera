package com.dfire.common.util;

import com.dfire.common.constants.Constants;
import com.dfire.common.kv.Tuple;
import com.dfire.logs.ErrorLog;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author xiaosuda
 * @date 2018/4/20
 */
public class ActionUtil {

    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 精确到分钟的时间
     */
    public static final String ACTION_MIN = "yyyyMMddHHmm";
    public static final String MON_MIN = "MM-dd HH:mm";
    /**
     * ACTION 表达式
     */
    public static final String ACTION_CRON = "0 m H d M";

    /**
     * 当前时刻生成版本格式
     */
    public static final String ACTION_VERSION_CURR = "yyyyMMddHHmm000000";
    /**
     * 当前小时版本格式
     */
    public static final String ACTION_VERSION_HOUR = "yyyyMMddHHmm000000";

    /**
     * 初始化今天凌晨的版本
     */
    public static final String ACTION_VERSION_INIT = "yyyyMMdd0000000000";
    /**
     * 版本Action 前缀
     */
    public static final String ACTION_VERSION_PREFIX = "yyyy-MM-dd";

    /**
     * 当前时间戳
     */
    public static final String YYYY_MM_DD = "yyyy-MM-dd";


    /**
     * 生成今天 Action版本的最晚时间
     */
    public static final int ACTION_CREATE_MAX_HOUR = 23;


    /**
     * 生成今天 Action版本的最早时间
     */
    public static final int ACTION_CREATE_MIN_HOUR = 6;


    public static String getTodayString() {
        return new DateTime().toString(DEFAULT_FORMAT);
    }

    public static String getFormatterDate(String formatter, Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(formatter);
        return sdf.format(date);
    }

    public static String getDefaultFormatterDate(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_FORMAT);
        return sdf.format(date);
    }

    public static String getDefaultFormatterDate(Long millis) {
        return getDefaultFormatterDate(new Date(millis));
    }


    public static String getCurrActionVersion() {
        return new DateTime().toString(ACTION_VERSION_CURR);
    }

    public static String getCurrHourVersion() {
        return new DateTime().toString(ACTION_VERSION_HOUR);
    }

    public static Long getLongCurrActionVersion() {
        return Long.parseLong(new DateTime().toString(ACTION_VERSION_CURR));
    }

    public static Long getActionByDateStr(String date) {
        return Long.parseLong(new DateTime(getDateByDateStr(date, DEFAULT_FORMAT)).toString(ACTION_VERSION_CURR));
    }

    public static String getTodayAction() {
        return new DateTime().toString(ACTION_VERSION_INIT);

    }

    public static String getActionVersionByDate(Date date) {
        return new DateTime(date).toString(ACTION_VERSION_INIT);

    }

    public static String getActionVersionPrefix(Date nowTime) {
        return new DateTime(nowTime).toString(ACTION_VERSION_PREFIX);
    }

    public static String getCurrDate() {
        return new DateTime().toString(YYYY_MM_DD);
    }

    public static Tuple<String, Date> getNextDayString() {
        DateTime dateTime = new DateTime().plusDays(1);
        Tuple<String, Date> tuple = new Tuple<>();
        tuple.setSource(dateTime.toString(ACTION_VERSION_INIT));
        tuple.setTarget(dateTime.toDate());
        return tuple;

    }

    public static long string2Timestamp(String dateString, String timezone)
            throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat(DEFAULT_FORMAT);
        if ((timezone != null) && (!timezone.equals(""))) {
            df.setTimeZone(TimeZone.getTimeZone(timezone));
        }
        Date date1 = df.parse(dateString);
        long temp = date1.getTime();
        return temp;
    }

    public static long version2timestamp(Long version) throws ParseException {
        String str = version.toString();
        String realStr = str.substring(0, 12);
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        Date parse = df.parse(realStr);
        return parse.getTime();
    }

    public static Date longToDate(Long time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DEFAULT_FORMAT);
        String tmp = simpleDateFormat.format(time);
        Date result = null;
        try {
            result = simpleDateFormat.parse(tmp);
        } catch (ParseException e) {
            ErrorLog.error("转换日期异常", e);
        }
        return result;
    }

    public static boolean isCurrActionVersion(Long actionId) {
        return ActionUtil.getCurrActionVersion().compareTo(String.valueOf(actionId)) <= 0;
    }

    public static boolean isTodayActionVersion(String actionId) {
        return ActionUtil.getTodayAction().compareTo(actionId) <= 0;
    }

    public static boolean jobEquals(Long first, Long second) {
        if (first == null || second == null) {
            return false;
        }
        String actionA = String.valueOf(first);
        String actionB = String.valueOf(second);


        int lenA = actionA.length();
        int lenB = actionB.length();
        int len = 6;
        if (lenA < len || lenB < len) {
            return false;
        }
        return actionA.substring(lenA - len).equals(actionB.substring(lenB - len));
    }


    public static Long getMillisFromStrDate(String date) {
        return getDateByDateStr(date, DEFAULT_FORMAT).getTime();
    }

    public static Date getDateByDateStr(String date, String format) {
        try {
            return new SimpleDateFormat(format).parse(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException("解析时间异常:" + date, e);
        }
    }

    public static Long getCurrentMillis() {
        return System.currentTimeMillis();
    }

    public static Integer getJobId(String action) {
        if (StringUtils.isBlank(action)) {
            return null;
        }
        int actionLen = action.length();
        int len = 6;
        if (actionLen < len) {
            return null;
        }
        return Integer.parseInt(action.substring(actionLen - len));
    }

    public static Integer getJobId(Long action) {
        return getJobId(String.valueOf(action));
    }

    /**
     * 获取当前时间 的 下一天的版本
     *
     * @return Long{201811290000000000}
     */
    public static Long getLongNextDayActionVersion() {
        return Long.parseLong(getNextDayActionVersion());
    }

    /**
     * 获取当前时间 的 下一天的版本
     *
     * @return String{201811290000000000}
     */
    public static String getNextDayActionVersion() {
        return new DateTime().plusDays(1).toString(ACTION_VERSION_INIT);
    }


    public static Long getMillisByAction(String actionId) {
        return getDateByDateStr(String.valueOf(Long.parseLong(actionId) / 1000000), ACTION_MIN).getTime();
    }

    public static int hourToInt(String hour) {
        String[] hours = hour.split(Constants.COLON);
        if (hours.length != 2) {
            throw new RuntimeException("时间格式错误:" + hour);
        }
        return Integer.parseInt(hours[0]) * 60 + Integer.parseInt(hours[1]);
    }

    public static String intTOHour(int time) {
        int hour = time / 60;
        int minute = time % 60;
        return (hour <= 9 ? "0" + hour : hour) + ":" + (minute <= 9 ? "0" + minute : minute);
    }


    public static Integer getHourOfDay() {
        return new DateTime().getHourOfDay();
    }


}
