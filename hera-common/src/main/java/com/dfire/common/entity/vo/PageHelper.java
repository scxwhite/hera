package com.dfire.common.entity.vo;

import lombok.Data;

/**
 *
 * @author xiaosuda
 * @date 2018/7/30
 */
@Data
public class PageHelper {
    private Integer offset;
    private Integer pageSize;
    private Integer jobId;
}
