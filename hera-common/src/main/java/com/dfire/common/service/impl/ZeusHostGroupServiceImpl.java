package com.dfire.common.service.impl;

import com.dfire.common.entity.ZeusHostGroup;
import com.dfire.common.entity.ZeusHostRelation;
import com.dfire.common.mapper.ZeusHostGroupMapper;
import com.dfire.common.service.ZeusHostRelationService;
import com.dfire.common.vo.ZeusHostGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import com.dfire.common.service.ZeusHostGroupService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 22:26 2018/1/10
 * @desc
 */
@Service("hostGroupServiceImpl")
@PropertySource(value = "classpath:application.yml", ignoreResourceNotFound = true)
@ConfigurationProperties("zeus") //// prefix zeus, find app.* values
public class ZeusHostGroupServiceImpl implements ZeusHostGroupService {

    @Value("${defaultWorkerGroupId}")
    private String defaultWorkerGroup;
    @Value("${preemptionMasterGroupId}")
    private String preemptionMasterGroup;

    @Autowired
    private ZeusHostGroupMapper zeusHostGroupMapper;
    @Autowired
    private ZeusHostRelationService zeusHostRelationService;

    public List<String> getPreemptionGroup(String preemptionMasterGroup) {
        return zeusHostGroupMapper.getPreemptionGroup(preemptionMasterGroup);
    }


    public List<ZeusHostGroup> getAllHostGroupList() {
        return zeusHostGroupMapper.getAllHostGroupList();
    }

    @Override
    public Map<String, ZeusHostGroupVo> getAllHostGroupInfo() {
        Map<String, ZeusHostGroupVo> hostGroupInfoMap = new HashMap<>();
        List<ZeusHostGroup> groupList = getAllHostGroupList();
        List<ZeusHostRelation> relationList = zeusHostRelationService.getAllHostRelationList();
        groupList.forEach(zeusHostGroup -> {
            if(zeusHostGroup.getEffective() == 1) {
                ZeusHostGroupVo vo = ZeusHostGroupVo.builder()
                        .id(zeusHostGroup.getId().toString())
                        .currentPosition(0)
                        .name(zeusHostGroup.getName())
                        .description(zeusHostGroup.getDescription())
                        .build();
                List<String> hosts = new ArrayList<>();
                relationList.forEach(zeusHostRelation -> {
                    if(zeusHostRelation.getHostGroupId().equals(zeusHostGroup.getId())) {
                        hosts.add(zeusHostRelation.getHost());
                    }
                });
                vo.setHosts(hosts);
                hostGroupInfoMap.put(zeusHostGroup.getId().toString(), vo);
            }
        });
        return hostGroupInfoMap;
    }
}
