package com.dfire.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:46 2018/4/17
 * @desc
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HeraGroup {

    private Integer id;

    private String configs;

    private String description;

    private Integer directory;

    private Date gmtCreate;

    private Date gmtModified;

    private String name;

    private String owner;

    private Integer parent;

    private String resources;

    private Integer existed;

}
