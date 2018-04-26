package com.dfire.core.util;

import com.dfire.common.constant.JobRunType;
import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.common.util.HierarchyProperties;
import com.dfire.core.job.*;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午12:13 2018/4/26
 * @desc
 */
public class JobUtils {

    public static Job createDebugJob(JobContext jobContext, HeraDebugHistory heraDebugHistory,
                                     String workDir, ApplicationContext applicationContext) {
        jobContext.setDebugHistory(heraDebugHistory);
        jobContext.setWorkDir(workDir);
        //脚本中的变量替换，暂时不做
        HierarchyProperties hierarchyProperties = new HierarchyProperties(new HashMap<>());
        String script = heraDebugHistory.getScript();
        List<Map<String, String>> resources = new ArrayList<>();

        List<Job> pres = new ArrayList<>(1);
        pres.add(new DownLoadJob(jobContext));
        Job core = null;
        if(heraDebugHistory.getJobRunType() == JobRunType.Shell) {
            core = new ShellJob(jobContext);
        }
        Job job = new WithProcessJob(jobContext, pres, new ArrayList<>(), core, applicationContext);
        return job;
    }

}
