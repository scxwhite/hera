package com.dfire.common.service;

import com.dfire.common.entity.HeraAction;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:41 2018/5/16
 * @desc
 */
public interface HeraJobActionService {

    int insert(HeraAction heraAction);

    int delete(String id);

    int update(HeraAction heraJobHistory);

    List<HeraAction> getAll();

    public HeraAction findById(HeraAction heraAction);

    public HeraAction findByJobId(HeraAction heraAction);

}
