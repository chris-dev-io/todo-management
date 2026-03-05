package com.todo.management.error;

import com.todo.management.api.ApiResponse;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiResponse<Void>> handleApi(ApiException ex, HttpServletRequest req) {
    if (ex.status().is4xxClientError()) {
      log.warn("request.failed status={} code={} path={} msg={}",
          ex.status().value(), ex.code(), req.getRequestURI(), ex.getMessage());
    } else {
      log.error("request.failed status={} code={} path={} msg={}",
          ex.status().value(), ex.code(), req.getRequestURI(), ex.getMessage(), ex);
    }

    return ResponseEntity.status(ex.status())
        .body(ApiResponse.fail(
            new ApiResponse.ApiError(ex.code().name(), ex.getMessage(), ex.details()),
            ApiResponse.ApiMeta.of(req, Instant.now())
        ));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    Map<String, Object> details = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(err -> details.put(err.getField(), err.getDefaultMessage()));

    log.warn("request.failed status=400 code={} path={} msg=Validation failed", ErrorCode.VALIDATION_ERROR, req.getRequestURI());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.fail(
            new ApiResponse.ApiError(ErrorCode.VALIDATION_ERROR.name(), "Validation failed", details),
            ApiResponse.ApiMeta.of(req, Instant.now())
        ));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
    log.warn("request.failed status=400 code={} path={} msg=Malformed JSON request body", ErrorCode.MALFORMED_JSON, req.getRequestURI());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.fail(
            new ApiResponse.ApiError(ErrorCode.MALFORMED_JSON.name(), "Malformed JSON request body", Map.of()),
            ApiResponse.ApiMeta.of(req, Instant.now())
        ));
  }

  @ExceptionHandler({OptimisticLockingFailureException.class, OptimisticLockException.class})
  public ResponseEntity<ApiResponse<Void>> handleOptimistic(Exception ex, HttpServletRequest req) {
    ApiException apiEx = Errors.concurrentModification();
    log.warn("request.failed status=409 code={} path={} msg={}", apiEx.code(), req.getRequestURI(), apiEx.getMessage());

    return ResponseEntity.status(apiEx.status())
        .body(ApiResponse.fail(
            new ApiResponse.ApiError(apiEx.code().name(), apiEx.getMessage(), Map.of()),
            ApiResponse.ApiMeta.of(req, Instant.now())
        ));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex, HttpServletRequest req) {
    log.error("request.failed status=500 code={} path={}", ErrorCode.INTERNAL_ERROR, req.getRequestURI(), ex);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.fail(
            new ApiResponse.ApiError(ErrorCode.INTERNAL_ERROR.name(), "Unexpected error", Map.of()),
            ApiResponse.ApiMeta.of(req, Instant.now())
        ));
  }
}
