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
        MonitorLog.info("==========开始启动: " + ActionUtil.getDefaultFormatterDate(startTime));
    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {

    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {

    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {

    }

    @Override
    public void finished(ConfigurableApplicationContext context, Throwable exception) {
        Date endTime = new Date();
        MonitorLog.info("==========启动完成: " + ActionUtil.getDefaultFormatterDate(endTime) + "; 共花费: " + (endTime.getTime() - startTime.getTime()) + "ms");
    }
}