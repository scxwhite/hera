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
 * @time: Created in 17:32 2018/1/11
 * @desc 开发中心脚本记录
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "hera开发中心脚本ø对象")
public class HeraFile {
    @ApiModelProperty(value = "ID")
    private Integer id;
    @ApiModelProperty(value = "脚本名称")
    private String name;
    @ApiModelProperty(value = "所有人")
    private String owner;
    @ApiModelProperty(value = "父目录")
    private Integer parent;

    /**
     * 脚本内容
     */
    @ApiModelProperty(value = "脚本内容")
    private String content;
    /**
     * 1 文件夹 2 文件
     */
    @ApiModelProperty(value = "类型：1 文件夹 2 文件")
    private Integer type;
    @SkipColumn
    @ApiModelProperty(value = "创建时间")
    private Date gmtCreate;
    @SkipColumn
    @ApiModelProperty(value = "更新时间")
    private Date gmtModified;

    @ApiModelProperty(value = "执行机器组id")
    private int hostGroupId;
    @ApiModelProperty(value = "关联调度任务id")
    private Integer jobId;

}
