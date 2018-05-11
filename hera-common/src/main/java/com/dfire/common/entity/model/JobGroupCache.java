package com.dfire.common.entity.model;

import com.dfire.common.entity.vo.HeraJobVo;
import com.dfire.common.kv.Tuple;
import com.dfire.common.service.HeraGroupService;
import com.dfire.common.vo.JobStatus;
import lombok.Builder;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午10:37 2018/5/6
 * @desc
 */
@Builder
public class JobGroupCache {

    private final String jobId;
    private HeraJobVo heraJobVo;

    private HeraGroupService heraGroupService;

    public HeraJobVo getHeraJobVo() {
        if(heraJobVo == null) {
            Tuple<HeraJobVo, JobStatus> jobStatusTuple = heraGroupService.getHeraJobVo(jobId);
            if(jobStatusTuple != null) {
                heraJobVo = jobStatusTuple.getSource();
            } else {
                heraJobVo = null;
            }
        }
        return heraJobVo;
    }

    public void refresh() {
        Tuple<HeraJobVo, JobStatus> jobStatusTuple = heraGroupService.getHeraJobVo(jobId);
        if(jobStatusTuple != null) {
            heraJobVo = jobStatusTuple.getSource();
        } else {
            heraJobVo = null;
        }
    }
}
