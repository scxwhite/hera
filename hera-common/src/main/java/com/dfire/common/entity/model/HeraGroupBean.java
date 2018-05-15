package com.dfire.common.entity.model;

import com.dfire.common.entity.vo.HeraGroupVo;
import com.dfire.common.util.HierarchyProperties;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午10:37 2018/5/8
 * @desc
 */
@Builder
@Data
public class HeraGroupBean {


    private HeraGroupBean parentGroupBean;
    private HeraGroupVo groupVo;
    private Map<String, HeraJobBean> jobBeanMap = new HashMap<>();
    private List<HeraGroupBean> child = new ArrayList<>();

    public HierarchyProperties getHierarchyProperties(){
        if(parentGroupBean!=null){
            return new HierarchyProperties(parentGroupBean.getHierarchyProperties(), groupVo.getProperties());
        }
        return new HierarchyProperties(groupVo.getProperties());
    }

    public Map<String, String> getProperties(){
        return groupVo.getProperties();
    }

    public List<Map<String, String>> getHierarchyResources(){
        List<Map<String, String>> local=new ArrayList<Map<String,String>>(groupVo.getResources());
        if(local==null){
            local=new ArrayList<Map<String,String>>();
        }
        if(parentGroupBean!=null){
            local.addAll(parentGroupBean.getHierarchyResources());
        }
        return local;
    }

    public Map<String,HeraJobBean> getAllSubJobBeans() {
        Map<String, HeraJobBean> map = new HashMap<>();
        return map;
    }
}
