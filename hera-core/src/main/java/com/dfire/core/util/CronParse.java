package com.dfire.core.util;

import com.dfire.common.util.ActionUtil;
import com.dfire.logs.ErrorLog;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午10:15 2018/5/10
 * @desc cron表达式解析工具，返回解析result
 */
public class CronParse {

    public static boolean Parser(String cronExpression, String cronDate, List<String> result) {
        if (cronExpression == null || cronExpression.length() < 1
                || cronDate == null || cronDate.length() < 1) {
            return false;
        } else {
            CronExpression exp;
            try {
                // 初始化cron表达式解析器
                exp = new CronExpression(cronExpression);
            } catch (ParseException e) {
                ErrorLog.error("解析cron表达式异常", e);
                return false;
            }
            // 定义生成时间范围
            // 定义开始时间，前一天的23点59分59秒
            Calendar c = Calendar.getInstance();
            String sStart = cronDate + " 00:00:00";
            SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                    ActionUtil.DEFAULT_FORMAT);
            Date dStart = null;
            try {
                dStart = sdf.parse(sStart);
            } catch (ParseException e) {
                ErrorLog.error("日期解析失败", e);
                return false;
            }
            c.setTime(dStart);
            c.add(Calendar.SECOND, -1);
            dStart = c.getTime();

            // 定义结束时间，当天的23点59分59秒
            c.add(Calendar.DATE, 1);
            Date dEnd = c.getTime();

            // 生成时间序列
            Date dd = dStart;
            dd = exp.getNextValidTimeAfter(dd);
            while ((dd.getTime() >= dStart.getTime())
                    && (dd.getTime() <= dEnd.getTime())) {
                result.add(sdf.format(dd));
                dd = exp.getNextValidTimeAfter(dd);
            }
        }
        return true;
    }
}
