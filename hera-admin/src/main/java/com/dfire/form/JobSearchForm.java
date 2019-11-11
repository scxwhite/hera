package com.dfire.form;

import lombok.Data;

/**
 * desc:
 *
 * @author scx
 * @create 2019/07/10
 */
@Data
public class JobSearchForm {

    private String script;
    private String name;
    private String description;
    private String config;
    private Integer auto;
    private String runType;


}