package com.dfire.common.config;

import com.dfire.common.vo.JobElement;

/**
 * @author scx
 */
public interface ExecuteFilter {

    void onExecute(JobElement element);


    void onResponse(JobElement element);

}
