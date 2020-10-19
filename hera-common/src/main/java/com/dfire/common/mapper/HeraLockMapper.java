package com.dfire.common.mapper;

import com.dfire.common.entity.HeraLock;
import com.dfire.common.mybatis.HeraInsertLangDriver;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import com.dfire.common.mybatis.HeraUpdateLangDriver;
import org.apache.ibatis.annotations.*;

import java.util.Date;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 14:25 2018/1/12
 * @desc
 */
public interface HeraLockMapper {

    @Select("select * from hera_lock where subgroup = #{subgroup}")
    @Lang(HeraSelectLangDriver.class)
    HeraLock findBySubgroup(String subgroup);

    @Insert("insert into hera_lock (#{heraLock})")
    @Lang(HeraInsertLangDriver.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Integer insert(HeraLock heraLock);


    @Update("update hera_lock (#{heraLock}) where id = #{id}")
    @Lang(HeraUpdateLangDriver.class)
    int update(HeraLock heraLock);

    /**
     * 根据上一次的master ip 更新抢占的master信息 保证三者抢占只有一个成功
     * @param host          新的master抢占ip
     * @param serverUpdate  更新时间
     * @param gmtModified   修改时间
     * @param lastHost      老master ip
     * @return
     */
    @Update("update hera_lock set gmt_modified = #{gmtModified},host = #{host},server_update = #{serverUpdate} where host = #{lastHost}")
    Integer updateLock(@Param("host") String host,
                       @Param("serverUpdate") Date serverUpdate,
                       @Param("gmtModified") Date gmtModified,
                       @Param("lastHost") String lastHost);
}

