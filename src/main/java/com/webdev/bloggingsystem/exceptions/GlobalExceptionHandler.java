package com.webdev.bloggingsystem.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<String> handleInvalidDateParameterException(){
        return new ResponseEntity<>("Invalid Date Format - must be yyyy-mm-dd", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValid(MethodArgumentNotValidException e){
        Map<String, String> errorMap = new HashMap<>();

        e.getBindingResult().getFieldErrors().forEach((error)->
                errorMap.put(error.getField(), error.getDefaultMessage())
        );

        return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
    }
}
