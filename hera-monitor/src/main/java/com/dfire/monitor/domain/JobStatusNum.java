package com.dfire.monitor.domain;

import lombok.Data;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午11:35 2018/8/15
 * @desc
 */
@Data
public class JobStatusNum {

    private String status;

    private Integer num;

    private String curDate;
}
