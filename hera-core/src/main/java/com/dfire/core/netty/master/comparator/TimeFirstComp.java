package com.dfire.core.netty.master.comparator;

import com.dfire.common.vo.JobElement;

import java.util.Comparator;

/**
 * desc:
 *
 * @author scx
 * @create 2020/04/02
 */
public class TimeFirstComp implements Comparator<JobElement> {

    @Override
    public int compare(JobElement o1, JobElement o2) {
        //优先根据任务优先级排列
        if (o1.getPriorityLevel() > o2.getPriorityLevel()) {
            return -1;
        } else if (o1.getPriorityLevel().equals(o2.getPriorityLevel())) {
            //如果任务优先级相等，根据版本的时间排序，一般来说版本时间越小越优先执行
            long firstId = o1.getJobId() / 1000000;
            long secondId = o2.getJobId() / 1000000;
            if (firstId < secondId) {
                return -1;
            } else if (firstId == secondId) {
                return 0;
            }
        }
        return 1;
    }
}
