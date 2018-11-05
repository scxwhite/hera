package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraFile;
import com.dfire.common.entity.vo.HeraFileTreeNodeVo;
import com.dfire.common.mapper.HeraFileMapper;
import com.dfire.common.service.HeraFileService;
import org.apache.commons.lang.StringUtils;
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

   static String PERSONAL = "个人文档";
   static String SHARE = "共享文档";

    static String FILE = "2";
    static String FOLDER = "1";

    @Autowired
    private HeraFileMapper heraFileMapper;

    @Override
    public String insert(HeraFile heraFile) {
         heraFileMapper.insert(heraFile);
         return heraFile.getId();
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

    /**
     * 构建开发中心文件树
     *
     * @param user
     * @return
     */
    @Override
    public List<HeraFileTreeNodeVo> buildFileTree(String user) {
        List<HeraFile> fileVoList = this.findByOwner(user);
        return fileVoList.stream().map(file -> {
            HeraFileTreeNodeVo vo = HeraFileTreeNodeVo.builder().id(file.getId()).name(file.getName()).build();
            if (file.getParent() == null || StringUtils.isBlank(file.getParent())) {
                vo.setParent("root");
            } else {
                vo.setParent(file.getParent());
            }
            if (file.getType().equals("1")) {
                vo.setIsParent(true);
            } else if (file.getType().equals("2")) {
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

}
