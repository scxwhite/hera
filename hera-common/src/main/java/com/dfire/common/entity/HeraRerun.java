package com.dfire.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:32 2018/1/11
 * @desc 开发中心脚本记录
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HeraRerun {

    private Integer id;

    private Integer jobId;

    private Integer isEnd;

    private String name;

    private Long startMillis;

    private Long endMillis;

    private Long gmtCreate;

    private String ssoName;

    private String extra;

    private Long actionNow;

}
