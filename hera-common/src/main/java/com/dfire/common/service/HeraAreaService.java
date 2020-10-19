package com.dfire.common.service;

import com.dfire.common.entity.HeraArea;

import java.util.List;

/**
 * @author xiaosuda
 * @date 2018/12/14
 */
public interface HeraAreaService {

    Integer add(HeraArea heraArea);

    HeraArea findById(Integer id);

    List<HeraArea> findByIdList(List<Integer> idList);

    Integer updateById(HeraArea heraArea);

    Integer deleteById(Integer id);

    List<HeraArea> findAll();


}
