package com.dfire.common.service.impl;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraJobMonitor;
import com.dfire.common.entity.HeraSso;
import com.dfire.common.entity.vo.HeraJobMonitorVo;
import com.dfire.common.mapper.HeraJobMonitorMapper;
import com.dfire.common.service.HeraJobMonitorService;
import com.dfire.common.service.HeraSsoService;
import com.dfire.common.util.ActionUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author xiaosuda
 * @date 2018/8/1
 */
@Service
public class HeraJobMonitorServiceImpl implements HeraJobMonitorService {

    @Autowired
    private HeraJobMonitorMapper heraJobMonitorMapper;

    @Autowired
    private HeraSsoService heraSsoService;

    @Override
    public boolean addMonitor(String userId, Integer jobId) {
        HeraJobMonitor res = heraJobMonitorMapper.findByJobId(jobId);
        HeraJobMonitor monitor = new HeraJobMonitor();
        monitor.setUserIds(userId.endsWith(Constants.COMMA) ? userId : userId + Constants.COMMA);
        monitor.setJobId(jobId);
        //插入
        if (res == null) {
            Integer insert = heraJobMonitorMapper.insert(monitor);
            return insert != null && insert > 0;
        } else { //更新
            Integer update = heraJobMonitorMapper.insertUser(monitor);
            return update != null && update > 0;
        }
    }

    @Override
    public boolean removeMonitor(String userId, Integer jobId) {
        HeraJobMonitor oldMonitor = heraJobMonitorMapper.findByJobId(jobId);
        StringBuilder newMonitor = new StringBuilder();
        Arrays.stream(oldMonitor.getUserIds().split(Constants.COMMA)).forEach(id -> {
            if (!id.equals(userId)) {
                newMonitor.append(id).append(Constants.COMMA);
            }
        });
        Integer res = heraJobMonitorMapper.update(jobId, newMonitor.toString());
        return res != null && res > 0;
    }

    @Override
    public HeraJobMonitor findByJobId(Integer jobId) {
        return heraJobMonitorMapper.findByJobId(jobId);
    }

    @Override
    public Set<HeraSso> getMonitorUser(Integer jobId) {
        Set<HeraSso> monitorUser = new HashSet<>();
        Optional.ofNullable(this.findByJobId(jobId))
                .map(HeraJobMonitor::getUserIds)
                .ifPresent(ids -> Arrays.stream(ids.split(Constants.COMMA))
                        .filter(StringUtils::isNotBlank)
                        .forEach(id -> {
                            Optional.ofNullable(heraSsoService.findSsoById(Integer.parseInt(id)))
                                    .ifPresent(monitorUser::add);
                        }));
        return monitorUser;
    }

    @Override
    public List<HeraJobMonitor> findAll() {
        return heraJobMonitorMapper.selectAll();
    }

    @Override
    public List<HeraJobMonitorVo> findAllVo() {
        return heraJobMonitorMapper.selectAllVo();
    }

    @Override
    public boolean updateMonitor(String userIds, Integer jobId) {
        if (StringUtils.isNotBlank(userIds)) {
            userIds = userIds.endsWith(Constants.COMMA) ? userIds : userIds + Constants.COMMA;
        }
        Integer update = heraJobMonitorMapper.update(jobId, userIds);
        return update != null && update > 0;
    }

    @Override
    public List<Integer> findBySsoId(Integer ssoId) {
        return heraJobMonitorMapper.selectByUser(ssoId);
    }
}
