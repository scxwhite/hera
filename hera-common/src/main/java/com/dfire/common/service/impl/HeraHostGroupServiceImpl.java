package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraHostGroup;
import com.dfire.common.entity.HeraHostRelation;
import com.dfire.common.mapper.HeraHostGroupMapper;
import com.dfire.common.service.HeraHostRelationService;
import com.dfire.common.entity.vo.HeraHostGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import com.dfire.common.service.HeraHostGroupService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 22:26 2018/1/10
 * @desc
 */
@Service("heraHostGroupService")
public class HeraHostGroupServiceImpl implements HeraHostGroupService {


    @Autowired
    private HeraHostGroupMapper heraHostGroupMapper;
    @Autowired
    private HeraHostRelationService heraHostRelationService;

    @Override
    public int insert(HeraHostGroup heraHostGroup) {
        return heraHostGroupMapper.insert(heraHostGroup);
    }

    @Override
    public int delete(int id) {
        return heraHostGroupMapper.delete(id);
    }

    @Override
    public int update(HeraHostGroup heraHostGroup) {
        return heraHostGroupMapper.update(heraHostGroup);
    }

    @Override
    public List<HeraHostGroup> getAll() {
        return heraHostGroupMapper.getAll();
    }

    @Override
    public HeraHostGroup findById(int id) {
        HeraHostGroup group = HeraHostGroup.builder().id(id).build();
        return heraHostGroupMapper.findById(group);
    }

    @Override
    public Map<Integer, HeraHostGroupVo> getAllHostGroupInfo() {
        List<HeraHostGroup> groupList = this.getAll();
        Map<Integer, HeraHostGroupVo> hostGroupInfoMap = new HashMap<>(groupList.size());
        List<HeraHostRelation> relationList = heraHostRelationService.getAll();
        groupList.forEach(heraHostGroup -> {
            if(heraHostGroup.getEffective() == 1) {
                HeraHostGroupVo vo = HeraHostGroupVo.builder()
                        .id(String.valueOf(heraHostGroup.getId()))
                        .name(heraHostGroup.getName())
                        .nextPos(0)
                        .description(heraHostGroup.getDescription())
                        .build();
                List<String> hosts = new ArrayList<>();
                relationList.forEach(heraHostRelation -> {
                    if(heraHostRelation.getHostGroupId() == (heraHostGroup.getId())) {
                        hosts.add(heraHostRelation.getHost());
                    }
                });
                vo.setHosts(hosts);
                hostGroupInfoMap.put(heraHostGroup.getId(), vo);
            }
        });
        return hostGroupInfoMap;
    }
}
