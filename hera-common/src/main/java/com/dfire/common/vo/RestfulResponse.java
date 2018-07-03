package com.dfire.common.vo;

import com.dfire.common.enums.HttpCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 18:15 2018/1/7
 * @desc
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestfulResponse {

    private static final long serialVersionUID = -5795089018013798231L;

    private boolean success;

    private Integer code;

    private String msg;
    /**
     * total recorded
     */
    private int results;

    public RestfulResponse(boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }


    public void setHttpCode(HttpCode httpCode) {
        this.code = httpCode.getCode();
        this.msg = httpCode.getMessage();
    }

    public RestfulResponse(HttpCode httpCode) {
        setHttpCode(httpCode);
    }
}
