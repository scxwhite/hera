package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraFile;
import com.dfire.common.mapper.HeraFileMapper;
import com.dfire.common.service.HeraFileService;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 19:20 2018/1/12
 * @desc
 */
@Service("heraFileService")
public class HeraFileServiceImpl implements HeraFileService {


    @Autowired
    private HeraFileMapper heraFileMapper;

    @Override
    public List<HeraFile> getHeraFileListByOwner(String owner) {
        return heraFileMapper.getFileListByOwner(owner);
    }

    @Override
    public String buildFileTree(String owner) {
        List<HeraFile> list = getHeraFileListByOwner(owner);
        return "";

    }
}
