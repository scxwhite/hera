package com.dfire.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeraUser {

    private Integer id;

    private String email;

    private Date gmtCreate;

    private Date gmtModified;

    private String name;

    private String phone;

    private String uid;

    private String wangwang;

    private String password;

    private int userType;

    private int isEffective;

    private String description;


}