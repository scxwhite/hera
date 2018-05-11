package com.dfire.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:46 2018/4/17
 * @desc
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HeraGroup {

    private String id;
    private String parent;
    private String name;
    private String owner;
    private String desc;
    private boolean directory;
    private int isExisted;
    private String configs;
    private String resources;

}
