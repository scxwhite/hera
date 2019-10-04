package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraSso;
import com.dfire.common.entity.vo.HeraSsoVo;
import com.dfire.common.mapper.HeraSsoMapper;
import com.dfire.common.service.HeraSsoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * desc:
 *
 * @author scx
 * @create 2019/06/10
 */
@Service
public class HeraSsoServiceImpl implements HeraSsoService {


    @Autowired
    private HeraSsoMapper heraSsoMapper;

    @Override
    public boolean addSso(HeraSso heraSso) {
        heraSso.setGmtModified(System.currentTimeMillis());
        heraSso.setIsValid(0);
        Integer insert = heraSsoMapper.insert(heraSso);
        return insert != null && insert > 0;
    }

    @Override
    public List<HeraSso> getAll() {
        return heraSsoMapper.selectAll();
    }

    @Override
    public boolean deleteSsoById(Integer id) {
        Integer delete = heraSsoMapper.delete(id);
        return delete != null && delete > 0;
    }

    @Override
    public boolean updateHeraSsoById(HeraSso heraSso) {
        heraSso.setGmtModified(System.currentTimeMillis());
        Integer update = heraSsoMapper.update(heraSso);
        return update != null && update > 0;
    }

    @Override
    public HeraSso findSsoById(Integer id) {
        return heraSsoMapper.findById(id);
    }

    @Override
    public HeraSsoVo findSsoVoById(Integer id) {
        return heraSsoMapper.findSsoVoById(id);
    }

    @Override
    public HeraSso findSsoByName(String name) {
        return heraSsoMapper.findByName(name);
    }

    @Override
    public boolean checkByName(String name) {
        Integer exist = heraSsoMapper.checkExistByName(name);
        return exist != 0 && exist >= 1;
    }

    @Override
    public boolean setValid(Integer id, Integer val) {
        Integer integer = heraSsoMapper.updateValid(id, val, System.currentTimeMillis());
        return integer != null && integer > 0;
    }
}