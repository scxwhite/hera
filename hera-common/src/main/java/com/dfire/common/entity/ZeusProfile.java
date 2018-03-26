package com.dfire.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:35 2018/1/11
 * @desc
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ZeusProfile {

    private String id;
    private String uid;
    private Map<String, String> hadoopConf=new HashMap<String, String>();
    private Date gmtCreate=new Date();
    private Date gmtModified=new Date();
}
