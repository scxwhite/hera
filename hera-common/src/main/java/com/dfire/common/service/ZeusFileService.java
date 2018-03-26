package com.dfire.common.service;

import com.dfire.common.entity.ZeusFile;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 19:19 2018/1/12
 * @desc 开发中心文件文件管理
 */
public interface ZeusFileService {

    List<ZeusFile> getFileListByOwner(String owner);
}
