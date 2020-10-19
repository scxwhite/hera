package com.dfire.config;

import com.dfire.common.util.ActionUtil;
import com.dfire.logs.MonitorLog;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Date;

/**
 * desc:
 * hera启动监听
 *
 * @author scx
 * @create 2019/03/25
 */
public class HeraRunListener implements SpringApplicationRunListener {


    private Date startTime;
    private SpringApplication application;
    private String[] args;


    public HeraRunListener(SpringApplication application, String[] args) {
        this.application = application;
        this.args = args;
    }

    @Override
    public void starting() {
        startTime = new Date();
        MonitorLog.info("spring starting: " + ActionUtil.getDefaultFormatterDate(startTime));
    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
        MonitorLog.info("spring environmentPrepared");


    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        MonitorLog.info("spring contextPrepared");

    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        MonitorLog.info("spring contextLoaded");

    }

    @Override
    public void finished(ConfigurableApplicationContext context, Throwable exception) {
        Date endTime = new Date();
        int serverPort = Integer.parseInt(context.getEnvironment().getProperty("server.port"));
        MonitorLog.info("Tomcat started on port(s):" + serverPort);
        MonitorLog.info("固定集群ip为:" + HeraGlobalEnv.getEmrFixedHost());
        MonitorLog.info("==========启动完成了: " + ActionUtil.getDefaultFormatterDate(endTime) + "; 共花费: " + DateBetween(startTime, endTime));
    }


    private String DateBetween(Date start, Date end) {
        if (start.compareTo(end) > 0) {
            Date tmp;
            tmp = start;
            start = end;
            end = tmp;
        }

        long cost = (end.getTime() - start.getTime()) / 1000;

        long hours, minute, seconds;

        hours = cost / 60 / 60;

        cost = cost % (60 * 60);

        minute = cost / 60;
        cost = cost % 60;
        seconds = cost;
        StringBuilder res = new StringBuilder();
        if (hours > 9) {
            res.append(hours);
        } else {
            res.append("0").append(hours);
        }
        res.append(":");
        if (minute > 9) {
            res.append(minute);
        } else {
            res.append("0").append(minute);
        }
        res.append(":");
        if (seconds > 9) {
            res.append(seconds);
        } else {
            res.append("0").append(seconds);
        }
        return res.toString();
    }
}
