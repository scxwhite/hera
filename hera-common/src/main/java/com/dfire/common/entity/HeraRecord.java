package com.dfire.common.entity;

import com.dfire.common.config.SkipColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * desc: hera操作日志 以及脚本的历史版本恢复
 *
 * @author scx
 * @create 2019/07/16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeraRecord {

    private Integer id;

    private Integer type;

    private String content;

    private String sso;

    private Integer gid;

    private String logType;

    private Integer logId;

    private Long gmtCreate;

    private Long gmtModified;


}
