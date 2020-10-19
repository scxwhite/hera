package com.dfire.common.entity;

import com.dfire.common.config.SkipColumn;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:46 2018/4/17
 * @desc
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "hera任务组对象")
public class HeraGroup {

    @ApiModelProperty(value = "组ID")
    private Integer id;
    @ApiModelProperty(value = "json格式的配置")

    private String configs;
    @ApiModelProperty(value = "描述")

    private String description;
    @ApiModelProperty(value = "0大目录，1小目录")
    private Integer directory;

    @SkipColumn
    @ApiModelProperty(value = "创建时间")
    private Date gmtCreate;

    @SkipColumn
    private Date gmtModified;


    private String name;
    @ApiModelProperty(value = "所有人")
    private String owner;
    @ApiModelProperty(value = "父目录id")
    private Integer parent;
    @ApiModelProperty(value = "目前无用")
    private String resources;
    @ApiModelProperty(value = "0删除，1存在")
    private Integer existed;

}
