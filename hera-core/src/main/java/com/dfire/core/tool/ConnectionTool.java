package com.dfire.core.tool;

import com.dfire.core.config.HeraGlobalEnvironment;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * @Description : ConnectionTool
 * @Author ： HeGuoZi
 * @Date ： 17:19 2018/8/21
 * @Modified :
 */
@Slf4j
public class ConnectionTool {

    private static Statement stmt;

    public static Statement getConnection() {
        if (stmt == null) {
            try {
                Class.forName(HeraGlobalEnvironment.getSparkDriver());
                Connection conn = DriverManager.getConnection(
                        HeraGlobalEnvironment.getSparkAddress(),
                        HeraGlobalEnvironment.getSparkUser(),
                        HeraGlobalEnvironment.getSparkPassword());
                stmt = conn.createStatement();
                log.info("hive建立连接成功");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return stmt;
    }
}
