package com.todo.management.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "todo_items", indexes = {
        @Index(name = "idx_todo_status", columnList = "status"),
        @Index(name = "idx_todo_due_date", columnList = "dueDate")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private long version;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TodoStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant dueDate;

    private Instant doneAt;

    public TodoItem(String description, Instant createdAt, Instant dueDate) {
        this.description = description;
        this.createdAt = createdAt;
        this.dueDate = dueDate;
        this.status = TodoStatus.NOT_DONE;
        this.doneAt = null;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    public void markDone(Instant now) {
        this.status = TodoStatus.DONE;
        this.doneAt = now;
    }

    public void markNotDone() {
        this.status = TodoStatus.NOT_DONE;
        this.doneAt = null;
    }

    public void markPastDue() {
        this.status = TodoStatus.PAST_DUE;
        this.doneAt = null;
    }

    public boolean isNotDone() {
        return this.status == TodoStatus.NOT_DONE;
    }

    public boolean isPastDue() {
        return this.status == TodoStatus.PAST_DUE;
    }
}
