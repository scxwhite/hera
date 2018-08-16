package com.dfire.common.entity.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
public class JsonResponse implements Serializable {

    private String  message;
    private boolean success;
    private Object  data;

    public JsonResponse(boolean success, String message) {
        this.message = message;
        this.success = success;
    }

    public JsonResponse(String message, boolean success, Object data) {
        this.message = message;
        this.success = success;
        this.data = data;
    }
    public JsonResponse(boolean success, String message, Object data) {
        this.message = message;
        this.success = success;
        this.data = data;
    }
}
