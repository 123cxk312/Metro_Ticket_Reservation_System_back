package com.example.metro.exception;

import com.example.metro.common.api.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        HttpStatus status = exception.getStatus();
        return ResponseEntity
                .status(status)
                .body(ApiResponse.failure(status.value(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException exception
    ) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = fieldError == null ? "请求参数不正确" : fieldError.getDefaultMessage();

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.failure(HttpStatus.BAD_REQUEST.value(), message));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateKeyException() {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(
                        HttpStatus.CONFLICT.value(),
                        "数据已存在或重复提交"
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        log.error("Unexpected server error", exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "服务器内部错误"
                ));
    }
}
