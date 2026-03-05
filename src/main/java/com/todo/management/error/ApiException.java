package com.todo.management.error;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ApiException extends RuntimeException {
  private final HttpStatus status;
  private final ErrorCode code;
  private final Map<String, Object> details;

  private ApiException(HttpStatus status, ErrorCode code, String message, Map<String, Object> details) {
    super(message);
    this.status = status;
    this.code = code;
    this.details = details == null ? Map.of() : details;
  }

  public HttpStatus status() { return status; }
  public ErrorCode code() { return code; }
  public Map<String, Object> details() { return details; }

  public static ApiException badRequest(ErrorCode code, String message) {
    return new ApiException(HttpStatus.BAD_REQUEST, code, message, Map.of());
  }

  public static ApiException badRequest(ErrorCode code, String message, Map<String, Object> details) {
    return new ApiException(HttpStatus.BAD_REQUEST, code, message, details);
  }

  public static ApiException notFound(ErrorCode code, String message) {
    return new ApiException(HttpStatus.NOT_FOUND, code, message, Map.of());
  }

  public static ApiException conflict(ErrorCode code, String message) {
    return new ApiException(HttpStatus.CONFLICT, code, message, Map.of());
  }

  public static ApiException conflict(ErrorCode code, String message, Map<String, Object> details) {
    return new ApiException(HttpStatus.CONFLICT, code, message, details);
  }
}
