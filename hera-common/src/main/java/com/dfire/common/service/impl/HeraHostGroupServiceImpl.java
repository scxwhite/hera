package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraHostGroup;
import com.dfire.common.entity.HeraHostRelation;
import com.dfire.common.mapper.HeraHostGroupMapper;
import com.dfire.common.service.HeraHostRelationService;
import com.dfire.common.vo.HeraHostGroupVo;
import com.dfire.common.vo.RestfulResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@Service("hostGroupServiceImpl")
@PropertySource(value = "classpath:config.yml", ignoreResourceNotFound = true)
@ConfigurationProperties("hera")
public class HeraHostGroupServiceImpl implements HeraHostGroupService {

    private String defaultWorkerGroup;

    private String preemptionMasterGroup;

    @Autowired
    private HeraHostGroupMapper heraHostGroupMapper;
    @Autowired
    private HeraHostRelationService heraHostRelationService;

    public List<String> getPreemptionGroup(String preemptionMasterGroup) {
        return heraHostGroupMapper.getPreemptionGroup(preemptionMasterGroup);
    }

    @Override
    public List<HeraHostGroup> getAllHostGroupList() {
        return heraHostGroupMapper.getAllHostGroupList();
    }

    @Override
    public Map<String, HeraHostGroupVo> getAllHostGroupInfo() {
        Map<String, HeraHostGroupVo> hostGroupInfoMap = new HashMap<>();
        List<HeraHostGroup> groupList = getAllHostGroupList();
        List<HeraHostRelation> relationList = heraHostRelationService.getAllHostRelationList();
        groupList.forEach(heraHostGroup -> {
            if(heraHostGroup.getEffective() == 1) {
                HeraHostGroupVo vo = HeraHostGroupVo.builder()
                        .id(heraHostGroup.getId().toString())
                        .currentPosition(0)
                        .name(heraHostGroup.getName())
                        .description(heraHostGroup.getDescription())
                        .build();
                List<String> hosts = new ArrayList<>();
                relationList.forEach(heraHostRelation -> {
                    if(heraHostRelation.getHostGroupId().equals(heraHostGroup.getId())) {
                        hosts.add(heraHostRelation.getHost());
                    }
                });
                vo.setHosts(hosts);
                hostGroupInfoMap.put(heraHostGroup.getId().toString(), vo);
            }
        });
        return hostGroupInfoMap;
    }

    @Override
    public Boolean deleteById(Integer id) {
        Integer x = heraHostGroupMapper.deleteHostGroup(id);
        if (x == null) {
            throw new RuntimeException("数据库操作异常");
        }
        return x > 0;
    }

    @Override
    public Boolean addHostGroup(HeraHostGroup hostGroup) {
        Integer x = heraHostGroupMapper.insertHostGroup(hostGroup);
        if (x == null) {
            throw new RuntimeException("数据库操作异常");
        }
        return x > 0;
    }

    @Override
    public Boolean updateHostGroup(HeraHostGroup hostGroup) {
        Integer x = heraHostGroupMapper.updateHostGroup(hostGroup);
        if (x == null) {
            throw new RuntimeException("数据库操作异常");
        }
        return x > 0;
    }

    @Override
    public RestfulResponse saveOrUpdate(HeraHostGroup heraHostGroup) {
        Boolean res;
        if (heraHostGroup.getId() == null) {
            res = this.addHostGroup(heraHostGroup);
            return new RestfulResponse(res, res ? "插入成功" : "插入失败");
        }
        res = this.updateHostGroup(heraHostGroup);
        return new RestfulResponse(res, res ? "更新成功" : "更新失败");
    }
}
