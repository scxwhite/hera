package com.dfire.common.entity.model;

import com.dfire.common.entity.vo.HeraActionVo;
import com.dfire.common.entity.vo.HeraJobVo;
import com.dfire.common.kv.Tuple;
import com.dfire.common.service.HeraGroupService;
import com.dfire.common.service.HeraJobActionService;
import com.dfire.common.vo.JobStatus;
import lombok.Builder;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午10:37 2018/5/6
 * @desc
 */
@Builder
public class JobGroupCache {

    //版本号id
    private final String jobId;
    private HeraActionVo heraActionVo;

    private HeraJobActionService heraJobActionService;

    public HeraActionVo getHeraActionVo() {
        if(heraActionVo == null) {
            Tuple<HeraActionVo, JobStatus> jobStatusTuple = heraJobActionService.findHeraActionVo(jobId);
            if(jobStatusTuple != null) {
                heraActionVo = jobStatusTuple.getSource();
            } else {
                heraActionVo = null;
            }
        }
        return heraActionVo;
    }

    public void refresh() {
        Tuple<HeraActionVo, JobStatus> jobStatusTuple = heraJobActionService.findHeraActionVo(jobId);
        if(jobStatusTuple != null) {
            heraActionVo = jobStatusTuple.getSource();
        } else {
            heraActionVo = null;
        }
    }
}
