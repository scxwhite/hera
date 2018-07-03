package com.dfire.common.mapper;

import com.dfire.common.entity.HeraLock;
import com.dfire.common.mybatis.HeraInsertLangDriver;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import com.dfire.common.mybatis.HeraUpdateLangDriver;
import org.apache.ibatis.annotations.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 14:25 2018/1/12
 * @desc
 */
public interface HeraLockMapper {

    @Select("select * from hera_lock where subgroup = #{subgroup}")
    @Lang(HeraSelectLangDriver.class)
    HeraLock findById(HeraLock heraLock);

    @Insert("insert into hera_lock (#{heraLock})")
    @Lang(HeraInsertLangDriver.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(HeraLock heraLock);


    @Update("update hera_lock (#{heraLock}) where id = #{id}")
    @Lang(HeraUpdateLangDriver.class)
    int update(HeraLock heraLock);

}
