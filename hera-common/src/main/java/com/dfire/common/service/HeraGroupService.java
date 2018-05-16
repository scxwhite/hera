package com.dfire.common.service;

import com.dfire.common.entity.HeraAction;
import com.dfire.common.entity.model.HeraGroupBean;
import com.dfire.common.entity.model.HeraJobBean;
import com.dfire.common.entity.vo.HeraJobVo;
import com.dfire.common.kv.Tuple;
import com.dfire.common.vo.JobStatus;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:01 2018/4/17
 * @desc
 */
public interface HeraGroupService {

    HeraGroupBean getGlobalGroup();

    Tuple<HeraJobVo, JobStatus> getHeraJobVo(int jobId);

    JobStatus getJobStatus(String jobId);

    HeraJobBean getUpstreamJobBean(String jobId);


    void updateJobStatus(JobStatus heraJobVo);

    void updateJob(HeraJobVo heraJobVo);

    void removeJob(String jobId);

    List<HeraAction> getAllAction();

    void saveJobAction(HeraAction action);

    List<HeraAction> getLastJobAction(String dp);
}
