package com.dfire.common.entity.model;

import lombok.Data;

/**
 * layui table分页工具
 *
 * @author xiaosuda
 * @date 2018/12/8
 */
@Data
public class TablePageForm {

    private Integer page;
    private Integer limit;
    private Integer count;


    public Integer getStartPos() {
        return (page - 1) * limit;
    }
}
