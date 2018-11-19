package com.dfire.common.util;

import com.dfire.common.constants.TimeFormatConstant;
import com.dfire.logs.HeraLog;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.StringWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 15:40 2018/3/22
 * @desc 层次结构属性解析，hera时间配置解析
 */
public class RenderHierarchyProperties extends HierarchyProperties {

    private HierarchyProperties properties;

    static {
        try {
            Velocity.init();
        } catch (Exception e) {
            HeraLog.error("velocity init fail", e);
        }
    }

    static Pattern pt = Pattern.compile("\\$\\{zdt.*?\\}");

    public RenderHierarchyProperties(HierarchyProperties properties) {
        super(new HashMap<>());
        this.properties = properties;
    }

    /**
     * @param template
     * @return
     * @desc hera配置日期变量替换, 如：${zdt.addDay(-2).format("yyyyMMdd")}，${zdt.addDay(-1).format("yyyyMMdd")}
     */
    public static String render(String template) {
        if (template == null) {
            return null;
        }
        Matcher matcher = pt.matcher(template);
        while (matcher.find()) {
            String m = template.substring(matcher.start(), matcher.end());
            StringWriter sw = new StringWriter();
            try {
                VelocityContext context = new VelocityContext();
                context.put("zdt", new HeraDateTool(new Date()));
                Velocity.evaluate(context, sw, "", m);
                if (m.equals(sw.toString())) {
                    HeraLog.error("render fail with target:" + m);
                    break;
                }
            } catch (Exception e) {
                HeraLog.error("zdt render error", e);
                break;
            }
            template = template.replace(m, sw.toString());
            matcher = pt.matcher(template);
        }
        template = template.replace("${yesterday}", new HeraDateTool(new Date()).addDay(-1).format(TimeFormatConstant.YYYYMMDD));
        return template;
    }

    /**
     * @param template
     * @param dateStr
     * @return hera配置日期变量替换,"${yesterday}"为系统变量
     */
    public static String render(String template, String dateStr) {
        if (template == null) {
            return null;
        }
        Matcher matcher = pt.matcher(template);
        while (matcher.find()) {
            String m = template.substring(matcher.start(), matcher.end());
            StringWriter sw = new StringWriter();
            try {
                VelocityContext context = new VelocityContext();
                context.put("zdt", new HeraDateTool(HeraDateTool.StringToDate(dateStr, "yyyyMMddHHmmss")));
                Velocity.evaluate(context, sw, "", m);
                if (m.equals(sw.toString())) {
                    HeraLog.error("render fail with target:" + m);
                    break;
                }
            } catch (Exception e) {
                HeraLog.error("zdt render error", e);
                break;
            }
            template = template.replace(m, sw.toString());
            matcher = pt.matcher(template);
        }
        template = template.replace("${yesterday}", new HeraDateTool(HeraDateTool.StringToDate(dateStr, "yyyyMMddHHmmss")).addDay(-1).format("yyyyMMdd"));
        return template;
    }

    @Override
    public HierarchyProperties getParent() {
        return new RenderHierarchyProperties(properties.getParent());
    }

    @Override
    public Map<String, String> getLocalProperties() {
        Map<String, String> map = properties.getLocalProperties();
        Map<String, String> result = properties.getLocalProperties().keySet().stream().collect(Collectors.toMap(v -> v, s -> render(map.get(s)), (t, k) -> k));
        return result;
    }

    @Override
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    @Override
    public Set<String> getPropertyKeys() {
        Set<String> result = new HashSet<>();
        for (String s : properties.getPropertyKeys()) {
            String render = render(s);
            if (render != null) {
                result.add(render);
            }
        }
        return properties.getPropertyKeys();
    }

    @Override
    public String getProperty(String key) {
        return render(properties.getProperty(key));
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return render(properties.getProperty(key, defaultValue));
    }

    @Override
    public String getLocalProperty(String key) {
        return render(properties.getLocalProperty(key));
    }

    @Override
    public String getLocalProperty(String key, String defaultValue) {
        return render(properties.getLocalProperty(key));
    }

    @Override
    public List<String> getHierarchyProperty(String key) {
        List<String> result = properties.getHierarchyProperty(key).stream().map(RenderHierarchyProperties::render).collect(Collectors.toList());
        return result;
    }

    @Override
    public Map<String, String> getAllProperties() {
        Map<String, String> map = properties.getAllProperties();
        Map<String, String> result = properties.getAllProperties().keySet().stream().collect(Collectors.toMap(v -> v, s -> render(map.get(s)), (t, k) -> k));
        return result;
    }

}
