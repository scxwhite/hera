package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraFile;
import com.dfire.common.mapper.HeraFileMapper;
import com.dfire.common.service.HeraFileService;

import java.util.List;

import com.dfire.common.service.HeraUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 19:20 2018/1/12
 * @desc
 */
@Service("heraFileService")
public class HeraFileServiceImpl implements HeraFileService {

   static String PERSONAL = "个人文档";
   static String SHARE = "共享文档";

    static String FILE = "2";
    static String FOLDER = "1";

    @Autowired
    private HeraFileMapper heraFileMapper;

    @Autowired
    private HeraUserService heraUserService;


    @Override
    public int insert(HeraFile heraFile) {
        return heraFileMapper.insert(heraFile);
    }

    @Override
    public int delete(String id) {
        return heraFileMapper.delete(id);
    }

    @Override
    public int update(HeraFile heraFile) {
        return heraFileMapper.update(heraFile);
    }

    @Override
    public List<HeraFile> getAll() {
        return heraFileMapper.getAll();
    }

    @Override
    public HeraFile findById(String id) {
        HeraFile heraFile = HeraFile.builder().id(id).build();
        return heraFileMapper.findById(heraFile);
    }

    @Override
    public List<HeraFile> findByIds(List<Integer> list) {
        return heraFileMapper.findByIds(list);
    }

    @Override
    public List<HeraFile> findByParent(HeraFile heraFile) {
        return heraFileMapper.findByParent(heraFile);
    }

    @Override
    public List<HeraFile> findByOwner(String owner) {
        HeraFile heraFile = HeraFile.builder().owner(owner).build();
        return heraFileMapper.findByOwner(heraFile);
    }

}
