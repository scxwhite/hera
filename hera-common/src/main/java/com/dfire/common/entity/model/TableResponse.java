package com.dfire.common.entity.model;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.Map;

/**
 * layui table返回专用
 *
 * @author xiaosuda
 * @date 2018/12/5
 */
@Data
public class TableResponse {

    private String msg;
    private Integer count;
    private Integer code;
    private Object data;


    public TableResponse(String msg, Integer code, Object data) {
        this.msg = msg;
        this.code = code;
        this.data = data;
    }

    public TableResponse(Integer code, String msg) {
        this.msg = msg;
        this.code = code;
    }

    public TableResponse(Integer count, Integer code, Object data) {
        this.count = count;
        this.code = code;
        this.data = data;
    }

    public Map<String, ?> toMap() {
        return (Map<String, Object>) JSONObject.toJSON(this);
    }
}
