package com.todo.management.error;

import java.time.Instant;
import java.util.Map;

public final class Errors {
  private Errors() {}

  public static ApiException todoNotFound(long id) {
    return ApiException.notFound(ErrorCode.TODO_NOT_FOUND, "Todo item not found: " + id);
  }

  public static ApiException pastDueImmutable(long id) {
    return ApiException.conflict(ErrorCode.PAST_DUE_IMMUTABLE, "Past due items cannot be modified: " + id);
  }

  public static ApiException invalidDueDate(Instant dueDate, Instant now) {
    return ApiException.badRequest(
        ErrorCode.INVALID_DUE_DATE,
        "dueDate must be in the future (UTC).",
        Map.of("dueDate", dueDate.toString(), "now", now.toString())
    );
  }

  public static ApiException concurrentModification() {
    return ApiException.conflict(ErrorCode.CONCURRENT_MODIFICATION, "Concurrent modification detected. Please retry.");
  }
}
