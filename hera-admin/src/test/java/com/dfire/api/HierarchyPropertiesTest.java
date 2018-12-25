package com.dfire.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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

    }

    @Test
    public void buildGlobalGraphTest() {
    }


}
