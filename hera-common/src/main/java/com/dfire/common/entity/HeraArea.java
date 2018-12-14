package com.dfire.common.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author xiaosuda
 * @date 2018/12/14
 */
@Data
public class HeraArea {

    private Integer id;

    private String timezone;

    private String name;

    private Date gmtCreate;

    private Date gmtModified;

}
