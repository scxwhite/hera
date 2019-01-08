package com.dfire.common.service.impl;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraFile;
import com.dfire.common.entity.vo.HeraFileTreeNodeVo;
import com.dfire.common.mapper.HeraFileMapper;
import com.dfire.common.service.HeraFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 19:20 2018/1/12
 * @desc
 */
@Service("heraFileService")
public class HeraFileServiceImpl implements HeraFileService {

    @Autowired
    protected HeraFileMapper heraFileMapper;

    @Override
    public Integer insert(HeraFile heraFile) {
        heraFileMapper.insert(heraFile);
        return heraFile.getId();
    }

    @Override
    public int delete(Integer id) {
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
    public HeraFile findById(Integer id) {
        return heraFileMapper.findById(id);
    }

    @Override
    public List<HeraFile> findByIds(List<Integer> list) {
        return heraFileMapper.findByIds(list);
    }

    @Override
    public List<HeraFile> findByParent(Integer parent) {
        return heraFileMapper.findByParent(parent);
    }

    @Override
    public List<HeraFile> findByOwner(String owner) {
        return heraFileMapper.findByOwner(owner);
    }

    /**
     * 构建开发中心文件树
     *
     * @param user
     * @return
     */
    @Override
    public List<HeraFileTreeNodeVo> buildFileTree(String user) {
        List<HeraFile> fileVoList = this.findByOwner(user);
        fileVoList.addAll(this.findByOwner(Constants.FILE_ALL_NAME));
        return fileVoList.parallelStream().map(file -> {
            HeraFileTreeNodeVo vo = HeraFileTreeNodeVo.builder().id(file.getId()).name(file.getName()).build();
            if (file.getParent() == null) {
                vo.setParent(-1);
            } else {
                vo.setParent(file.getParent());
            }
            if (file.getType() == 1) {
                vo.setIsParent(true);
            } else if (file.getType() == 2) {
                vo.setIsParent(false);
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public int updateContent(HeraFile heraFile) {
        return heraFileMapper.updateContent(heraFile);
    }

    @Override
    public int updateFileName(HeraFile heraFile) {
        return heraFileMapper.updateFileName(heraFile);
    }

    @Override
    public HeraFile findDocByOwner(String owner) {
        return heraFileMapper.findDocByOwner(owner);
    }

    @Override
    public boolean updateParentById(Integer id, Integer parent) {
        Integer update = heraFileMapper.updateParentById(id, parent);
        return update != null && update > 0;
    }

}
