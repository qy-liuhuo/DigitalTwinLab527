package org.mobinets.dtlab.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ResponseCode implements StatusCode {

    //成功提示码
    SUCCESS(200, "success"),

    //错误提示码
    FAILURE(500, "failed"),

    //参数错误
    PARAM_ERROR(400,"Parameter error"),

    //越权访问
    PERMISSION_ERROR(403,"Illegal access deny"),

    //未登录
    NOT_LOGGED_IN(405,"Haven't logged in yet");



    private final Integer code;
    private final String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}