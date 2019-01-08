package com.dfire.common.service;

import com.dfire.common.entity.HeraFile;
import com.dfire.common.entity.vo.HeraFileTreeNodeVo;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 19:19 2018/1/12
 * @desc 开发中心文件文件管理
 */
public interface HeraFileService {


    Integer insert(HeraFile heraFile);

    int delete(Integer id);

    int update(HeraFile heraFile);

    List<HeraFile> getAll();

    HeraFile findById(Integer id);

    List<HeraFile> findByIds(List<Integer> list);

    List<HeraFile> findByParent(Integer parent);

    List<HeraFile> findByOwner(String heraFile);

    List<HeraFileTreeNodeVo> buildFileTree(String user);

    int updateContent(HeraFile heraFile);

    int updateFileName(HeraFile heraFile);

    HeraFile findDocByOwner(String owner);


    boolean updateParentById(Integer id, Integer parent);
}
