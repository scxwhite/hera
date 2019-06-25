package com.dfire.common.util;


import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 14:58 2018/3/22
 * @desc 带层次结构的属性, 任务配置继承
 */
public class HierarchyProperties {

    protected HierarchyProperties parent;
    protected Map<String, String> properties;

    public HierarchyProperties(Map<String, String> properties) {
        if (properties != null) {
            this.properties = properties;
        } else {
            this.properties = new HashMap<>(0);
        }
    }

    public HierarchyProperties(HierarchyProperties parent, Map<String, String> properties) {
        this.parent = parent;
        if (properties == null) {
            this.properties = new HashMap<>(0);
        } else {
            this.properties = new HashMap<>(properties);
        }
    }

    public HierarchyProperties getParent() {
        return parent;
    }

    public Map<String, String> getLocalProperties() {
        return properties;
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

    public Set<String> getPropertyKeys() {
        Set<String> set = new HashSet<>();
        if (parent != null) {
            set.addAll(parent.getPropertyKeys());
        }
        set.addAll(properties.keySet());
        return set;
    }

    /**
     * @param key
     * @return
     * @desc 获取属性值, 如果自身属性中没有，则向父属性查询
     */
    public String getProperty(String key) {
        if (properties != null && properties.containsKey(key)) {
            return properties.get(key);
        }
        if (parent != null) {
            return parent.getProperty(key);
        }
        return null;
    }

    /**
     * @param key
     * @return
     * @desc 获取属性值, 如果自身属性中没有，则向父属性查询
     */
    public String getProperty(String key, String defaultValue) {
        return StringUtils.isBlank(getProperty(key)) ? defaultValue : getProperty(key);
    }

    /**
     * @param key
     * @return
     * @desc 获取属性值, 只在自身属性中查询
     */
    public String getLocalProperty(String key) {
        return properties.get(key);
    }

    /**
     * @param key
     * @param defaultValue
     * @return 获取属性值, 只在自身属性中查询, 如果没有，则返回传入的默认值
     */
    public String getLocalProperty(String key, String defaultValue) {
        return StringUtils.isBlank(getLocalProperty(key)) ? defaultValue : getLocalProperty(key);
    }

    /**
     * @param key
     * @return
     * @desc 向上获取所有的数据, 一般用于获取带继承性质的属性, 比如classpath，需要父级的classpath
     */
    public List<String> getHierarchyProperty(String key) {
        List<String> list = new ArrayList<>();
        if (properties.get(key) != null) {
            list.add(properties.get(key));
        }
        if (parent != null) {
            list.addAll(parent.getHierarchyProperty(key));
        }
        return list;
    }

    /**
     * 获得层级属性
     *
     * @return
     */
    public Map<String, String> getAllProperties() {
        if (parent != null) {
            Map<String, String> parentMap = new HashMap<>(parent.getAllProperties());
            parentMap.putAll(getLocalProperties());
            return parentMap;
        }
        return getLocalProperties();
    }

}
