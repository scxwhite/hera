package com.dfire.core.netty.master.comparator;

import com.dfire.common.vo.JobElement;

import java.util.Comparator;

/**
 * desc:
 *
 * @author scx
 * @create 2020/04/02
 */
public class FiFoComp implements Comparator<JobElement> {

    @Override
    public int compare(JobElement o1, JobElement o2) {
        //优先根据任务优先级排列
        if (o1.getPriorityLevel() > o2.getPriorityLevel()) {
            return -1;
        } else if (o1.getPriorityLevel().equals(o2.getPriorityLevel())) {
            return 0;
        }
        return 1;
    }
}
