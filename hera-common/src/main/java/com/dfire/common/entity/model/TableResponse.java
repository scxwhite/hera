package com.dfire.common.entity.model;

import lombok.Data;

/**
 * layui table返回专用
 * @author xiaosuda
 * @date 2018/12/5
 */
@Data
public class TableResponse<T> {

    private String msg;
    private Integer count;
    private Integer code;
    private T data;


    public TableResponse(String msg, Integer code, T data) {
        this.msg = msg;
        this.code = code;
        this.data = data;
    }

    public TableResponse( Integer code, String msg) {
        this.msg = msg;
        this.code = code;
    }

    public TableResponse(Integer count, Integer code, T data) {
        this.count = count;
        this.code = code;
        this.data = data;
    }
}
