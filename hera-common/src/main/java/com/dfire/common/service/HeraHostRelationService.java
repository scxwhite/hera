package com.dfire.common.service;

import com.dfire.common.entity.HeraHostRelation;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:07 2018/1/12
 * @desc
 */
public interface HeraHostRelationService {

    int insert(HeraHostRelation heraHostRelation);

    int delete(int id);

    int update(HeraHostRelation heraHostRelation);

    List<HeraHostRelation> getAll();

    HeraHostRelation findById(HeraHostRelation heraHostRelation);

    List<String> findPreemptionGroup(int id);
}
