package com.dfire.monitor.service.impl;

import com.dfire.common.util.ActionUtil;
import com.dfire.monitor.domain.ActionTime;
import com.dfire.monitor.domain.JobHistoryVo;
import com.dfire.monitor.domain.JobStatusNum;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.monitor.mapper.JobManagerMapper;
import com.dfire.monitor.service.JobManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:13 2018/8/14
 * @desc
 */
@Service("jobManageService")
public class JobManageServiceImpl implements JobManageService {

    @Autowired
    JobManagerMapper jobManagerMapper;

    @Override
    public JsonResponse findJobHistoryByStatus(String status) {

        List<JobHistoryVo> failedJobs = jobManagerMapper.findAllJobHistoryByStatus(status);
        if (failedJobs == null) {
            return new JsonResponse(false, "失败任务查询数据为空");
        }
        List<JobHistoryVo> result = failedJobs.stream().filter(distinctByKey(h -> h.getJobId())).collect(Collectors.toList());
        return new JsonResponse("查询成功", true, result);
    }

    /**
     * 去除jobId相同的记录
     *
     * @param keyExtractor
     * @param <T>
     * @return
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new HashMap<>(1024);
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    /**
     * 任务top10
     *
     * @return
     */
    @Override
    public JsonResponse findJobRunTimeTop10() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        Map<String, Object> map = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd 23:00:00");
        String start = sdf.format(date);
        String end = sdf2.format(date);
        map.put("startDate", start);
        map.put("endDate", end);
        map.put("limitNum", 10);
        List<ActionTime> jobTime = jobManagerMapper.findJobRunTimeTop10(map);
        if (jobTime == null || jobTime.size() == 0) {
            return new JsonResponse(false, "查询不到任务");
        }

        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Date time = calendar.getTime();
        String yesterday = ActionUtil.getFormatterDate("yyyy-MM-dd", time);
        for (ActionTime actionTime : jobTime) {
            actionTime.setYesterdayTime(jobManagerMapper.getYesterdayRunTime(actionTime.getJobId(), yesterday));
        }
        return new JsonResponse("查询成功", true, jobTime);
    }

    /**
     * 首页饼图
     *
     * @return
     */
    @Override
    public JsonResponse findAllJobStatus() {
        List<JobStatusNum> currDayStatusNum = jobManagerMapper.findAllJobStatus();
        if (currDayStatusNum == null || currDayStatusNum.size() == 0) {
            return new JsonResponse(false, "任务状态查询数据为空");
        }
        return new JsonResponse("查询成功", true, currDayStatusNum);
    }

    /**
     * 任务执行状态
     *
     * @return
     */
    @Override
    public JsonResponse findAllJobStatusDetail() {
        Map<String, Object> res = new HashMap<>(9);
        res.put("runFailed", jobManagerMapper.findJobDetailByStatus("failed"));
        res.put("runSuccess", jobManagerMapper.findJobDetailByStatus("success"));

        String curDate;
        Integer day = 6;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -day);
        List<String> xAxis = new ArrayList<>(day);
        for (int i = 0; i <= day; i++) {
            curDate = ActionUtil.getFormatterDate("yyyy-MM-dd", calendar.getTime());
            res.put(curDate, jobManagerMapper.findJobDetailByDate(curDate));
            xAxis.add(curDate);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        res.put("xAxis", xAxis);
        return new JsonResponse("查询成功", true, res);
    }


}
