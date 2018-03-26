package com.dfire.common.mapper;

import com.dfire.common.entity.ZeusFile;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:54 2018/1/17
 * @desc 开发中心文件管理
 */
public interface ZeusFileMapper {

    @Select("SELECT * FROM ZEUS_FILE WHERE OWNER = #{owner}")
    List<ZeusFile> getFileListByOwner(@Param("owner") String owner);
}
