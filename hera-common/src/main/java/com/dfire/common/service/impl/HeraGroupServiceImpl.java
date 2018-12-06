package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraGroup;
import com.dfire.common.entity.model.HeraGroupBean;
import com.dfire.common.entity.model.HeraJobBean;
import com.dfire.common.entity.vo.HeraActionVo;
import com.dfire.common.kv.Tuple;
import com.dfire.common.mapper.HeraGroupMapper;
import com.dfire.common.service.HeraGroupService;
import com.dfire.common.service.HeraJobActionService;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.common.vo.JobStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:02 2018/4/17
 * @desc
 */
@Service("heraGroupService")
public class HeraGroupServiceImpl implements HeraGroupService {

    @Autowired
    protected HeraGroupMapper heraGroupMapper;

    @Autowired
    private HeraJobActionService heraJobActionService;

    @Override
    public HeraGroup getRootGroup() {
        return this.findById(-1);
    }

    @Override
    public HeraJobBean getUpstreamJobBean(String actionId) {
        Tuple<HeraActionVo, JobStatus> tuple = heraJobActionService.findHeraActionVo(actionId);
        if (tuple != null) {
            HeraJobBean jobBean = HeraJobBean.builder()
                    .heraActionVo(tuple.getSource())
                    .jobStatus(tuple.getTarget())
                    .build();
            jobBean.setGroupBean(getUpstreamGroupBean(tuple.getSource().getGroupId()));
            return jobBean;
        }
        return null;
    }

    private HeraGroupBean getUpstreamGroupBean(Integer groupId) {
        HeraGroup heraGroup = this.findById(groupId);
        HeraGroupBean result = HeraGroupBean.builder()
                .groupVo(BeanConvertUtils.convert(heraGroup))
                .build();
        if (heraGroup != null && heraGroup.getParent() != null) {
            HeraGroupBean parentGroupBean = getUpstreamGroupBean(heraGroup.getParent());
            result.setParentGroupBean(parentGroupBean);
        }
        return result;
    }


    @Override
    public int insert(HeraGroup heraFile) {
        return heraGroupMapper.insert(heraFile);
    }

    @Override
    public int delete(int id) {
        return heraGroupMapper.delete(id);
    }

    @Override
    public int update(HeraGroup heraFile) {
        return heraGroupMapper.update(heraFile);
    }

    @Override
    public List<HeraGroup> getAll() {
        return heraGroupMapper.getAll();
    }

    @Override
    public HeraGroup findById(Integer id) {
        return heraGroupMapper.findById(id);
    }

    @Override
    public List<HeraGroup> findByIds(List<Integer> list) {
        return heraGroupMapper.findByIds(list);
    }

    @Override
    public List<HeraGroup> findByParent(Integer parentId) {
        return heraGroupMapper.findByParent(parentId);
    }

    @Override
    public List<HeraGroup> findByOwner(String owner) {
        return heraGroupMapper.findByOwner(owner);
    }

    @Override
    public HeraGroup findConfigById(Integer id) {
        return heraGroupMapper.selectConfigById(id);
    }
}
