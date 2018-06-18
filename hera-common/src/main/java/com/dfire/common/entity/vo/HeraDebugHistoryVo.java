package com.dfire.common.entity.vo;

import com.dfire.common.enums.JobRunTypeEnum;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.vo.LogContent;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午12:05 2018/5/16
 * @desc
 */
@Builder
@Data
public class HeraDebugHistoryVo {

    private String id;

    private String fileId;

    private Date startTime;

    private Date endTime;

    private String executeHost;

    private StatusEnum statusEnum;

    private String owner;

    private Date gmtCreate;

    private Date gmtModified;

    private String script;

    private JobRunTypeEnum runType;

    private LogContent log ;

    private String host;

    private int hostGroupId;

}
