# Todo Service

A REST backend service for managing a to-do list.

## Base URL

http://localhost:8080

------------------------------------------------------------------------

# Consistent API Response

Every JSON response uses the same envelope structure.

## Success Example

``` json
{
  "success": true,
  "data": {
    "id": 12,
    "description": "Read new book",
    "status": "NOT_DONE",
    "createdAt": "2026-03-03T13:30:00Z",
    "dueDate": "2026-03-10T10:00:00Z",
    "doneAt": null
  },
  "error": null,
  "meta": {
    "timestamp": "2026-03-03T13:30:00Z",
    "path": "/todos",
    "requestId": "b0d2b8c7-6a6f-4a77-9c4c-3cfc1b9d7c1a"
  }
}
```

## Failure Example

``` json
{
  "success": false,
  "data": null,
  "error": {
    "code": "INVALID_DUE_DATE",
    "message": "dueDate must be in the future (UTC).",
    "details": {
      "dueDate": "2000-01-01T00:00:00Z",
      "now": "2026-03-03T13:30:00Z"
    }
  },
  "meta": {
    "timestamp": "2026-03-03T13:30:00Z",
    "path": "/todos",
    "requestId": "b0d2b8c7-6a6f-4a77-9c4c-3cfc1b9d7c1a"
  }
}
```

The `meta` field contains request-level information such as `timestamp`,
`path`, and `requestId`.

------------------------------------------------------------------------

# Business Rules

-   All timestamps use **UTC**
-   A todo becomes **PAST_DUE** when `now > dueDate`
-   Past-due todos are **immutable via the REST API (409 Conflict)**
-   `dueDate` must be **strictly in the future** when creating a todo
-   `mark-done` and `mark-not-done` endpoints are **idempotent**

------------------------------------------------------------------------

# Pagination

Pagination follows Spring Data conventions.

Default values:

-   default page: `0`
-   default size: `10`
-   maximum size: `100`
-   default sort: `createdAt,desc`

Example:

GET /todos?page=0&size=10

------------------------------------------------------------------------

# Endpoints

POST /todos
GET /todos
GET /todos/{id}
PUT /todos/{id}/description
PUT /todos/{id}/done
PUT /todos/{id}/not-done

Filtering examples:

GET /todos?status=NOT_DONE
GET /todos?status=DONE
GET /todos?status=PAST_DUE

Multiple filters:

GET /todos?status=NOT_DONE&status=PAST_DUE

------------------------------------------------------------------------

# Example Request

Create a todo

POST /todos

``` json
{
  "description": "Buy new book",
  "dueDate": "2026-03-10T10:00:00Z"
}
```

------------------------------------------------------------------------

# Tech Stack

-   Java 21
-   Spring Boot (Web, Validation, Data JPA)
-   H2 in-memory database
-   JUnit 5 integration tests
-   Docker (multi-stage build)

------------------------------------------------------------------------

# Build and Test

./mvnw clean test

------------------------------------------------------------------------

# Run Locally

./mvnw spring-boot:run

------------------------------------------------------------------------

# Run with Docker

Build the image

docker build -t todo-service .

Run the container

docker run -p 8080:8080 todo-service

------------------------------------------------------------------------

# Notes

-   `/h2-console` is enabled for local debugging
-   The API response wrapper excludes `/h2-console/**` and
    `/actuator/**` to avoid breaking non-JSON responses
