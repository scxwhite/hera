package com.dfire.common.entity.vo;

import lombok.Data;

/**
 *
 * @author jet
 * @date 2019-12-20
 */
@Data
public class PageHelperTimeRange {
    private Integer offset;
    private Integer pageSize;
    private Integer jobId;
    private String jobType;
    private String beginDt;
    private String endDt;
}
