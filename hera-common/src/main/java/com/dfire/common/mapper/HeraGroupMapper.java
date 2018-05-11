package com.dfire.common.mapper;

import com.dfire.common.entity.HeraGroup;
import com.dfire.common.entity.model.HeraGroupBean;
import org.apache.ibatis.annotations.Select;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:59 2018/4/17
 * @desc
 */
public interface HeraGroupMapper {

    @Select("SELECT * FROM hera_group WHERE name = 'default' ")
    HeraGroup getGlobalGroup();
}
