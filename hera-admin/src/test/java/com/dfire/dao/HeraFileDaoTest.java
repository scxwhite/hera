package com.dfire.dao;

import com.dfire.common.entity.HeraFile;
import com.dfire.common.service.HeraFileService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:14 2018/5/14
 * @desc
 */
@ComponentScan(basePackages = "com.dfire")
@MapperScan(basePackages = "com.dfire.common.mapper")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = HeraFileDaoTest.class)
public class HeraFileDaoTest {

    @Autowired
    private HeraFileService heraFileService;

    @Test
    public void select() {
        HeraFile heraFile = heraFileService.getHeraFile("1");
        System.out.println(heraFile.getGmtModified());

    }
}
