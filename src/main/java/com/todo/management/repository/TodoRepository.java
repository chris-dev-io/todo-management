package com.todo.management.repository;

import com.todo.management.domain.TodoItem;
import com.todo.management.domain.TodoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;

public interface TodoRepository extends JpaRepository<TodoItem, Long> {

  Page<TodoItem> findAllByStatusIn(Collection<TodoStatus> statuses, Pageable pageable);

  @Modifying
  @Query("""
      update TodoItem t
      set t.status = :pastDue,
          t.doneAt = null
      where t.status = :notDone
        and t.dueDate < :now
      """)
  int markOverdueAsPastDue(@Param("now") Instant now,
                         @Param("notDone") TodoStatus notDone,
                         @Param("pastDue") TodoStatus pastDue);
}
