package com.todo.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDescriptionRequest(
    @NotBlank(message = "description must not be blank")
    @Size(max = 1000, message = "description must be at most 1000 characters")
    String description
) {}
