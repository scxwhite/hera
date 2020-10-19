package com.dfire.common.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * desc:
 *
 * @author scx
 * @create 2019/06/10
 */
@Data
@ApiModel(value = "hera用户对象")
public class HeraSso {
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "用户名")
    private String name;
    @ApiModelProperty(value = "密码")
    private String password;
    @ApiModelProperty(value = "用户组id")
    private Integer gid;
    @ApiModelProperty(value = "电话")
    private String phone;
    @ApiModelProperty(value = "邮箱")
    private String email;
    @ApiModelProperty(value = "工号")
    private String jobNumber;
    @ApiModelProperty(value = "更改时间戳")
    private Long gmtModified;
    @ApiModelProperty(value = "0已删除，1存在")
    private Integer isValid;
}
