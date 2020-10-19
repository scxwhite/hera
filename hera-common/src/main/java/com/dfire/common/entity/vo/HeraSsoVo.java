package com.dfire.common.entity.vo;

import lombok.Data;

/**
 * @author xiaosuda
 * @date 2018/12/28
 */
@Data
public class HeraSsoVo {

    private Integer id;

    private String name;

    private String gName;

    private String phone;

    private String email;

    private String jobNumber;

    private Long gmtModified;

    private Integer isValid;
}