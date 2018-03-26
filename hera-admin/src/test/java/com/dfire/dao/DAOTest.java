package com.dfire.dao;

import com.dfire.common.entity.ZeusFile;
import com.dfire.common.entity.ZeusJob;
import com.dfire.common.service.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 12:29 2018/1/12
 * @desc
 */
@ComponentScan(basePackages = "com.dfire")
@MapperScan(basePackages = "com.dfire.common.mapper")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DAOTest.class)
public class DAOTest {

    @Autowired
    ZeusHostGroupService hostGroupService;
    @Autowired
    ZeusUserService zeusUserService;
    @Autowired
    ZeusJobService zeusJobService;
    @Autowired
    ZeusLockService zeusLockService;
    @Autowired
    ZeusFileService zeusFileService;

    @Test
    public void getHostGroupList() {
        String id = "1";
        List<String> list = hostGroupService.getPreemptionGroup(id);
        ZeusJob zeusJob = zeusJobService.findByName(675);
        System.out.println(list.size());
        System.out.println(zeusLockService.getZeusLock("online"));

    }
    @Test
    public void zeusFileTest() {
        List<ZeusFile> list = zeusFileService.getFileListByOwner("biadmin");
        System.out.println(list.get(0));
    }
}
