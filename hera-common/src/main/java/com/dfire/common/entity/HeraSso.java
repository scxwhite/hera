package com.dfire.common.entity;

import lombok.Data;

/**
 * desc:
 *
 * @author scx
 * @create 2019/06/10
 */
@Data
public class HeraSso {

    private Integer id;

    private String name;

    private String password;

    private Integer gid;

    private String phone;

    private String email;

    private String jobNumber;

    private Long gmtModified;

    private Integer isValid;
}
