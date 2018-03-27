package com.dfire.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeraUser {

	private Long id;
	private String email;
	private Date gmtCreate;
	private Date gmtModified;
    private String name;
    private String phone;
    private String uid;
    private String wangwang;
    private String password;
	private Integer user_type;
	private Integer is_effective;
	private String description;


}