package com.dfire.common.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * desc:
 *
 * @author scx
 * @create 2020/12/21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeraActionMani {

    private Long id;

    private Integer jobId;

    private int auto;

    private String dependencies;

    private Long historyId;

    private String readyDependency;

    private Integer scheduleType;

    private String status;

    private String cycle;

}
