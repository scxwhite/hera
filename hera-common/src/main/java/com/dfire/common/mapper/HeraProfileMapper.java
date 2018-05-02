package com.dfire.common.mapper;

import com.dfire.common.entity.HeraProfile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:46 2018/5/1
 * @desc
 */
public interface HeraProfileMapper {

    @Select("SELECT * FROM hera_user WHERE owner = #{owner}")
    HeraProfile findByOwner(@Param("owner") String owner);

    @Insert("INSERT INTO hera_profile(id, gmt_create, gmt_modified, uid) "+
            "VALUES(#{id, jdbcType=VARCHAR}, #{gmtCreate, jdbcType=VARCHAR}, #{gmtModified, jdbcType=VARCHAR}, #{uid, jdbcType=VARCHAR})")
    void insert(HeraProfile profile);


    @Update("UPDATE  hera_user set id = #{id}, uid = #{uid}")
    void update(HeraProfile profile);

}
