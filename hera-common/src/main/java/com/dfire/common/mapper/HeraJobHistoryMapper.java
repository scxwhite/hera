package com.dfire.common.mapper;

import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.vo.JobLogHistory;
import com.dfire.common.entity.vo.PageHelper;
import com.dfire.common.entity.vo.PageHelperTimeRange;
import com.dfire.common.mybatis.HeraInsertLangDriver;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import com.dfire.common.mybatis.HeraUpdateLangDriver;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:08 2018/4/23
 * @desc
 */
public interface HeraJobHistoryMapper {

    @Insert("insert into hera_action_history (#{heraJobHistory})")
    @Lang(HeraInsertLangDriver.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(HeraJobHistory heraJobHistory);

    @Delete("delete from hera_action_history where id = #{id}")
    int delete(@Param("id") String id);

    @Update("update hera_action_history (#{heraJobHistory}) where id = #{id}")
    @Lang(HeraUpdateLangDriver.class)
    int update(HeraJobHistory heraJobHistory);

    @Select("select * from hera_action_history")
    @Lang(HeraSelectLangDriver.class)
    List<HeraJobHistory> getAll();

    @Select("select * from hera_action_history where id = #{id}")
    HeraJobHistory findById(@Param("id") String id);

    @Select("select * from hera_action_history where action_id = #{actionId}")
    List<HeraJobHistory> findByActionId(@Param("actionId") String actionId);

    /**
     * 更新日志
     *
     * @param heraJobHistory
     * @return
     */
    @Update("update hera_action_history set log = #{log} where id = #{id}")
    int updateHeraJobHistoryLog(HeraJobHistory heraJobHistory);

    /**
     * 更新状态
     *
     * @param heraJobHistory
     * @return
     */
    @Update("update hera_action_history set status = #{status} where id = #{id}")
    int updateHeraJobHistoryStatus(HeraJobHistory heraJobHistory);

    /**
     * 更新日志和状态
     *
     * @param heraJobHistory
     * @return
     */
    @Update("update hera_action_history set log = #{log},status = #{status},end_time = #{endTime} where id = #{id}")
    Integer updateHeraJobHistoryLogAndStatus(HeraJobHistory heraJobHistory);

    /**
     * 根据jobId查询运行历史
     *
     * @param jobId
     * @return
     */
    @Select("select * from hera_action_history where job_id = #{job_id} order by id desc")
    List<HeraJobHistory> findByJobId(@Param("job_id") String jobId);

    /**
     * 根据ID查询日志逆袭
     *
     * @param id
     * @return
     */
    @Select("select log,status from hera_action_history where id = #{id}")
    HeraJobHistory selectLogById(Integer id);

    
    
    @Select("select count(1) from hera_action_history where job_id = #{id}")
    Integer selectCountById(Integer id);
    
    
    /**
     * 获取jobid的时间范围的执行个数
     * @param pageHelperTimeRange
     * @return
     */
    @Select("select count(1) as cnt "
    		+ "from hera_action_history a "
    		+ "where ( a.job_id = #{jobId}  )"
    		+ "and (a.start_time>=CAST(#{beginDt,jdbcType=VARCHAR} AS date) and  a.start_time< ADDDATE( CAST(#{endDt,jdbcType=VARCHAR} AS date) ,1)  ) "
    		)
    Integer selectCountByPageJob(PageHelperTimeRange pageHelperTimeRange);
    
    /**
     * 获取jobid的时间范围的执行历史明细
     * @param pageHelperTimeRange
     * @return
     */
    @Select("select a.id,a.action_id,a.job_id,a.start_time,a.end_time,a.execute_host,a.operator,a.status,a.trigger_type,a.illustrate,a.host_group_id,a.batch_id,a.biz_label"
    		+ ",b.name as job_name,b.description ,b.group_id,c.name as group_name "
    		+ "from hera_action_history a "
    		+ "inner join hera_job b on a.job_id=b.id  "
    		+ "inner join hera_group c on b.group_id=c.id  "
    		+ "where ( a.job_id = #{jobId}  )"
    		+ "and (a.start_time>=CAST(#{beginDt,jdbcType=VARCHAR} AS date) and  a.start_time< ADDDATE( CAST(#{endDt,jdbcType=VARCHAR} AS date) ,1)  ) "
    		+ "order by a.id desc "
    		+ "limit #{offset,jdbcType=INTEGER},#{pageSize,jdbcType=INTEGER} ")
    List<JobLogHistory> selectByPageJob(PageHelperTimeRange pageHelperTimeRange );
    
    
    /**
     * 获取groupId下的时间范围的执行历史个数
     * @param pageHelperTimeRange
     * @return
     */
    @Select("select count(1) as cnt "
    		+ "from hera_action_history a "
    		+ "inner join hera_job b on a.job_id=b.id  "
    		+ "where ( b.group_id = #{jobId} )"
    		+ "and (a.start_time>=CAST(#{beginDt,jdbcType=VARCHAR} AS date) and  a.start_time< ADDDATE( CAST(#{endDt,jdbcType=VARCHAR} AS date) ,1)  ) "
    		)
    Integer selectCountByPageGroup(PageHelperTimeRange pageHelperTimeRange);
    
    /**
     * 获取groupId下的时间范围的执行历史明细
     * @param pageHelperTimeRange
     * @return
     */
    @Select("select a.id,a.action_id,a.job_id,a.start_time,a.end_time,a.execute_host,a.operator,a.status,a.trigger_type,a.illustrate,a.host_group_id,a.batch_id,a.biz_label"
    		+ ",b.name as job_name,b.description ,b.group_id,c.name as group_name "
    		+ "from hera_action_history a "
    		+ "inner join hera_job b on a.job_id=b.id  "
    		+ "inner join hera_group c on b.group_id=c.id  "
    		+ "where ( b.group_id = #{jobId} )"
    		+ "and (a.start_time>=CAST(#{beginDt,jdbcType=VARCHAR} AS date) and  a.start_time< ADDDATE( CAST(#{endDt,jdbcType=VARCHAR} AS date) ,1)  ) "
    		+ "order by a.id desc "
    		+ "limit #{offset,jdbcType=INTEGER},#{pageSize,jdbcType=INTEGER} ")
    List<JobLogHistory> selectByPageGroup(PageHelperTimeRange pageHelperTimeRange );


    @Select("select job_id,start_time,end_time,status from hera_action_history where left(start_time,10) >= CURDATE()")
    List<HeraJobHistory> findTodayJobHistory();

    @Update("update hera_action_history set illustrate=#{illustrate},status=#{status},endTime=#{endTime} where id=#{id} ")
    int updateStatusAndIllustrate(@Param("id") Integer id,
                                  @Param("status") String status,
                                  @Param("illustrate") String illustrate,
                                  @Param("endTime") Date endTime);

    @Delete("delete from hera_action_history where action_id < DATE_SUB(CURRENT_DATE(),INTERVAL #{beforeDay} DAY) * 10000000000;")
    Integer deleteHistoryRecord(Integer beforeDay);

    @Select("select * from hera_action_history where job_id = #{jobId} order by id desc limit 1")
    HeraJobHistory findNewest(String jobId);
}
