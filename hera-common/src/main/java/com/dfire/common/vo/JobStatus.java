package com.dfire.common.vo;

import com.dfire.common.enums.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 23:43 2018/1/12
 * @desc Job的状态用于持久化Job状态，重启服务器时用作恢复
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobStatus {

    /**
     * 版本号id
     */
    private Long actionId;

    private StatusEnum status;

    private Long historyId;

    private Date startTime;

    private Date endTime;

    /**
     * 依赖的Job的状态：key 依赖的版本号ID，value 依赖的Job的完成时间
     */
    private Map<String, String> readyDependency;
}
