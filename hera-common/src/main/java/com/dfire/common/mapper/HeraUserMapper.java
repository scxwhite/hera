package com.dfire.common.mapper;

import com.dfire.common.entity.HeraUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 23:55 2017/12/29
 * @desc
 */
public interface HeraUserMapper {

    @Select("SELECT * FROM HERA_USER WHERE NAME = #{name}")
    HeraUser findByName(@Param("name") String name) ;

    @Insert("INSERT INTO HERA_USER(NAME, AGE) VALUES(#{name}, #{age})")
    int insert(@Param("name") String name, @Param("age") Integer age);

}
