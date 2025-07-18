package com.project.back_end.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ValidationFailed {

    // 1. Set Up the Global Exception Handler:
    // - Annotate the class with `@RestControllerAdvice` to apply it globally across
    // all controllers.
    // - This class is responsible for handling exceptions and customizing error
    // responses uniformly.

    // 2. Define the `handleValidationException` Method:
    // - Annotate with `@ExceptionHandler(MethodArgumentNotValidException.class)` to
    // intercept validation exceptions thrown when a request body fails `@Valid`
    // checks.
    // - Iterates through all field validation errors from the exception.
    // - Extracts and collects default error messages (e.g., "Email is required",
    // "Invalid phone number").
    // - Constructs a response map containing the error message under the
    // `"message"` key.
    // - Returns a `ResponseEntity` with HTTP 400 Bad Request status and the error
    // message in the body.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException exception) {
        Map<String, Object> map = new HashMap<>();
        for (FieldError e : exception.getFieldErrors()) {
            map.put("message", e.getDefaultMessage());
        }
        return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
    }

}
