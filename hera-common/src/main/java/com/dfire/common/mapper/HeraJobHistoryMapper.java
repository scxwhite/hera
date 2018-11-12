package com.dfire.common.mapper;

import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.vo.PageHelper;
import com.dfire.common.mybatis.HeraInsertLangDriver;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import com.dfire.common.mybatis.HeraUpdateLangDriver;
import org.apache.ibatis.annotations.*;

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

    @Select("select * from hera_action_history where action_id = #{id} limit 1")
    HeraJobHistory findByActionId(@Param("id") String id);

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

    @Select("select id,action_id,job_id,start_time,end_time,execute_host,operator,status,trigger_type,illustrate,host_group_id from hera_action_history where job_id = #{jobId} order by id desc limit #{offset,jdbcType=INTEGER},#{pageSize,jdbcType=INTEGER} ")
    List<HeraJobHistory> selectByPage(PageHelper pageHelper);


    @Select("select job_id,start_time,end_time,status from hera_action_history where left(start_time,10) >= CURDATE()")
    List<HeraJobHistory> findTodayJobHistory();
}

