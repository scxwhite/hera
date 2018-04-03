package com.dfire.dao;

import com.dfire.common.entity.HeraFile;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.service.*;
import com.dfire.common.tree.TreeHelper;
import com.dfire.common.tree.TreeNode;
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
        List<HeraFile> list = heraFileService.getHeraFileListByOwner("biadmin");
        HeraFile rootFile = list.stream().filter(file -> file.getParent() == null).findAny().get();
        TreeNode root = TreeNode.builder().id(rootFile.getId()).parentId("").object(rootFile).build();
        TreeHelper.Convert<HeraFile, TreeNode> convert = (HeraFile file) -> {
            TreeNode treeNode = new TreeNode();
            treeNode.setId(file.getId());
            if(StringUtils.isNotBlank(file.getParent())) {
                treeNode.setParentId(file.getParent());
            }
            treeNode.setObject(file);
            return treeNode;
        };
        Map<String, HeraFile> heraFileMap = list.stream().collect(Collectors.toMap(file -> file.getId(), Function.identity(), (v1,v2) -> v2));
        TreeHelper treeHelper = new TreeHelper();
        treeHelper.setRoot(root);
        treeHelper.setTempNodeList(treeHelper.convert(list, convert));
        treeHelper.generateTree();
        TreeNode node = treeHelper.getRoot();
        node.traverse();
    }
}
