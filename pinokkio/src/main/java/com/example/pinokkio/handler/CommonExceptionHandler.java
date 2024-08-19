package com.example.pinokkio.handler;


import com.example.pinokkio.exception.ErrorResponse;
import com.example.pinokkio.exception.base.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(BadInputException.class)
    public ResponseEntity<ErrorResponse> badInputExceptionHandler(BadInputException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(e.getCode())
                .message(e.getMessage())
                .errors(e.getErrors())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> resourceNotFoundExceptionHandler(NotFoundException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(e.getCode())
                .message(e.getMessage())
                .errors(e.getErrors())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ErrorResponse> authorizationExceptionHandler(AuthorizationException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(e.getCode())
                .message(e.getMessage())
                .errors(e.getErrors())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> conflictExceptionHandler(ConflictException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(e.getCode())
                .message(e.getMessage())
                .errors(e.getErrors())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

}
