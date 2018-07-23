package com.dfire.api;

import com.dfire.common.entity.HeraJob;
import org.junit.Before;
import org.junit.Test;

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


    private final String hera_username = "lineage";
    private final String hera_password = "lineage@552208";
    private final String hera_url = "jdbc:mysql://rdsdb1101.my.2dfire-inc.com:3306/lineage";
    private final String zeus_username = "scm_zeusdb";
    private final String zeus_password = "scm_zeusdb@552208";
    private final String zeus_url = "jdbc:mysql://rdsdb1101.my.2dfire-inc.com:3306/scm_zeusdb";
    private final String driver = "com.mysql.jdbc.Driver";
    private Connection heraConnection = null;
    private Connection zeusConnection = null;

    private List<Integer> jobs = Arrays.asList(6608, 6610, 6613, 6622, 6612, 6620, 6624, 4946, 6625, 6628, 971);


    @Before
    public void init() throws SQLException, ClassNotFoundException {
        Class.forName(driver);
        heraConnection = DriverManager.getConnection(hera_url, hera_username, hera_password);
        zeusConnection = DriverManager.getConnection(zeus_url, zeus_username, zeus_password);
    }

    @Test
    public void moveJob() throws SQLException {
        StringBuilder selectSql = new StringBuilder("select * from zeus_job where id in (");
        selectSql.append(jobs.get(0));
        int size = jobs.size();
        for (int index = 1; index < size; index++) {
            selectSql.append(",").append(jobs.get(index));
        }
        selectSql.append(")");

        PreparedStatement statement = zeusConnection.prepareStatement(selectSql.toString());

        ResultSet resultSet = statement.executeQuery();
        Map<String, String> cacheMap = new HashMap<>();
        List<String> fields = initFields(cacheMap);

        while (resultSet.next()) {
            Map<String, String> res = new HashMap<>();
            HeraJob job = new HeraJob();
            fields.forEach(x -> {
                try {
                    res.put(x, resultSet.getString(x));
                } catch (SQLException e) {
                    res.put(x, null);
                }
            });
            setValue(res, cacheMap, job);
            //TODO  插入hera

        }
    }

    public List initFields(Map<String, String> cacheMap) {
        Field[] declaredFields = HeraJob.class.getDeclaredFields();
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
                        } else if(typeName.equals("java.util.Date")) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                            method.invoke(bean, sdf.parse(map.get(key)));
                        } else if(typeName.equals("java.lang.Long")) {
                            method.invoke(bean, Long.parseLong(map.get(key)));
                        }
                        else {
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

    public static void main(String[] args) throws ParseException {


    }
}
