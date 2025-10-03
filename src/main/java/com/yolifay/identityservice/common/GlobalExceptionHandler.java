package com.yolifay.identityservice.common;

import com.yolifay.identityservice.exception.BadRequestException;
import com.yolifay.identityservice.exception.ConflictException;
import com.yolifay.identityservice.exception.DataNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${service.id}")
    private String serviceId;

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<ResponseApiService<Void>> handleDataNotFound(DataNotFoundException e) {
        log.warn("DataNotFound: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ResponseApiUtil.setResponse(
                        HttpStatus.NOT_FOUND.value(),
                        serviceId,
                        Constants.RESPONSE.ACCOUNT_NOT_FOUND,
                        null
                )
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ResponseApiService<String>> handleBadRequest(BadRequestException e) {
        log.warn("BadRequest: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ResponseApiUtil.setResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        serviceId,
                        Constants.RESPONSE.BAD_REQUEST,
                        e.getMessage()
                )
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ResponseApiService<Void>> handleConflict(ConflictException e) {
        log.warn("Conflict: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseApiUtil.setResponse(
                        HttpStatus.CONFLICT.value(),
                        serviceId,
                        Constants.RESPONSE.DATA_EXISTS,
                        null
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseApiService<Void>> handleOthers(Exception e) {
        log.error("Unhandled error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ResponseApiUtil.setResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        serviceId,
                        Constants.RESPONSE.HTTP_INTERNAL_ERROR,
                        null
                )
        );
    }
}