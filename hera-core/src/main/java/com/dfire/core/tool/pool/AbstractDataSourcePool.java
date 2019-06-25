package com.dfire.core.tool.pool;

import com.alibaba.druid.pool.DruidDataSource;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.logs.ErrorLog;

import java.sql.Connection;

/**
 * @Description : Druid连接池
 * @Author ： HeGuoZi
 * @Date ： 10:04 2018/8/24
 * @Modified :
 */
public abstract class AbstractDataSourcePool {

    private volatile boolean isClose;

    private DruidDataSource dataSource;

    public AbstractDataSourcePool() {
        dataSource = new DruidDataSource();
        dataSource.setDriverClassName(HeraGlobalEnv.getSparkDriver());
        dataSource.setUrl(HeraGlobalEnv.getSparkAddress());
        dataSource.setUsername(HeraGlobalEnv.getSparkUser());
        dataSource.setPassword(HeraGlobalEnv.getSparkPassword());
        dataSource.setInitialSize(1);
        dataSource.setMaxActive(20);
        dataSource.setMinIdle(1);
        dataSource.setTestOnBorrow(true);
        dataSource.setTestOnReturn(false);
        dataSource.setTestWhileIdle(true);
        dataSource.setTimeBetweenEvictionRunsMillis(600 * 1000);
        isClose = false;
    }

    public Connection getConnection() {
        if (isClose || dataSource == null) {
            return null;
        }
        try {
            return dataSource.getConnection();
        } catch (Exception e) {
            ErrorLog.error("获取连接失败", e);
            return null;
        }
    }

    public void close() {
        if (!isClose) {
            isClose = true;
            if (dataSource != null) {
                dataSource.close();
                dataSource = null;
            }
        }
    }

    public boolean isClose() {
        return isClose;
    }

}
