package com.dfire.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:34 2018/1/11
 * @desc
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ZeusPermission {

    public static final String GROUP_TYPE="group";
    public static final String JOB_TYPE="job";
    private Long id;
    private String type;
    private Long targetId;
    private String uid;
    private Date gmtCreate=new Date();
    private Date gmtModified=new Date();
}
