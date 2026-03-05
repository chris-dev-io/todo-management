package com.todo.management.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
    List<T> items,
    int page,
    int size,
    int count,
    long totalElements,
    int totalPages,
    boolean hasNext
) {

  public static <T> PageResponse<T> from(Page<T> p) {
    return new PageResponse<>(
        p.getContent(),
        p.getNumber(),
        p.getSize(),
        p.getNumberOfElements(),
        p.getTotalElements(),
        p.getTotalPages(),
        p.hasNext()
    );
  }
}
