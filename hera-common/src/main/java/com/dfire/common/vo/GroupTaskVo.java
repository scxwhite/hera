package com.dfire.common.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author xiaosuda
 * @date 2018/12/3
 */
@Data
@Builder
public class GroupTaskVo {

    private String actionId;
    private Integer jobId;
    private String name;
    private String status;
    private String readyStatus;
    private String lastResult;
}
