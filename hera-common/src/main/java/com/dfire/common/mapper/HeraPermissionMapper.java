package com.dfire.common.mapper;


import com.dfire.common.entity.HeraPermission;
import com.dfire.common.mybatis.HeraInsertLangDriver;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:49 2018/5/15
 * @desc
 */
public interface HeraPermissionMapper {

    @Insert("insert into hera_permission (#{heraPermission})")
    @Lang(HeraInsertLangDriver.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(HeraPermission heraPermission);


    @Select("select * from hera_permission where target_id =#{targetId} and is_valid = #{isValid} and type = #{type}")
    List<HeraPermission> findByTargetId(@Param("targetId") Integer targetId,
                                        @Param("type") String type,
                                        @Param("isValid") Integer isValid);

    @Select("select * from hera_permission where target_id = #{id} and is_valid = 1 and uid = #{owner} and type=#{type}")
    HeraPermission findByCond(@Param("id") Integer id,
                              @Param("owner") String owner,
                              @Param("type") String type);

    @Delete("update hera_permission set is_valid = #{is_valid} where target_id = #{id} and type = #{type} and uid=#{uid}")
    Integer updateByUid(@Param("id") Integer id,
                        @Param("type") String type,
                        @Param("is_valid") Integer isValid,
                        @Param("uid") String uId);

    @Insert({"<script> " +
            " insert into hera_permission (gmt_modified,target_id,type,uid) " +
            "values " +
            "<foreach collection=\"list\"  separator=\",\" item=\"item\" > " +
            " (#{item.gmtModified},#{item.targetId},#{item.type},#{item.uid}) " +
            " </foreach>" +
            " </script>"})
    Integer insertList(@Param("list") List<HeraPermission> permissions);
}
