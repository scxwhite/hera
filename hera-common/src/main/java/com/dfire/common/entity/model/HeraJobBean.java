package com.dfire.common.entity.model;

import com.dfire.common.entity.vo.HeraActionVo;
import com.dfire.common.util.HierarchyProperties;
import com.dfire.common.vo.JobStatus;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午10:35 2018/5/8
 * @desc
 */
@Builder
@Data
public class HeraJobBean {

    private HeraActionVo heraActionVo;
    private JobStatus jobStatus;
    private HeraGroupBean groupBean;

    private Set<HeraJobBean> upStream;

    private Set<HeraJobBean> downStream;

    public HierarchyProperties getHierarchyProperties() {
        if (groupBean != null) {
            return new HierarchyProperties(groupBean.getHierarchyProperties(), heraActionVo.getConfigs());
        }
        return new HierarchyProperties(heraActionVo.getConfigs());
    }

    public List<Map<String, String>> getHierarchyResources() {
        List<String> existList = new ArrayList<>();
        List<Map<String, String>> local = new ArrayList<>(heraActionVo.getResources());

        for (Map<String, String> map : local) {
            if (map.get("name") != null && !existList.contains(map.get("name"))) {
                existList.add(map.get("name"));
            }
        }
        if (groupBean != null) {
            List<Map<String, String>> parent = groupBean.getHierarchyResources();
            for (Map<String, String> map : parent) {
                if (map.get("name") != null && !existList.contains(map.get("name"))) {
                    existList.add(map.get("name"));
                    local.add(map);
                }
            }
        }
        return local;
    }

    public void addUpStream(HeraJobBean jobBean) {
        if (!upStream.contains(jobBean)) {
            upStream.add(jobBean);
        }
    }

    public void addDownStream(HeraJobBean heraJobBean) {
        if (!downStream.contains(heraJobBean)) {
            downStream.add(heraJobBean);
        }
    }
}
