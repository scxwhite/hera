package com.dfire.api;

import com.dfire.common.util.RenderHierarchyProperties;
import com.dfire.common.util.StringUtil;
import org.junit.Test;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 18:29 2018/3/22
 * @desc
 */
public class StringUtilTest {

    @Test
    public void md5() {
        System.out.println(StringUtil.EncoderByMd5("123456"));

    }

    @Test
    public void parseDate() {
        String s = RenderHierarchyProperties.render("${zdt.add(5,-2).format(\"yyyy-MM-dd\")}");
        System.out.println(s);
        s = RenderHierarchyProperties.render("${zdt.addDay(100).format(\"yyyyMMdd\")}");
        System.out.println(s);
        s = RenderHierarchyProperties.render("${yesterday}");
        System.out.println(s);

    }
}
