package com.dfire.common.enums;

/**
 *
 * @author xiaosuda
 * @date 2018/6/19
 */
public enum  HttpCode {

    USER_NOT_LOGIN(401, "用户未登录"),
    REQUEST_SUCCESS(200, "请求成功"),
    REQUEST_FAIL(500, "请求异常");


    private Integer code;
    private String message;

    HttpCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
