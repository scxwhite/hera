package com.dfire.common.mapper;

import com.dfire.common.entity.ZeusHostRelation;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:05 2018/1/12
 * @desc
 */
public interface ZeusHostRelationMapper {

    @Select("SELECT * FROM ZEUS_HOST_RELATION ")
    public List<ZeusHostRelation> getAllHostRelationList();

}
