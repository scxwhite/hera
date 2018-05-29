package com.dfire.dao;

import com.dfire.common.entity.*;
import com.dfire.common.entity.vo.HeraDebugHistoryVo;
import com.dfire.common.entity.vo.HeraJobTreeNodeVo;
import com.dfire.common.service.*;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.common.vo.HeraHostGroupVo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:15 2018/5/14
 * @desc
 */
@ComponentScan(basePackages = "com.dfire")
@MapperScan(basePackages = "com.dfire.common.mapper")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = HeraBaseDaoTest.class)
public class HeraBaseDaoTest {

    @Autowired
    HeraJobActionService heraJobActionService;
    @Autowired
    HeraPermissionService heraPermissionService;
    @Autowired
    HeraDebugHistoryService heraDebugHistoryService;
    @Autowired
    HeraFileService heraFileService;
    @Autowired
    HeraGroupService heraGroupService;
    @Autowired
    HeraLockService heraLockService;
    @Autowired
    HeraHostRelationService heraHostRelationService;
    @Autowired
    HeraHostGroupService heraHostGroupService;
    @Autowired
    HeraJobService heraJobService;

    HeraAction heraAction;
    HeraPermission heraPermission;
    HeraDebugHistory heraDebugHistory;
    HeraFile heraFile;
    HeraGroup heraGroup;
    HeraLock heraLock;


    @Before
    public void doBefore() {
        heraAction = HeraAction.builder()
                .id("1111111111111111111")
                .jobId("6666")
                .gmtCreate(new Date())
                .gmtModified(new Date())
                .groupId(1)
                .name("lx")
                .owner("lx")
                .build();

        heraPermission = HeraPermission
                .builder()
                .gmtCreate(new Date())
                .build();

        heraDebugHistory = HeraDebugHistory
                .builder()
                .fileId("1")
                .hostGroupId(1)
                .owner("import")
                .gmtCreate(new Date())
                .gmtModified(new Date())
                .startTime(new Date())
                .endTime(new Date())
                .build();

        heraFile = HeraFile.builder()
                .build();

        heraGroup = HeraGroup.builder().build();
        heraLock = HeraLock.builder()
                .subgroup("test")
                .host("127.0.0.1")
                .serverUpdate(new Date())
                .build();


    }


    @Test
    public void heraActionDaoTest() {
        HeraAction action = HeraAction.builder().id("201801010010000350").jobId("350").build();
        System.out.println(heraAction.getGmtCreate());
        List<HeraAction> list = heraJobActionService.getAll();
        System.out.println(list.get(5).getJobDependencies());
        heraJobActionService.insert(heraAction);
        heraJobActionService.delete("1111111111111111111");

    }


    @Test
    public void heraPermissionDaoTest() {
        int id = heraPermissionService.insert(heraPermission);
        System.out.println(id);
        System.out.println(System.currentTimeMillis());
        heraPermission.setGmtModified(new Date());
        heraPermissionService.update(heraPermission);

        List<Integer> list = Arrays.asList(new Integer[]{new Integer(20), new Integer(12)});
        List<HeraPermission> permissions = heraPermissionService.findByIds(list);
        System.out.println(permissions.get(0).getGmtCreate());

        heraPermission.setId(19);
        heraPermissionService.delete("250");
        heraPermissionService.findById(heraPermission);
    }

    @Test
    public void heraDebugHistoryDaoTest() {
        String id = heraDebugHistoryService.insert(heraDebugHistory);
        System.out.printf(id);

        heraDebugHistory.setId("14");
        heraDebugHistory.setRunType("shell");
        heraDebugHistory.setExecuteHost("127.0.0.1");
        heraDebugHistory.setStatus("running");
        heraDebugHistory.setLog("hera running");
        heraDebugHistoryService.update(heraDebugHistory);

        List<HeraDebugHistory> list = heraDebugHistoryService.getAll();
        System.out.println(list.size());

        heraDebugHistory.setId("14");
        HeraDebugHistoryVo debugHistoryVo = heraDebugHistoryService.findById(heraDebugHistory);
        System.out.println(debugHistoryVo.getStatus());
        System.out.println(debugHistoryVo.getLog());
        heraDebugHistory.setId("12");
        heraDebugHistoryService.delete(13);

        heraDebugHistory.setId("14");
        HeraDebugHistory history = BeanConvertUtils.convert(debugHistoryVo);
        System.out.println(history.getLog());
        System.out.println(history.getStatus());

    }

    @Test
    public void heraFileDaoTest() {
        heraFile = heraFileService.findById("2");
        System.out.println(heraFile.getName());

        heraFile.setContent("test");
        heraFileService.update(heraFile);

        List<Integer> list = Arrays.asList(new Integer[]{new Integer(20), new Integer(12)});
        List<HeraFile> heraFileList = heraFileService.findByIds(list);
        System.out.println(heraFileList.size());

        heraFileService.delete("3");

        heraFile.setParent("3");
        List<HeraFile> subList = heraFileService.findByParent(heraFile);
        System.out.println(subList.size());

        List<HeraFile> pList = heraFileService.findByOwner("biadmin");
        System.out.println(pList.size());

        heraFile = HeraFile.builder().owner("test").name("test").type("2").build();
        heraFileService.insert(heraFile);

    }

    @Test
    public void heraGroupDaoTest() {
        heraGroup = heraGroupService.findById(3579);
        System.out.println(heraGroup.getConfigs());
        heraGroup.setConfigs("test");
        heraGroupService.update(heraGroup);

        HeraGroup group = heraGroupService.findById(3587);
        System.out.println(group.getConfigs());

        List<Integer> list = Arrays.asList(new Integer[]{new Integer(3607), new Integer(3608)});
        List<HeraGroup> groups = heraGroupService.findByIds(list);
        System.out.println(groups.get(0).getOwner());


        List<HeraGroup> subGroup = heraGroupService.findByParent(3578);
        System.out.println(subGroup.size());

        List<HeraGroup> userGroup = heraGroupService.findByOwner("biadmin");
        System.out.println(userGroup.size());

        heraGroupService.delete(3580);

    }

    @Test
    public void heraLockDaoTest() {
        HeraLock lock = heraLockService.findById("online");
        lock.setServerUpdate(new Date());
        heraLockService.update(lock);

        heraLockService.insert(heraLock);
    }

    @Test
    public void heraHostGroupDaoTest() {
        HeraHostGroup group = heraHostGroupService.findById(1);
        System.out.println(group.getName());

        Map<Integer, HeraHostGroupVo> map = heraHostGroupService.getAllHostGroupInfo();
        System.out.println(map.toString());

    }

    @Test
    public void heraJobDaoTest() {
        List<HeraJobTreeNodeVo> list = heraJobService.buildJobTree();
        System.out.println(list.size());
    }


}
