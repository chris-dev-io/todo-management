package com.todo.management.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.time.Instant;

@ControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

  @Override
  public boolean supports(MethodParameter returnType, Class converterType) {
    return !ApiResponse.class.isAssignableFrom(returnType.getParameterType());
  }

  @Override
  public Object beforeBodyWrite(Object body,
                                MethodParameter returnType,
                                MediaType selectedContentType,
                                Class selectedConverterType,
                                ServerHttpRequest request,
                                ServerHttpResponse response) {

    String path = request.getURI().getPath();

    if (path.startsWith("/h2-console")) return body;
    if (path.startsWith("/actuator")) return body;

    if (!MediaType.APPLICATION_JSON.isCompatibleWith(selectedContentType)) return body;

    if (body instanceof ApiResponse<?> ) return body;

    HttpServletRequest servletReq = ((ServletServerHttpRequest) request).getServletRequest();
    return ApiResponse.ok(body, ApiResponse.ApiMeta.of(servletReq, Instant.now()));
  }
}
