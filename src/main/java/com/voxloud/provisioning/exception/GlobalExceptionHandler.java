package com.voxloud.provisioning.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDeviceNotFoundException(DeviceNotFoundException e) {
        log.warn("Device not found: {}", e.getMessage());
        return new ResponseEntity<>(new ErrorResponse("DEVICE_NOT_FOUND", e.getMessage()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProvisioningException.class)
    public ResponseEntity<ErrorResponse> handleProvisioningException(ProvisioningException e) {
        log.error("Provisioning error: {}", e.getMessage());
        return new ResponseEntity<>(new ErrorResponse("PROVISIONING_ERROR", e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        return new ResponseEntity<>(new ErrorResponse("INTERNAL_SERVER_ERROR", "An unexpected error occurred"),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    static class ErrorResponse {
        private final String code;
        private final String message;

        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}