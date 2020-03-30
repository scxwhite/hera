package com.dfire.common.entity.form;

import lombok.Data;

/**
 * desc:
 *
 * @author scx
 * @create 2019/11/28
 */
@Data
public class HeraRerunForm {
    private Integer id;
    private Integer jobId;
    private String name;
    private String startTime;
    private String endTime;
    private String threads;

}
