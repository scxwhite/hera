package com.dfire.common.service;

import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.common.entity.vo.HeraDebugHistoryVo;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午2:55 2018/4/16
 * @desc
 */
public interface HeraDebugHistoryService {

    Long insert(HeraDebugHistory heraDebugHistory);

    int delete(Long id);

    int update(HeraDebugHistory heraDebugHistory);

    List<HeraDebugHistory> getAll();

    HeraDebugHistoryVo findById(Long id);

    List<HeraDebugHistory> findByFileId(Integer fileId);

    int updateStatus(HeraDebugHistory heraDebugHistory);

    int updateLog(HeraDebugHistory heraDebugHistory);


    HeraDebugHistory findLogById(Long id);

    void updateStatus(Long id, String msg, String status);
}
