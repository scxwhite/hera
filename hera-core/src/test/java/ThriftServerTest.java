import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @Description ： ThriftServerTest
 * @Author ： HeGuoZi
 * @Date ： 16:16 2018/8/21
 * @Modified :
 */
public class ThriftServerTest {

    @Test
    public void executeSql() {

        String url = "jdbc:hive2://10.10.18.215:10000";
        try {
            Class.forName("org.apache.hive.jdbc.HiveDriver");
            Connection conn = DriverManager.getConnection(url, "heguozi", "123456");
            Statement stmt = conn.createStatement();
            String sql = "SELECT count(*) FROM ods_order_org.instancedetail";
            ResultSet res = stmt.executeQuery(sql);
            while (res.next()) {
                System.out.println("1");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void executeSql2() {

        String url = "jdbc:hive2://10.1.21.141:10000";
        try {
            Class.forName("org.apache.hive.jdbc.HiveDriver");
            Connection conn = DriverManager.getConnection(url, "heguozi", "123456");
            Statement stmt = conn.createStatement();
            String sql = "show databases";
            ResultSet res = stmt.executeQuery(sql);
            while (res.next()) {
                System.out.println("1");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
