package com.dfire.common.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author xiaosuda
 * @date 2018/12/3
 */
@Data
public class Judge {

    public Date lastModified;
    public Integer maxId;
    public Integer count;
    public Date stamp;
}

