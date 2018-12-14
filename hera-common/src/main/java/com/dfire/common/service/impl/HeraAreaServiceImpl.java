package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraArea;
import com.dfire.common.mapper.HeraAreaMapper;
import com.dfire.common.service.HeraAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xiaosuda
 * @date 2018/12/14
 */
@Service("heraAreaServiceImpl")
public class HeraAreaServiceImpl implements HeraAreaService {


    @Autowired
    private HeraAreaMapper heraAreaMapper;

    @Override
    public Integer add(HeraArea heraArea) {
        return heraAreaMapper.insert(heraArea);
    }

    @Override
    public HeraArea findById(Integer id) {
        return heraAreaMapper.selectById(id);
    }

    @Override
    public List<HeraArea> findByIdList(List<Integer> idList) {
        return heraAreaMapper.selectByIdList(idList);
    }

    @Override
    public Integer updateById(HeraArea heraArea) {
        return heraAreaMapper.updateById(heraArea);
    }

    @Override
    public Integer deleteById(Integer id) {
        return heraAreaMapper.deleteById(id);
    }

    @Override
    public List<HeraArea> findAll() {
        return heraAreaMapper.selectAll();
    }
}
