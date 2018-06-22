package com.dfire.api;

import com.dfire.common.entity.model.HeraJobBean;
import com.dfire.common.util.HierarchyProperties;
import com.dfire.common.util.RenderHierarchyProperties;
import com.dfire.common.util.SpringContextHolder;
import com.dfire.core.util.JobUtils;
import graph.JobGroupGraphTool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:05 2018/6/22
 * @desc
 */
@ComponentScan(basePackages = "com.dfire")
@MapperScan(basePackages = "com.dfire.common.mapper")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = HierarchyPropertiesTest.class)
public class HierarchyPropertiesTest {

    @Test
    public void groupBeanTest() {
        HeraJobBean jobBean = JobGroupGraphTool.getUpstreamJobBean("201806120300000093");

        HierarchyProperties properties = jobBean.getHierarchyProperties();
        List<Map<String, String>> list = jobBean.getHierarchyResources();
        String script = "echo ${a};  ${zdt.addDay(-1).format(\"yyyyMMdd\")}; download[hdfs:///zeus/hdfs-upload-dir/boss_monitor.py boss_monitor.py]";
        String replaceScript = JobUtils.replace(properties.getAllProperties(), script);
        System.out.println(replaceScript);

        System.out.println(properties.getAllProperties().toString());
        System.out.println(list.toString());

        RenderHierarchyProperties renderHierarchyProperties = new RenderHierarchyProperties(properties);

        String dateReplace = RenderHierarchyProperties.render(script,"2018061203000000");
        System.out.println(dateReplace);

        String resourceReplace = JobUtils.resolveScriptResource(list, script, SpringContextHolder.getApplicationContext());
        System.out.println(resourceReplace);
        System.out.println(list);


    }


}
