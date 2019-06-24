package com.dfire.common.util;

import com.dfire.common.kv.Tuple;
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
    public static final String ACTION_VERSION_CURR = "yyyyMMddHHmmss0000";
    /**
     * 当前小时版本格式
     */
    public static final String ACTION_VERSION_HOUR = "yyyyMMddHH00000000";

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


    public static String getCurrActionVersion() {
        return new DateTime().toString(ACTION_VERSION_CURR);
    }

    public static String getCurrHourVersion() {
        return new DateTime().toString(ACTION_VERSION_HOUR);
    }

    public static Long getLongCurrActionVersion() {
        return Long.parseLong(new DateTime().toString(ACTION_VERSION_CURR));
    }

    public static String getInitActionVersion() {
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

    public static Date longToDate(Long time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DEFAULT_FORMAT);
        String tmp = simpleDateFormat.format(time);
        Date result = null;
        try {
            result = simpleDateFormat.parse(tmp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean isCurrActionVersion(String actionId) {
        return ActionUtil.getCurrActionVersion().compareTo(actionId) <= 0;
    }

    public static boolean isTodayActionVersion(String actionId) {
        return ActionUtil.getInitActionVersion().compareTo(actionId) <= 0;
    }

    public static boolean jobEquals(String actionA, String actionB) {
        if (StringUtils.isBlank(actionA) || StringUtils.isBlank(actionB)) {
            return false;
        }
        int lenA = actionA.length();
        int lenB = actionB.length();
        int len = 4;
        if (lenA < len || lenB < len) {
            return false;
        }
        return actionA.substring(lenA - len).equals(actionB.substring(lenB - len));
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


}
