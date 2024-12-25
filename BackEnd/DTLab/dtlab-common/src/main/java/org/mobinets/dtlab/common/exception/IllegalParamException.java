package org.mobinets.dtlab.common.exception;

public class IllegalParamException extends BusinessException {

    public IllegalParamException(String message) {
        super(400, message);
    }

}
