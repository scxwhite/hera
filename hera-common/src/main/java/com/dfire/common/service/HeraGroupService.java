package com.dfire.common.service;

import com.dfire.common.entity.HeraGroup;
import com.dfire.common.entity.model.HeraGroupBean;
import com.dfire.common.entity.model.HeraJobBean;
import com.dfire.common.entity.vo.HeraJobVo;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:01 2018/4/17
 * @desc
 */
public interface HeraGroupService {

    HeraGroupBean getGlobalGroup();

    HeraJobBean getUpstreamJobBean(String jobId);

    void updateJob(HeraJobVo heraJobVo);


    int insert(HeraGroup heraGroup);

    int delete(int id);

    int update(HeraGroup heraGroup);

    List<HeraGroup> getAll();

    HeraGroup findById(int id);

    List<HeraGroup> findByIds(List<Integer> list);

    List<HeraGroup> findByParent(int parentId);

    List<HeraGroup> findByOwner(String owner);

}
