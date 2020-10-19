package com.dfire.common.entity.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * layui table分页工具
 *
 * @author xiaosuda
 * @date 2018/12/8
 */
@Data
@ApiModel("layui分页对象")
public class TablePageForm {

    @ApiModelProperty("当前页码")
    private Integer page;
    @ApiModelProperty("页面大小")
    private Integer limit;
    @ApiModelProperty("总数据量")
    private Integer count;


    public Integer getStartPos() {
        return (page - 1) * limit;
    }
}
