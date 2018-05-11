package com.dfire.common.entity.vo;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:51 2018/4/17
 * @desc
 */
@Builder
@Data
public class HeraGroupVo {

    private String id;
    private String parent;
    private String name;
    private String owner;
    private String desc;
    private boolean directory;
    private int isExisted;

    private Map<String, String> properties=new HashMap<String, String>();
    private List<Map<String,String>> resources=new ArrayList<Map<String,String>>();
}
