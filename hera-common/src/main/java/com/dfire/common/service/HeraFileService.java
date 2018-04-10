package com.dfire.common.service;

import com.dfire.common.entity.HeraFile;
import com.dfire.common.entity.vo.HeraFileVo;
import com.dfire.common.tree.HeraFileTreeNode;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 19:19 2018/1/12
 * @desc 开发中心文件文件管理
 */
public interface HeraFileService {


    List<HeraFileVo> getSubHeraFiles(String owner);

    public List<HeraFileVo> getUserFiles(String owner);


    //FileManagerService
    HeraFileVo addHeraFile(HeraFile heraFile );

    public void deleteHeraFile(String fileId) ;

    void updateHeraFileContent(String fileId, String content);

    void updateHeraFileName(String fileId, String name);

    public HeraFile getHeraFile(String id);

    public  HeraFileTreeNode getUserFiles();

    void moveHeraFile(String sourceId,String targetId);

    List<HeraFileVo> getCommonFiles(HeraFileVo fm);

    void updateHostGroupId(String fileId, String hostGroupId);

}
