package com.dfire.common.mapper;

import com.dfire.common.entity.HeraAdvice;
import com.dfire.common.mybatis.HeraInsertLangDriver;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author xiaosuda
 * @date 2018/12/5
 */
public interface HeraAdviceMapper {

    @Insert("insert into hera_advice (#{heraAdvice})")
    @Lang(HeraInsertLangDriver.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Integer insert(HeraAdvice heraAdvice);


    @Select("select * from hera_advice")
    @Lang(HeraSelectLangDriver.class)
    List<HeraAdvice> getAll();

}
