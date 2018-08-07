package com.dfire.api;

import com.dfire.common.entity.HeraGroup;
import com.dfire.common.entity.HeraJob;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by xiaosuda on 2018/7/20.
 */
public class ZeusToHera {


    private String hera_username = "lineage";
    private String hera_password = "lineage@552208";
    private String hera_url = "jdbc:mysql://rdsdb1101.my.2dfire-inc.com:3306/lineage";
    private String zeus_username = "scm_zeusdb";
    private String zeus_password = "scm_zeusdb@552208";
    private String zeus_url = "jdbc:mysql://rdsdb1101.my.2dfire-inc.com:3306/scm_zeusdb";
    private String driver = "com.mysql.jdbc.Driver";
    private Connection heraConnection = null;
    private Connection zeusConnection = null;
    private final String env = "daily";
    private final boolean isAll = true;

    private String tableName = "hera_group";
    private Class<?> clazz = HeraGroup.class;

    private List<Integer> jobs = Arrays.asList(6625, 6628, 971);


    @Before
    public void init() throws SQLException, ClassNotFoundException {
        Class.forName(driver);
        if (env.equals("daily")) {
            hera_url = "jdbc:mysql://common101.my.2dfire-daily.com:3306/hera";
            hera_username = "twodfire";
            hera_password = "123456";

            zeus_url = "jdbc:mysql://common101.my.2dfire-daily.com:3306/zeus";
            zeus_username = hera_username;
            zeus_password = hera_password;
        }
        if (clazz == HeraGroup.class) {
            tableName = "_group";
        } else if (clazz == HeraJob.class) {
            tableName = "_job";
        }


        heraConnection = DriverManager.getConnection(hera_url, hera_username, hera_password);
        zeusConnection = DriverManager.getConnection(zeus_url, zeus_username, zeus_password);
    }


    @Test
    public void postExecute() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet("http://10.1.28.81:8080/hera/scheduleCenter/execute?id=1245&owner=biadmin");
        httpClient.execute(httpGet);

    }

    @Test
    public void parallelTest() throws SQLException, IOException {
        PreparedStatement statement = heraConnection.prepareStatement("select id from hera_job where auto = 1");

        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            String id = resultSet.getString("id");
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet("http://10.1.28.81:8080/hera/scheduleCenter/execute?id=" + id +"&owner=biadmin");
            httpClient.execute(httpGet);
            System.out.println(id + ": ok");
        }
    }


    @Test
    public void moveJob() throws SQLException {
        StringBuilder selectSql = new StringBuilder();
        StringBuilder deleteSql = new StringBuilder();
        if (isAll) {
            selectSql.append("select * from zeus").append(tableName);
            deleteSql.append("truncate table hera").append(tableName);
        } else {
            selectSql.append("select * from zeus").append(tableName).append(" where id in (");
            selectSql.append(jobs.get(0));
            int size = jobs.size();
            for (int index = 1; index < size; index++) {
                selectSql.append(",").append(jobs.get(index));
                deleteSql.append(jobs.get(index)).append(",");
            }
            selectSql.append(")");
            deleteSql.replace(deleteSql.length() - 1, deleteSql.length(), ")");
        }

        PreparedStatement statement = zeusConnection.prepareStatement(selectSql.toString());
        ResultSet resultSet = statement.executeQuery();
        Map<String, String> cacheMap = new HashMap<>();
        List<String> fields = initFields(cacheMap);

        StringBuilder insertSql = new StringBuilder();
        StringBuilder fieldStr = new StringBuilder();
        StringBuilder valueStr = new StringBuilder();
        PreparedStatement heraPs;
        PreparedStatement preparedStatement = heraConnection.prepareStatement(deleteSql.toString());
        preparedStatement.executeUpdate();
        while (resultSet.next()) {
            Map<String, String> res = new HashMap<>();
            insertSql.delete(0, insertSql.length());
            fieldStr.delete(0, fieldStr.length());
            valueStr.delete(0, valueStr.length());
            insertSql.append("insert into hera").append(tableName).append(" ( ");
            fields.forEach(x -> {
                try {
                    if (resultSet.getString(x) != null) {
                        valueStr.append("'").append(resultSet.getString(x).replace("'", "''").replace("\\", "\\\\")).append("',");
                        fieldStr.append(x).append(",");
                    }
                } catch (SQLException e) {
                    res.put(x, null);
                }
            });
            insertSql.append(fieldStr.substring(0, fieldStr.length() - 1))
                    .append(") values (")
                    .append(valueStr.substring(0, valueStr.length() - 1)).append(")").append(";");

            heraPs = heraConnection.prepareStatement(insertSql.toString());
            System.out.println("执行结果：" + heraPs.executeUpdate());
        }
    }

    public List initFields(Map<String, String> cacheMap) {
        Field[] declaredFields = clazz.getDeclaredFields();
        String name;
        Integer size;
        char ch;
        List<String> field = new ArrayList<>(declaredFields.length);
        for (Field declaredField : declaredFields) {
            name = declaredField.getName();
            size = name.length();

            for (int i = 0; i < size; i++) {
                ch = name.charAt(i);
                if (ch >= 'A' && ch <= 'Z') {
                    name = name.replace(ch + "", ("_" + ch).toLowerCase());
                    size++;
                    i++;
                }
            }
            cacheMap.put(declaredField.getName(), name);
            field.add(name);
        }
        return field;
    }

    public void setValue(Map<String, String> map, Map<String, String> cacheMap, Object bean) {
        Class<?> aClass = bean.getClass();
        Method[] methods = aClass.getDeclaredMethods();
        String methodName;
        for (Method method : methods) {
            methodName = method.getName();
            if (methodName.startsWith("set")) {
                methodName = methodName.substring(3);
                String key = cacheMap.get(methodName.substring(0, 1).toLowerCase() + methodName.substring(1));
                try {
                    if (map.get(key) != null) {
                        Class<?>[] types = method.getParameterTypes();
                        String typeName = types[0].getTypeName();
                        System.out.println(typeName);
                        if (typeName.equals("java.lang.Integer") || typeName.equals("int")) {
                            method.invoke(bean, Integer.parseInt(map.get(key)));
                        } else if (typeName.equals("java.util.Date")) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                            method.invoke(bean, sdf.parse(map.get(key)));
                        } else if (typeName.equals("java.lang.Long")) {
                            method.invoke(bean, Long.parseLong(map.get(key)));
                        } else {
                            method.invoke(bean, map.get(key));
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        }

    }
}
