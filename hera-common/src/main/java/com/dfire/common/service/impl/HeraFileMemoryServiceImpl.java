package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraFile;
import com.dfire.common.entity.Judge;
import com.dfire.logs.HeraLog;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xiaosuda
 * @date 2018/12/5
 */
@Service("heraFileMemoryService")
public class HeraFileMemoryServiceImpl extends HeraFileServiceImpl {


    private volatile Judge judge;

    private Map<Integer, HeraFile> memoryJob;

    private Map<Integer, HeraFile> getMemoryJob() {
        Judge newJudge = heraFileMapper.selectTableInfo();
        if (newJudge.getMaxId() == null || newJudge.getLastModified() == null || newJudge.getCount() == null) {
            return new HashMap<>(0);
        }
        if (judge == null || !newJudge.getCount().equals(judge.getCount()) || !newJudge.getLastModified().equals(judge.getLastModified()) || !newJudge.getMaxId().equals(judge.getMaxId())) {
            synchronized (this) {
                if (judge == null || !newJudge.getCount().equals(judge.getCount()) || !newJudge.getLastModified().equals(judge.getLastModified()) || !newJudge.getMaxId().equals(judge.getMaxId())) {
                    HeraLog.info("刷新hera_file库");
                    judge = newJudge;
                    List<HeraFile> all = heraFileMapper.getAll();
                    Map<Integer, HeraFile> jobMap = new HashMap<>(all.size());
                    all.forEach(file -> jobMap.put(file.getId(), file));
                    memoryJob = jobMap;
                }
            }
        }
        judge.setStamp(new Date());
        return memoryJob;
    }


    @Override
    public List<HeraFile> getAll() {
        return new ArrayList<>(getMemoryJob().values());
    }

    @Override
    public List<HeraFile> findByOwner(String owner) {
        return getMemoryJob().values().stream().filter(heraFile -> heraFile.getOwner().equals(owner)).collect(Collectors.toList());
    }

    @Override
    public List<HeraFile> findByParent(Integer parent) {
        return getMemoryJob().values().stream().filter(heraFile -> heraFile.getParent().equals(parent)).collect(Collectors.toList());
    }

}
