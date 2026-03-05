package com.todo.management.controller;

import com.todo.management.domain.TodoStatus;
import com.todo.management.dto.CreateTodoRequest;
import com.todo.management.dto.PageResponse;
import com.todo.management.dto.TodoResponse;
import com.todo.management.dto.UpdateDescriptionRequest;
import com.todo.management.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/todos")
@RequiredArgsConstructor
public class TodoController {

  private final TodoService service;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TodoResponse create(@Valid @RequestBody CreateTodoRequest req) {
    return TodoResponse.from(service.create(req.description(), req.dueDate()));
  }

  @GetMapping
  public PageResponse<TodoResponse> list(
      @RequestParam(required = false) List<TodoStatus> status,
      Pageable pageable
  ) {
    Page<TodoResponse> page = service.list(status, pageable).map(TodoResponse::from);
    return PageResponse.from(page);
  }

  @GetMapping("/{id}")
  public TodoResponse get(@PathVariable Long id) {
    return TodoResponse.from(service.get(id));
  }

  @PutMapping("/{id}/description")
  public TodoResponse updateDescription(@PathVariable Long id, @Valid @RequestBody UpdateDescriptionRequest req) {
    return TodoResponse.from(service.updateDescription(id, req.description()));
  }

  @PutMapping("/{id}/done")
  public TodoResponse markDone(@PathVariable Long id) {
    return TodoResponse.from(service.markDone(id));
  }

  @PutMapping("/{id}/not-done")
  public TodoResponse markNotDone(@PathVariable Long id) {
    return TodoResponse.from(service.markNotDone(id));
  }
}
