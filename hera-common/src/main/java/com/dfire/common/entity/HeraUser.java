package com.dfire.common.entity;

import com.dfire.common.config.SkipColumn;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("用户组对象")
public class HeraUser {
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "邮箱")
    private String email;

    @SkipColumn
    @ApiModelProperty(value = "创建时间")
    private Date gmtCreate;
    @SkipColumn
    @ApiModelProperty(value = "更新时间")
    private Date gmtModified;

    @ApiModelProperty(value = "名称")
    private String name;
    @ApiModelProperty(value = "电话")
    private String phone;
    @ApiModelProperty(value = "废弃")
    private String uid;
    @ApiModelProperty(value = "废弃")
    private String wangwang;
    @ApiModelProperty(value = "密码md5")
    private String password;
    @ApiModelProperty(value = "废弃")
    private int userType;
    @ApiModelProperty(value = "是否有效，0：已删除，1有效")
    private int isEffective;
    @ApiModelProperty(value = "描述")
    private String description;


}
