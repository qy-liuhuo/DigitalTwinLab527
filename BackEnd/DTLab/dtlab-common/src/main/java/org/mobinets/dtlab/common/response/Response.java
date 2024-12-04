package org.mobinets.dtlab.common.response;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

@Data
public class Response<T> implements Serializable {

    private Integer code;

    private String message;

    private T data;

    public Response(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Response<T> success(){
        return new Response<>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage(), null);
    }

    public static <T> Response<T> success(T data){
        return new Response<>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage(), data);
    }

    public static <T> Response<T> fail(){
        return new Response<>(ResponseCode.FAILURE.getCode(), ResponseCode.FAILURE.getMessage(), null);
    }

    public static <T> Response<T> fail(ResponseCode responseCode){
        return new Response<>(responseCode.getCode(), responseCode.getMessage(), null);
    }

    public static <T> Response<T> fail(Integer code, String message){
        return new Response<>(code, message, null);
    }

}