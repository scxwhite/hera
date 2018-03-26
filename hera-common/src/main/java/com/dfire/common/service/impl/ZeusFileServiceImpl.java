package com.dfire.common.service.impl;

import com.dfire.common.entity.ZeusFile;
import com.dfire.common.mapper.ZeusFileMapper;
import com.dfire.common.service.ZeusFileService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 19:20 2018/1/12
 * @desc
 */
@Service("zeusFileService")
public class ZeusFileServiceImpl implements ZeusFileService {

    @Autowired
    private ZeusFileMapper zeusFileMapper;

    @Override
    public List<ZeusFile> getFileListByOwner(String owner) {
        return zeusFileMapper.getFileListByOwner(owner);
    }
}
