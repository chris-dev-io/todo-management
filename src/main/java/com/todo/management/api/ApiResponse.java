package com.todo.management.api;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.util.Map;

public record ApiResponse<T>(
    boolean success,
    T data,
    ApiError error,
    ApiMeta meta
) {
  public static <T> ApiResponse<T> ok(T data, ApiMeta meta) {
    return new ApiResponse<>(true, data, null, meta);
  }

  public static <T> ApiResponse<T> fail(ApiError error, ApiMeta meta) {
    return new ApiResponse<>(false, null, error, meta);
  }

  public record ApiError(String code, String message, Map<String, Object> details) {}

  public record ApiMeta(Instant timestamp, String path, String requestId) {
    public static ApiMeta of(HttpServletRequest req, Instant now) {
      Object rid = req.getAttribute(RequestIdFilter.ATTR_REQUEST_ID);
      String requestId = rid == null ? "n/a" : rid.toString();
      return new ApiMeta(now, req.getRequestURI(), requestId);
    }
  }
}
