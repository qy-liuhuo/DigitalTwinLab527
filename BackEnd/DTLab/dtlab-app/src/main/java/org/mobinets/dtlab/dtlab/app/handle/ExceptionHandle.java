package org.mobinets.dtlab.dtlab.app.handle;

import lombok.extern.log4j.Log4j2;
import org.mobinets.dtlab.common.exception.BusinessException;
import org.mobinets.dtlab.common.response.Response;
import org.mobinets.dtlab.common.response.ResponseCode;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Log4j2
@RestControllerAdvice
public class ExceptionHandle {

    @ExceptionHandler(BusinessException.class)
    public Response<?> businessExceptionHandle(BusinessException e) {
        log.error("BusinessException: {}", e.getMessage());
        return Response.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<?> argumentNotValidHandle(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldErrorList = bindingResult.getFieldErrors();
        StringBuilder sb = new StringBuilder();
        fieldErrorList.forEach(fieldError -> {
            log.error("FieldError: {}", fieldError);
            sb.append(fieldError.getDefaultMessage()).append(";\n");
        });
        return Response.fail(ResponseCode.FAILURE.getCode(), sb.toString());
    }

}
