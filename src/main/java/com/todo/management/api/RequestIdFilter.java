package com.todo.management.api;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class RequestIdFilter implements Filter {
  public static final String ATTR_REQUEST_ID = "requestId";
  public static final String HEADER_REQUEST_ID = "X-Request-Id";
  private static final String MDC_REQUEST_ID = "requestId";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;

    String requestId = req.getHeader(HEADER_REQUEST_ID);
    if (requestId == null || requestId.isBlank()) {
      requestId = UUID.randomUUID().toString();
    }

    req.setAttribute(ATTR_REQUEST_ID, requestId);
    res.setHeader(HEADER_REQUEST_ID, requestId);

    MDC.put(MDC_REQUEST_ID, requestId);
    long start = System.currentTimeMillis();

    try {
      log.info("request.start method={} path={}", req.getMethod(), req.getRequestURI());
      chain.doFilter(req, res);
    } finally {
      long ms = System.currentTimeMillis() - start;
      log.info("request.end method={} path={} status={} durationMs={}",
          req.getMethod(), req.getRequestURI(), res.getStatus(), ms);
      MDC.remove(MDC_REQUEST_ID);
    }
  }
}
