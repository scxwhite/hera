package com.dfire.common.service;

import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.vo.HeraFileTreeNodeVo;
import com.dfire.common.entity.vo.HeraJobTreeNodeVo;
import com.dfire.common.vo.RestfulResponse;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 2:08 2018/1/11
 * @desc
 */
public interface HeraJobService {

    int insert(HeraJob heraJob);

    int delete(int id);

    Integer update(HeraJob heraJob);

    List<HeraJob> getAll();

    HeraJob findById(int id);

    List<HeraJob> findByIds(List<Integer> list);

    List<HeraJob> findByPid(int groupId);

    /**
     * 构建job树形目录结构
     *
     * @return
     */
    List<HeraJobTreeNodeVo> buildJobTree();

    boolean changeSwitch(Integer id);

    RestfulResponse checkAndUpdate(HeraJob heraJob);

}
