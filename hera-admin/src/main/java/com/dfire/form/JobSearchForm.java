package com.dfire.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * desc:
 *
 * @author scx
 * @create 2019/07/10
 */
@Data
@ApiModel("任务模糊搜索表单")
public class JobSearchForm {

    @ApiModelProperty("脚本内容")
    private String script;
    @ApiModelProperty("任务名称")
    private String name;
    @ApiModelProperty("任务描述")
    private String description;
    @ApiModelProperty("任务配置")
    private String config;
    @ApiModelProperty("开启状态")
    private Integer auto;
    @ApiModelProperty("执行类型")
    private String runType;


}