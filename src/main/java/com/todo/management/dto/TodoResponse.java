package com.todo.management.dto;

import com.todo.management.domain.TodoItem;
import com.todo.management.domain.TodoStatus;

import java.time.Instant;

public record TodoResponse(
    Long id,
    String description,
    TodoStatus status,
    Instant createdAt,
    Instant dueDate,
    Instant doneAt
) {
  public static TodoResponse from(TodoItem item) {
    return new TodoResponse(
        item.getId(),
        item.getDescription(),
        item.getStatus(),
        item.getCreatedAt(),
        item.getDueDate(),
        item.getDoneAt()
    );
  }
}
