package com.dfire.common.mapper;

import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.vo.JobStatus;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:08 2018/4/23
 * @desc
 */
public interface HeraJobHistoryMapper {


    @Select("select * from hera_job_history where job_id = #{jobId}")
    @Results({
            @Result(id=true, column="id", property = "id"),
            @Result(column="gmt_create", property = "gmtCreate")
    })
    public HeraJobHistory findJobHistory(String jobId);

    public void addHeraJobHistory(HeraJobHistory heraJobHistory);

    public void updateHeraJobHistory(HeraJobHistory heraJobHistory);

    //任务链路的任务状态也需要更新
    public void updateJobStatus(JobStatus jobStatus);
}
