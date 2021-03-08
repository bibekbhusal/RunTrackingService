package com.bhusalb.runtrackingservice.configuration;

import com.bhusalb.runtrackingservice.exceptions.ResourceNotFoundException;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LogManager.getLogger();

    @ExceptionHandler (ResourceNotFoundException.class)
    public ResponseEntity<ApiCallError<String>> handleNotFoundException (final HttpServletRequest request,
                                                                         final ResourceNotFoundException ex) {
        logger.error("NotFoundException {}\n", request.getRequestURI(), ex);

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ApiCallError<>("Not found exception", Lists.newArrayList(ex.getMessage())));
    }

    @ExceptionHandler (ValidationException.class)
    public ResponseEntity<ApiCallError<String>> handleValidationException (final HttpServletRequest request,
                                                                           final ValidationException ex) {
        logger.error("ValidationException {}\n", request.getRequestURI(), ex);

        return ResponseEntity
            .badRequest()
            .body(new ApiCallError<>("Validation exception", Lists.newArrayList(ex.getMessage())));
    }

    @ExceptionHandler (IllegalArgumentException.class)
    public ResponseEntity<ApiCallError<String>> handleIllegalArgumentException (final HttpServletRequest request,
                                                                                final IllegalArgumentException ex) {
        logger.error("IllegalArgumentException {}\n", request.getRequestURI(), ex);

        return ResponseEntity
            .badRequest()
            .body(new ApiCallError<>("Invalid input provided.", Lists.newArrayList(ex.getMessage())));
    }

    @ExceptionHandler (MissingServletRequestParameterException.class)
    public ResponseEntity<ApiCallError<String>> handleMissingServletRequestParameterException (
        final HttpServletRequest request, final MissingServletRequestParameterException ex) {

        logger.error("handleMissingServletRequestParameterException {}\n", request.getRequestURI(), ex);

        return ResponseEntity
            .badRequest()
            .body(new ApiCallError<>("Missing request parameter", Lists.newArrayList(ex.getMessage())));
    }

    @ExceptionHandler (MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiCallError<Map<String, String>>> handleMethodArgumentTypeMismatchException (
        final HttpServletRequest request, final MethodArgumentTypeMismatchException ex) {
        logger.error("handleMethodArgumentTypeMismatchException {}\n", request.getRequestURI(), ex);

        Map<String, String> details = new HashMap<>();
        details.put("paramName", ex.getName());
        details.put("paramValue", Optional.ofNullable(ex.getValue()).map(Object::toString).orElse(""));
        details.put("errorMessage", ex.getMessage());

        return ResponseEntity
            .badRequest()
            .body(new ApiCallError<>("Method argument type mismatch", Lists.newArrayList(details)));
    }

    @ExceptionHandler (MethodArgumentNotValidException.class)
    public ResponseEntity<ApiCallError<Map<String, String>>> handleMethodArgumentNotValidException (
        final HttpServletRequest request, final MethodArgumentNotValidException ex) {
        logger.error("handleMethodArgumentNotValidException {}\n", request.getRequestURI(), ex);

        List<Map<String, String>> details = new ArrayList<>();
        ex.getBindingResult()
            .getFieldErrors()
            .forEach(fieldError -> {
                Map<String, String> detail = new HashMap<>();
                detail.put("objectName", fieldError.getObjectName());
                detail.put("field", fieldError.getField());
                detail.put("rejectedValue", "" + fieldError.getRejectedValue());
                detail.put("errorMessage", fieldError.getDefaultMessage());
                details.add(detail);
            });

        return ResponseEntity
            .badRequest()
            .body(new ApiCallError<>("Method argument validation failed", details));
    }

    @ExceptionHandler (AccessDeniedException.class)
    public ResponseEntity<ApiCallError<String>> handleAccessDeniedException (final HttpServletRequest request,
                                                                             final AccessDeniedException ex) {
        logger.error("handleAccessDeniedException {}\n", request.getRequestURI(), ex);

        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(new ApiCallError<>("Access denied!", Lists.newArrayList(ex.getMessage())));
    }

    @ExceptionHandler (AuthenticationException.class)
    public ResponseEntity<ApiCallError<String>> handleAuthenticationException (final HttpServletRequest request,
                                                                               final AuthenticationException ex) {
        logger.error("handleAuthenticationException {}\n", request.getRequestURI(), ex);

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(new ApiCallError<>("Unable to authenticate.", Lists.newArrayList(ex.getMessage())));
    }

    @ExceptionHandler (HttpClientErrorException.class)
    public ResponseEntity<ApiCallError<String>> handleHttpClientErrorException (final HttpServletRequest request,
                                                                                final HttpClientErrorException ex) {
        logger.error("handleHttpClientErrorException {}\n", request.getRequestURI(), ex);

        return ResponseEntity
            .status(ex.getStatusCode())
            .body(new ApiCallError<>(ex.getStatusText(), Lists.newArrayList(ex.getMessage())));
    }

    @ExceptionHandler (Exception.class)
    public ResponseEntity<ApiCallError<String>> handleInternalServerError (final HttpServletRequest request,
                                                                           final Exception ex) {
        logger.error("handleInternalServerError {}\n", request.getRequestURI(), ex);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiCallError<>("Internal server error", Lists.newArrayList(ex.getMessage())));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiCallError<T> {
        private String message;
        private List<T> details;
    }
}
