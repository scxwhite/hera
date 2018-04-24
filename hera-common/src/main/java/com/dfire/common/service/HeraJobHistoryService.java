package com.dfire.common.service;

import com.dfire.common.entity.HeraJobHistory;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 19:18 2018/1/12
 * @desc
 */
public interface HeraJobHistoryService {

    HeraJobHistory findJobHistory(String id);
}
