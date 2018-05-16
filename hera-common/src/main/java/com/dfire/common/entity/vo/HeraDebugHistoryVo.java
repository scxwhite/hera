package com.dfire.common.entity.vo;

import com.dfire.common.enums.JobRunType;
import com.dfire.common.enums.Status;
import com.dfire.common.vo.LogContent;

import java.util.Date;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午12:05 2018/5/16
 * @desc
 */
public class HeraDebugHistoryVo {

    private String id;

    private String fileId;

    private Date startTime;

    private Date endTime;

    private String executeHost;

    private Status status;

    private String owner;

    private Date gmtCreate ;

    private Date gmtModified;

    private String script;

    private JobRunType runType;

    private LogContent log = new LogContent();

    private String host;

    private String hostGroupId;

}
