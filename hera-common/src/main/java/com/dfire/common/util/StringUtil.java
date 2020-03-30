package com.dfire.common.util;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPathException;
import com.dfire.common.constants.Constants;
import com.dfire.common.processor.DownProcessor;
import com.dfire.common.processor.Processor;
import com.dfire.logs.ErrorLog;
import org.apache.commons.lang.StringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 18:06 2018/1/7
 * @desc 字符串处理工具类
 */

public class StringUtil {

    /**
     * @param sourceStr
     * @return
     * @desc 登陆密码md5加密
     */

    public static String EncoderByMd5(String sourceStr) {
        int i;
        StringBuffer buf = new StringBuffer("");
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            ErrorLog.error("MD5 错误", e);
        }
        md.update(sourceStr.getBytes());
        byte b[] = md.digest();
        for (int offset = 0; offset < b.length; offset++) {
            i = b[offset];
            if (i < 0) {
                i += 256;
            }
            if (i < 16) {
                buf.append("0");
            }
            buf.append(Integer.toHexString(i));
        }
        return buf.toString();
    }

    /**
     * job中的config解析成map
     *
     * @param config
     * @return
     */
    public static Map<String, String> convertStringToMap(String config) throws RuntimeException {
        if (StringUtils.isBlank(config) || "{}".equals(config)) {
            return new HashMap<>(0);
        }
        JSONObject jsonObject;
        try {
            jsonObject = JSONObject.parseObject(config);
        } catch (JSONPathException e) {
            throw new RuntimeException("json parse error:", e);
        }
        Map<String, String> map = new TreeMap<>();
        for (Object key : jsonObject.keySet()) {
            map.put(key.toString(), jsonObject.getString(key.toString()));
        }
        return map;
    }


    public static Map<String, String> configsToMap(String configs) {
        configs = configs.trim();
        Map<String, String> configMap = new HashMap<>();
        String[] split = configs.split("\n");
        Arrays.stream(split).forEach(x -> {
            int index = x.indexOf("=");
            if (index != -1) {
                configMap.put(x.substring(0, index).trim(), x.substring(index + 1).trim());
            }
        });
        return configMap;
    }


    public static String mapToConfigs(Map<String, String> configMap) {
        StringBuilder configs = new StringBuilder();
        configMap.forEach((key, val) -> configs.append(key).append("=").append(val).append("\n"));
        return configs.toString();
    }

    /**
     * config转成json
     *
     * @param config
     * @return
     */
    public static String convertMapToString(Map<String, String> config) {
        if (config == null) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, String> entry : config.entrySet()) {
            jsonObject.put(entry.getKey(), entry.getValue());
        }
        return jsonObject.toString();
    }


    public static List<Processor> convertProcessorToList(String processor) {
        List<Processor> list = new ArrayList<>();
        Processor result = null;
        if (processor == null || processor.equals("") || processor.equals("[]")) {
            return list;
        }
        JSONObject jsonObject = JSONObject.parseObject(processor);
        String id = jsonObject.getString("id");
        if (StringUtils.isNotBlank(id)) {
            if (("download").equals(id)) {
                result = new DownProcessor();
                list.add(result);
            }
        }
        return list;
    }

    public static String convertProcessorToList(List<Processor> list) {
        JSONArray preArray = new JSONArray();
        for (Processor p : list) {
            JSONObject o = new JSONObject();
            o.put("id", p.getId());
            o.put("config", p.getConfig());
            preArray.add(o);
        }
        return preArray.toString();
    }


    public static boolean actionIdToJobId(String actionId, String id) {
        String str = actionId.substring(12);
        Integer id1 = Integer.valueOf(str);
        Integer id2 = Integer.valueOf(id);
        return id1.equals(id2);
    }

    public static boolean actionIdToJobId(Long actionId, String id) {
        return actionIdToJobId(String.valueOf(actionId), id);
    }

    /**
     * job resource转换为List
     *
     * @param resource
     * @return
     */
    public static List<Map<String, String>> convertResources(String resource) {
        List<Map<String, String>> tempRes = new ArrayList<>();
        JSONArray resArray = new JSONArray();

        if (StringUtils.isNotBlank(resource)) {

            try {
                resArray = JSONArray.parseArray(resource);
            } catch (Exception e) {
                ErrorLog.error("解析字符串异常", e);
            }
            for (int i = 0; i < resArray.size(); i++) {
                JSONObject o = resArray.getJSONObject(i);
                Map<String, String> map = new HashMap<>();
                for (Object key : o.keySet()) {
                    map.put(key.toString(), o.getString(key.toString()));
                }
                tempRes.add(map);
            }
        }
        return tempRes;
    }

    public static String convertResourceToString(List<Map<String, String>> list) {
        String resource = "[]";
        if (list != null && list.size() > 0) {
            JSONArray resArray = new JSONArray();
            for (Map<String, String> map : list) {
                JSONObject o = new JSONObject();
                for (String key : map.keySet()) {
                    o.put(key, map.get(key));
                }
                resArray.add(o);
            }
            resource = resArray.toString();
        }
        return resource;
    }

    public static Integer getGroupId(String group) {
        String groupNum = group;
        if (group.startsWith(Constants.GROUP_PREFIX)) {
            groupNum = group.split("_")[1];
        }
        Integer res;
        try {
            res = Integer.parseInt(groupNum);
        } catch (Exception e) {
            throw new IllegalArgumentException("无法识别的groupId：" + group);
        }

        return res;
    }
}
