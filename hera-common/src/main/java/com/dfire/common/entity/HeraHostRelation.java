package com.dfire.common.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:33 2018/1/11
 * @desc
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("机器组管理对象")
public class HeraHostRelation {

    @ApiModelProperty("id")
    private Integer id;
    @ApiModelProperty("ip")
    private String host;
    @ApiModelProperty("机器组id")
    private Integer hostGroupId;
}
