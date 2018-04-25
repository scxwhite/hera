package com.dfire.common.mapper;

import com.dfire.common.entity.HeraDebugHistory;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午7:25 2018/4/16
 * @desc
 */
public interface HeraDebugHistoryMapper {

    public void addHeraDebugHistory(HeraDebugHistory heraDebugHistory);

    @Select("SELECT * FROM hera_debug_history WHERE id = #{id}")
    public HeraDebugHistory findById(@Param("id") String id);


    @Update("UPDATE hera_debug_history SET content = #{content}, name = #{name}, owner = #{owner}, parent = #{parent}, type = #{type}, host_group_id = #{hostGroupId}, gmt_create = #{gmtCreate}, gmt_modified = #{gmtModified} WHERE id =#{id}")
    public void update(HeraDebugHistory heraDebugHistory);


}
