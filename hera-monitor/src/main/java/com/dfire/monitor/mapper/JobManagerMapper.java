package com.dfire.monitor.mapper;

import com.dfire.monitor.domain.ActionTime;
import com.dfire.monitor.domain.JobHistoryVo;
import com.dfire.monitor.domain.JobStatusNum;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:05 2018/8/14
 * @desc
 */
public interface JobManagerMapper {

    /**
     * 今日任务详情
     *
     * @param status
     * @return
     */
    @Select(" select\n" +
            "                m.job_id job_id,hera_job.name job_name,hera_job.description description,  m.start_time start_time,m.end_time end_time,m.execute_host execute_host,m.status status,m.operator\n" +
            "\n" +
            "        from (\n" +
            "            select\n" +
            "                job_id,action_id,start_time,end_time,execute_host,status,operator\n" +
            "                from hera_action_history where job_id in\n" +
            "                (\n" +
            "                        select job_id\n" +
            "                        from\n" +
            "                        (\n" +
            "                                select job_id,substring_index(group_concat(status order by start_time desc),\",\",1) as status\n" +
            "                                from hera_action_history\n" +
            "                                where left(start_time,10) = CURRENT_DATE()\n" +
            "                                group by job_id\n" +
            "                        ) a\n" +
            "                        where status = #{status,jdbcType=VARCHAR}\n" +
            "                )\n" +
            "                and left(start_time,10) = CURRENT_DATE() and status = #{status,jdbcType=VARCHAR}\n" +
            "                order by job_id,start_time\n" +
            "            ) m left join hera_job on m.job_id = hera_job.id")
    List<JobHistoryVo> findJobHistoryByStatus(String status);


    /**
     *
     * 任务运行时长top10
     *
     * @param map
     * @return
     */
    @Select("select a.job_id,a.action_id,a.job_time,b.run_type from\n" +
            "        (select job_id,action_id,timestampdiff(SECOND,start_time,end_time)/60 as job_time from hera_action_history where start_time>#{startDate}\n" +
            "        " +
            "        and end_time<#{endDate} ) a\n" +
            "        " +
            "        inner join (select id,run_type from hera_job ) b\n" +
            "        on a.job_id = b.id order by a.job_time desc limit #{limitNum};")
    List<ActionTime> findJobRunTimeTop10(Map<String, Object> map);

    /**
     * 任务昨日运行时长
     *
     * @param jobId
     * @param startDate
     * @return
     */
    @Select(" select max(timestampdiff(SECOND,start_time,end_time)/60) from hera_action_history\n" +
            "        WHERE  job_id = #{jobId}\n" +
            "        AND DATE_FORMAT(start_time, '%Y-%m-%d') =  DATE_FORMAT(#{startDate}, '%Y-%m-%d')")
    Integer getYesterdayRunTime(@Param("jobId") Integer jobId, @Param("startDate") String startDate);

    /**
     * 按照运行状态汇总,初始化首页饼图
     *
     * @return
     */
    @Select(" select status,count(1) as num\n" +
            "        from\n" +
            "        (\n" +
            "        select job_id,substring_index(group_concat(status order by start_time desc),\",\",1) as status\n" +
            "        from hera_action_history\n" +
            "        where left(start_time,10)=CURRENT_DATE()\n" +
            "        group by job_id\n" +
            "        ) t\n" +
            "        group by status")
    List<JobStatusNum> findAllJobStatus();

    /**
     * 按照日期查询任务明细
     *
     * @param curDate
     * @return
     */
    @Select(" select status,count(1) as num\n" +
            "        from\n" +
            "        (\n" +
            "        select job_id,substring_index(group_concat(status order by start_time desc),\",\",1) as status\n" +
            "        from hera_action_history\n" +
            "        where left(start_time,10)=#{selectDate,jdbcType=VARCHAR}\n" +
            "        group by job_id\n" +
            "        ) t\n" +
            "        group by status")
    List<JobStatusNum> findJobDetailByDate(String curDate);
}
