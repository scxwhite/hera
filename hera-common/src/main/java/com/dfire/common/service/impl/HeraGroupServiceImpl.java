package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraAction;
import com.dfire.common.entity.HeraGroup;
import com.dfire.common.entity.model.HeraGroupBean;
import com.dfire.common.entity.model.HeraJobBean;
import com.dfire.common.entity.vo.HeraJobVo;
import com.dfire.common.kv.Tuple;
import com.dfire.common.mapper.HeraGroupMapper;
import com.dfire.common.service.HeraGroupService;
import com.dfire.common.vo.JobStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:02 2018/4/17
 * @desc
 */
@Service("heraGroupService")
public class HeraGroupServiceImpl implements HeraGroupService {

    @Autowired
    private HeraGroupMapper heraGroupMapper;

    @Override
    public HeraGroupBean getGlobalGroup() {
        return HeraGroupBean.builder().build();
    }

    @Override
    public Tuple<HeraJobVo, JobStatus> getHeraJobVo(String jobId) {
        return null;
    }

    @Override
    public JobStatus getJobStatus(String jobId) {
        return null;
    }

    @Override
    public HeraJobBean getUpstreamJobBean(String jobId) {
        return null;
    }

    @Override
    public void updateJobStatus(JobStatus heraJobVo) {

    }

    @Override
    public void updateJob(HeraJobVo heraJobVo) {

    }

    @Override
    public void removeJob(String jobId) {

    }

    @Override
    public List<HeraAction> getAllAction() {
        return null;
    }

    @Override
    public void saveJobAction(HeraAction action) {

    }

    @Override
    public List<HeraAction> getLastJobAction(String dp) {
        return null;
    }
}
