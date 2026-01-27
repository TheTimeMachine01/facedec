package com.application.facedec.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.security.SignatureException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<Object> handleSignatureException(SignatureException ex) {
        ErrorResponse errorResponse = new ErrorResponse("Invalid or corrupted token signature.", false);
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    public ResponseEntity<Object> handleError(String message,Boolean status, HttpStatus httpStatus) {
        ErrorResponse handleErrorResp = new ErrorResponse(message, status);
        return new ResponseEntity<>(handleErrorResp, httpStatus);
    }


    @Setter
    @Getter
    static class ErrorResponse {
        private String message;
        private Boolean status;

        public ErrorResponse(String message, Boolean status) {
            this.message = message;
            this.status = status;
        }

    }
}
