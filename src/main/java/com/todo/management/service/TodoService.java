package com.todo.management.service;

import com.todo.management.domain.TodoItem;
import com.todo.management.domain.TodoStatus;
import com.todo.management.error.Errors;
import com.todo.management.error.ApiException;
import com.todo.management.error.ErrorCode;
import com.todo.management.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TodoService {

  private final TodoRepository repo;
  private final Clock clock;

  @Transactional
  public TodoItem create(String description, Instant dueDate) {
    Instant now = Instant.now(clock);

    String desc = description == null ? null : description.trim();
    if (desc == null || desc.isBlank()) {
      throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "description must not be blank");
    }

    if (dueDate == null) {
      throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "dueDate must not be null");
    }

    if (!dueDate.isAfter(now)) {
      log.warn("todo.create.rejected reason=invalid_due_date dueDate={} now={}", dueDate, now);
      throw Errors.invalidDueDate(dueDate, now);
    }

    TodoItem saved = repo.save(new TodoItem(desc, now, dueDate));
    log.info("todo.created id={} dueDate={}", saved.getId(), saved.getDueDate());
    return saved;
  }

  @Transactional
  public TodoItem get(Long id) {
    Instant now = Instant.now(clock);

    TodoItem item = repo.findById(id).orElseThrow(() -> Errors.todoNotFound(id));

    if (item.isNotDone() && now.isAfter(item.getDueDate())) {
      item.markPastDue();
      log.info("todo.autoPastDue.singleUpdated id={} now={}", id, now);
    }

    return item;
  }

  @Transactional
  public Page<TodoItem> list(List<TodoStatus> statuses, Pageable pageable) {
    Instant now = Instant.now(clock);

    int updated = repo.markOverdueAsPastDue(now, TodoStatus.NOT_DONE, TodoStatus.PAST_DUE);
    if (updated > 0) {
      log.info("todo.autoPastDue.bulkUpdated count={} now={}", updated, now);
    }

    if (statuses == null || statuses.isEmpty()) {
      return repo.findAll(pageable);
    }

    return repo.findAllByStatusIn(statuses, pageable);
  }

  @Transactional
  public TodoItem updateDescription(Long id, String newDescription) {
    TodoItem item = repo.findById(id).orElseThrow(() -> Errors.todoNotFound(id));
    forbidIfOverdueOrPastDue(item);

    String desc = newDescription == null ? null : newDescription.trim();
    if (desc == null || desc.isBlank()) {
      throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "description must not be blank");
    }

    item.changeDescription(desc);
    log.info("todo.descriptionUpdated id={}", id);
    return item;
  }

  @Transactional
  public TodoItem markDone(Long id) {
    TodoItem item = repo.findById(id).orElseThrow(() -> Errors.todoNotFound(id));
    forbidIfOverdueOrPastDue(item);

    if (item.getStatus() == TodoStatus.DONE) return item;
    item.markDone(Instant.now(clock));
    log.info("todo.markDone id={}", id);
    return item;
  }

  @Transactional
  public TodoItem markNotDone(Long id) {
    TodoItem item = repo.findById(id).orElseThrow(() -> Errors.todoNotFound(id));
    forbidIfOverdueOrPastDue(item);

    if (item.getStatus() == TodoStatus.NOT_DONE) return item;
    item.markNotDone();
    log.info("todo.markNotDone id={}", id);
    return item;
  }

  private void forbidIfOverdueOrPastDue(TodoItem item) {
    Instant now = Instant.now(clock);

    if (item.isNotDone() && now.isAfter(item.getDueDate())) {
      item.markPastDue();
      log.warn("todo.writeRejected reason=past_due id={} dueDate={} now={}", item.getId(), item.getDueDate(), now);
      throw Errors.pastDueImmutable(item.getId());
    }

    if (item.isPastDue()) {
      log.warn("todo.writeRejected reason=past_due id={}", item.getId());
      throw Errors.pastDueImmutable(item.getId());
    }
  }
}
