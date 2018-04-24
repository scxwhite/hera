package com.dfire.common.mapper;

import com.dfire.common.entity.HeraJobHistory;
import org.apache.ibatis.annotations.Select;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:08 2018/4/23
 * @desc
 */
public interface HeraJobHistoryMapper {


    @Select("select * from hera_job_history where job_id = #{jobId}")
    public HeraJobHistory findJobHistory(String jobId);
}
