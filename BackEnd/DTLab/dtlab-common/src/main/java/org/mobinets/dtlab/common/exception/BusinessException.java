package org.mobinets.dtlab.common.exception;

import lombok.Getter;
import lombok.Setter;
import org.mobinets.dtlab.common.response.ResponseCode;

import java.io.Serializable;


@Getter
@Setter
public class BusinessException extends RuntimeException{

    int code;
    String message;

    public BusinessException(String message) {
        this.message = message;
        this.code = ResponseCode.FAILURE.getCode();
    }

    public BusinessException(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
