package com.todo.management;

import com.todo.management.api.RequestIdFilter;
import com.todo.management.repository.TodoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestClockConfig.class)
class TodoApiIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired MutableClock clock;

    @Autowired TodoRepository repo;

    @BeforeEach
    void resetState() {
        repo.deleteAll();
        clock.setInstant(Instant.parse("2026-03-03T10:00:00Z"));
    }

    @Test
    void create_success_is_enveloped_and_sets_request_id_header() throws Exception {
        var body = om.writeValueAsString(new CreateReq("Read new book", Instant.parse("2026-03-10T10:00:00Z")));

        mvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists(RequestIdFilter.HEADER_REQUEST_ID))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.description").value("Read new book"))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.meta.path").value("/todos"))
                .andExpect(jsonPath("$.meta.requestId").value(not(emptyString())));
    }

    @Test
    void create_invalid_due_date_returns_400_with_specific_error() throws Exception {
        var body = om.writeValueAsString(new CreateReq("Bad", Instant.parse("2000-01-01T00:00:00Z")));

        mvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.error.code").value("INVALID_DUE_DATE"))
                .andExpect(jsonPath("$.error.message").value(containsString("dueDate must be in the future")))
                .andExpect(jsonPath("$.meta.path").value("/todos"));
    }

    @Test
    void create_malformed_json_returns_400_malformed_json() throws Exception {
        mvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("MALFORMED_JSON"));
    }

    @Test
    void past_due_becomes_immutable_and_is_reported() throws Exception {
        var createBody = om.writeValueAsString(new CreateReq("Task", Instant.parse("2026-03-03T10:00:05Z")));

        var createRes = mvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        String json = createRes.getResponse().getContentAsString();
        long id = om.readTree(json).path("data").path("id").asLong();

        clock.advanceSeconds(10);

        var updBody = om.writeValueAsString(new UpdateDescReq("new"));

        mvc.perform(put("/todos/" + id + "/description")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PAST_DUE_IMMUTABLE"));

        mvc.perform(get("/todos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PAST_DUE"));
    }

    @Test
    void list_is_paginated_and_can_filter_by_status() throws Exception {
        var a = om.writeValueAsString(new CreateReq("A", Instant.parse("2026-03-10T10:00:00Z")));
        var b = om.writeValueAsString(new CreateReq("B", Instant.parse("2026-03-10T10:00:00Z")));

        long idA = om.readTree(
                mvc.perform(post("/todos").contentType(MediaType.APPLICATION_JSON).content(a))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        ).path("data").path("id").asLong();

        long idB = om.readTree(
                mvc.perform(post("/todos").contentType(MediaType.APPLICATION_JSON).content(b))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        ).path("data").path("id").asLong();

        mvc.perform(put("/todos/" + idB + "/done"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DONE"));

        mvc.perform(get("/todos")
                        .param("status", "NOT_DONE")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.count").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value((int) idA))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10));

        mvc.perform(get("/todos")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.count").value(2))
                .andExpect(jsonPath("$.data.items[*].id", hasItem((int) idA)))
                .andExpect(jsonPath("$.data.items[*].id", hasItem((int) idB)));
    }

    record CreateReq(String description, Instant dueDate) {}
    record UpdateDescReq(String description) {}
}