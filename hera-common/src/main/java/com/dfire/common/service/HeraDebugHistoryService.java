package com.dfire.common.service;

import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.vo.HeraDebugHistoryVo;
import com.dfire.common.mybatis.HeraInsertLangDriver;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import com.dfire.common.mybatis.HeraUpdateLangDriver;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午2:55 2018/4/16
 * @desc
 */
public interface HeraDebugHistoryService {

    String insert(HeraDebugHistory heraDebugHistory);

    int delete( int id);

    int update(HeraDebugHistory heraDebugHistory);

    List<HeraDebugHistory> getAll();

    HeraDebugHistoryVo findById(String id);

    List<HeraDebugHistory> findByFileId(String fileId);

    int updateStatus(HeraDebugHistory heraDebugHistory);

    int updateLog(HeraDebugHistory heraDebugHistory);


    HeraDebugHistory findLogById(Integer id);
}
