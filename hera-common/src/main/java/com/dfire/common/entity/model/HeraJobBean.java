package com.dfire.common.entity.model;

import com.dfire.common.entity.vo.HeraJobVo;
import com.dfire.common.util.HierarchyProperties;
import com.dfire.common.vo.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午10:35 2018/5/8
 * @desc
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeraJobBean {

    private HeraJobVo heraJobVo;
    private JobStatus jobStatus;
    private HeraGroupBean groupBean;

    private Set<HeraJobBean> upStream = new HashSet<>();

    private Set<HeraJobBean> downStream = new HashSet<>();

    public HierarchyProperties getHierarchyProperties() {
        if(groupBean != null) {
            return new HierarchyProperties(groupBean.getHierarchyProperties(), heraJobVo.getConfigs());
        }
        return new HierarchyProperties(heraJobVo.getConfigs());
    }

    public List<Map<String, String>> getHierarchyResources() {
        List<String> existList = new ArrayList<>();
        List<Map<String, String>> local = new ArrayList<>();
        if (heraJobVo.getResources() != null) {
            local.addAll(heraJobVo.getResources());
        }

        for(Map<String, String> map : local) {
            if(map.get("name") != null && !existList.contains(map.get("name"))) {
                existList.add(map.get("name"));
            }
        }
        if(groupBean != null && groupBean.getHierarchyResources() != null) {
            List<Map<String, String>> parent = groupBean.getHierarchyResources();
            for(Map<String, String> map : parent) {
                if(map.get("name") != null && !existList.contains(map.get("name"))) {
                    existList.add(map.get("name"));
                    local.add(map);
                }
            }
        }
        return local;
    }

    public void addUpStream(HeraJobBean jobBean) {
        if(!upStream.contains(jobBean)) {
            upStream.add(jobBean);
        }
    }

    public void addDownStream(HeraJobBean heraJobBean) {
        if(!downStream.contains(heraJobBean)) {
            downStream.add(heraJobBean);
        }
    }
}
