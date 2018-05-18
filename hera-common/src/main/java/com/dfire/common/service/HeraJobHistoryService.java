package com.dfire.common.service;

import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.mybatis.HeraInsertLangDriver;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import com.dfire.common.mybatis.HeraUpdateLangDriver;
import com.dfire.common.vo.JobStatus;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 19:18 2018/1/12
 * @desc
 */
public interface HeraJobHistoryService {

    public HeraJobHistoryVo findJobHistory(String id);

    public void addHeraJobHistory(HeraJobHistory heraJobHistory);

    public void updateHeraJobHistory(HeraJobHistory heraJobHistory);

    public void updateJobStatus(JobStatus jobStatus);


    int insert(HeraJobHistory heraJobHistory);

    int delete(String id);

    int update(HeraJobHistory heraJobHistory);

    List<HeraJobHistory> getAll();

    public HeraJobHistory findById(String id);


}
