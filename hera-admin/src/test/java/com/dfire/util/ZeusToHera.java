package com.dfire.util;

import com.dfire.common.entity.HeraFile;
import com.dfire.common.entity.HeraGroup;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.HeraUser;
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
 * desc:
 * zeus到hera迁移工具
 *
 * @author scx
 * @create 2019/03/20
 */
public class ZeusToHera {

    /**
     * hera连接信息
     */
    private String hera_username = "hera";
    private String hera_password = "hera";
    private String hera_url = "jdbc:mysql://localhost:3306/hera";

    /**
     * zeus连接信息
     */
    private String zeus_username = "zeus";
    private String zeus_password = "zeus";
    private String zeus_url = "jdbc:mysql://localhost:3306/zeus";


    private String driver = "com.mysql.jdbc.Driver";


    private Connection heraConnection = null;
    private Connection zeusConnection = null;

    /**
     * 是否迁移所有zeus_job 任务 （true 时会删除 hera对应表的所有数据）
     */
    private final boolean isAll = true;

    /**
     * isAll 为false时 只迁移该列表的任务
     */
    private List<Integer> jobs = Arrays.asList(6625, 6628, 971);


    /**
     * 要迁移的zeus表对应的hera entity 类
     */
    private Class<?> clazz = HeraJob.class;
    /**
     * 根据 clazz 自动生成
     */
    private String tableName;




    @Before
    public void init() throws SQLException, ClassNotFoundException {
        Class.forName(driver);
        if (clazz == HeraGroup.class) {
            tableName = "_group";
        } else if (clazz == HeraJob.class) {
            tableName = "_job";
        } else if (clazz == HeraFile.class) {
            tableName = "_file";
        } else if (clazz == HeraUser.class) {
            tableName = "_user";
        } else {
            throw new RuntimeException("暂不支持的类型:" + clazz.getSimpleName());
        }

        heraConnection = DriverManager.getConnection(hera_url, hera_username, hera_password);
        zeusConnection = DriverManager.getConnection(zeus_url, zeus_username, zeus_password);
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
        int cnt = 0;
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
            System.out.println("序号：" + (++cnt) + "执行结果：" + heraPs.executeUpdate());
        }
    }

    public List<String> initFields(Map<String, String> cacheMap) {
        Field[] declaredFields = clazz.getDeclaredFields();
        String name;
        int size;
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
