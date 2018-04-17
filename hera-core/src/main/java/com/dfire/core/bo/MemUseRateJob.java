package com.dfire.core.bo;

import com.alibaba.fastjson.JSONObject;
import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.job.JobContext;
import com.dfire.core.job.ShellJob;
import com.dfire.core.netty.util.RunningJobKeys;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xiaosuda
 * @date 2018/4/13
 */
@Slf4j
public class MemUseRateJob extends ShellJob {

    private double rate;

    private double totalMem;

    private static Pattern pattern = Pattern.compile("\\d+");

    private static long date = System.currentTimeMillis();

    public static final String MEM = "mem";

    public static final String MEM_TOTAL = "memTotal";

    public MemUseRateJob(JobContext jobContext, double rate) {
        super(jobContext, "free -m | grep buffers/cache");
        jobContext.getProperties().getAllProperties().put(RunningJobKeys.JOB_RUN_TYPE, "MemUseRateJob");
        this.rate = rate;
    }

    @Override
    public int run() {
        //非linux系统 测试数据
        if (!HeraGlobalEnvironment.isLinuxSystem()) {
            jobContext.putData(MEM, 0.1);
            jobContext.putData(MEM_TOTAL, 204800d);
            return 0;
        }
        Integer exitCode = super.run();
        log.info("shell执行结果：{}",exitCode);
        if (exitCode == 0) {
            String[] contents = getJobContext().getHeraJobHistory().getLog().getContent().split("\n");
            log("执行结果：" + JSONObject.toJSONString(contents));
            for (String content : contents) {
                if (content.contains("buffers/cache")) {
                    String line = content.substring(content.indexOf("buffers/cache:"));
                    Matcher matcher = pattern.matcher(line);
                    double used = 0.0d, free = 0.0d;
                    int num = 0;
                    while (matcher.find()) {
                        if (num == 0) {
                            used = Double.valueOf(matcher.group());
                            num++;
                            continue;
                        }
                        if (num == 1) {
                            free = Double.valueOf(matcher.group());
                            break;
                        }
                    }
                    totalMem = free + used;
                    if ((System.currentTimeMillis() - date) > 3 * 60 * 1000) {
                        log.info("mem use rate used:" + used + " free:" + free + " rate:" + (used / (totalMem)));
                        date = System.currentTimeMillis();
                    }
                    jobContext.putData(MEM, (used / (totalMem)));
                    jobContext.putData(MEM_TOTAL, totalMem);

                    if (used / totalMem > rate) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            }
        }
        return -1;
    }

}
