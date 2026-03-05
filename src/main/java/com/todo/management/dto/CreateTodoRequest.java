package com.todo.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateTodoRequest(
    @NotBlank(message = "description must not be blank")
    @Size(max = 1000, message = "description must be at most 1000 characters")
    String description,

    @NotNull(message = "dueDate must not be null")
    Instant dueDate
) {}
