package com.dfire.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:32 2018/1/11
 * @desc 开发中心脚本记录
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HeraFile {

    private Integer id;

    private String name;

    private String owner;

    private Integer parent;

    /**
     * 脚本内容
     */
    private String content;

    /**
     * 1 文件夹 2 文件
     */
    private Integer type;

    private Date gmtCreate;

    private Date gmtModified;

    private int hostGroupId;

    private Integer jobId;

}
