package com.dfire.common.mapper;

import com.dfire.common.entity.HeraFile;
import java.util.List;

import org.apache.ibatis.annotations.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:54 2018/1/17
 * @desc 开发中心文件管理
 */
public interface HeraFileMapper {


    @Select("SELECT * FROM hera_file WHERE parent = #{parent}")
    List<HeraFile> getSubHeraFiles(@Param("parent") String parentId);

    @Select("SELECT * FROM hera_file WHERE OWNER = #{owner} and parent is null")
    List<HeraFile> getUserHeraFiles(@Param("owner") String owner);

    @Select("SELECT * FROM hera_file WHERE OWNER = #{owner}")
    List<HeraFile> getAllUserHeraFiles(@Param("owner") String owner);

    @Select("SELECT * FROM hera_file WHERE id = #{id}")
    public HeraFile getHeraFile(String id);


    @Insert("INSERT into hera_file(content, name, owner, parent, type, host_group_id, gmt_create, gmt_modified) VALUES(#{content}, #{name}, #{owner}, #{parent}, #{type}, #{hostGroupId}, #{gmtCreate}, #{gmtModified})")
    public void addHerFile(HeraFile heraFile);

    @Delete("DELETE FROM hera_file WHERE id =#{id}")
    public void deleteHeraFile(String id);

    @Update("UPDATE hera_file SET content = #{content}, name = #{name}, owner = #{owner}, parent = #{parent}, type = #{type}, host_group_id = #{hostGroupId}, gmt_create = #{gmtCreate}, gmt_modified = #{gmtModified} WHERE id =#{id}")
    public void update(HeraFile heraFile);

}
