package com.dfire.monitor.service.impl;

import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.util.ActionUtil;
import com.dfire.monitor.domain.ActionTime;
import com.dfire.monitor.domain.JobHistoryVo;
import com.dfire.monitor.domain.JobStatusNum;
import com.dfire.monitor.mapper.JobManagerMapper;
import com.dfire.monitor.service.JobManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public JsonResponse findJobHistoryByStatus(String status,String begindt,String enddt) {
        List<JobHistoryVo> failedJobs = jobManagerMapper.findAllJobHistoryByStatus(status,begindt,enddt);
        if (failedJobs == null) {
            return new JsonResponse(false, "失败任务查询数据为空");
        }
        List<JobHistoryVo> result = failedJobs.stream().filter(distinctByKey(JobHistoryVo::getJobId)).collect(Collectors.toList());
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
        Map<String, Object> map = new HashMap<>(3);
        map.put("startDate", ActionUtil.getActionVersionByDate(calendar.getTime()));
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        map.put("endDate", ActionUtil.getActionVersionByDate(calendar.getTime()));
        map.put("limitNum", 10);
        List<ActionTime> jobTime = jobManagerMapper.findJobRunTimeTop10(map);
        if (jobTime == null || jobTime.size() == 0) {
            return new JsonResponse(false, "查询不到任务");
        }

        calendar.add(Calendar.DAY_OF_MONTH, -2);
        String id = ActionUtil.getFormatterDate("yyyyMMdd", calendar.getTime());
        for (ActionTime actionTime : jobTime) {
            actionTime.setYesterdayTime(jobManagerMapper.getYesterdayRunTime(actionTime.getJobId(), id));
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
        Integer day = 6;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -day);
        Long lastDate = Long.parseLong(ActionUtil.getActionVersionByDate(calendar.getTime()));
        res.put("runFailed", jobManagerMapper.findJobDetailByStatus(lastDate, "failed"));
        res.put("runSuccess", jobManagerMapper.findJobDetailByStatus(lastDate, "success"));
        String curDate;
        List<String> xAxis = new ArrayList<>(day);
        Date startDate;
        for (int i = 0; i <= day; i++) {
            startDate = calendar.getTime();
            curDate = ActionUtil.getFormatterDate("yyyy-MM-dd", startDate);
            xAxis.add(curDate);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            res.put(curDate,
                    jobManagerMapper.findJobDetailByDate(
                            Long.parseLong(ActionUtil.getActionVersionByDate(startDate)),
                            Long.parseLong(ActionUtil.getActionVersionByDate(calendar.getTime()))));

        }
        res.put("xAxis", xAxis);
        return new JsonResponse("查询成功", true, res);
    }


}
