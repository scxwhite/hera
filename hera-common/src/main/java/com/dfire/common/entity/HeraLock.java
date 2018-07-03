package com.dfire.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 20:43 2018/1/10
 * @desc hera的分布式锁基于数据库实现
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HeraLock {

    private Integer id;
    private String host;
    private Date serverUpdate;
    private Date gmtCreate ;
    private Date gmtModified;
    private String subgroup;

}
