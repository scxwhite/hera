package com.dfire.dao;

import com.dfire.common.entity.HeraFile;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.vo.HeraFileVo;
import com.dfire.common.service.*;
import com.dfire.common.tree.HeraFileTreeNode;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    HeraHostGroupService hostGroupService;
    @Autowired
    HeraUserService heraUserService;
    @Autowired
    HeraJobService heraJobService;
    @Autowired
    HeraLockService heraLockService;
    @Autowired
    HeraFileService heraFileService;

    @Test
    public void getHostGroupList() {
        String id = "1";
        List<String> list = hostGroupService.getPreemptionGroup(id);
        HeraJob heraJob = heraJobService.findByName(675);
        System.out.println(list.size());

        System.out.println(heraLockService.getHeraLock("online"));

    }

    @Test
    public void heraFileTest() {
        List<HeraFileVo> list = heraFileService.getSubHeraFiles("3");
        System.out.println(list.size());
        HeraFileTreeNode node = heraFileService.getUserFiles();
        System.out.println(node.getChildList().size());
        node.traverse();
    }
}
