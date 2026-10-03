# MiniBase

A lightweight asynchronous MySQL database service for Java.

MiniBase provides a simple database abstraction built around
[HikariCP](https://github.com/brettwooldridge/HikariCP), connection pooling,
asynchronous database execution, and a small SQL builder.

## Requirements
- Java 21+
- MySQL 8+


## Features

- MySQL support
- HikariCP connection pooling
- Asynchronous database operations
- Dedicated database thread pool
- Automatic connection lifecycle management
- PreparedStatement support
- Batch update support
- SQL query/result mapping
- SQLBuilder for common SQL operations
- Type-safe value handling with `SQLValueType`
- CompletableFuture-based API
- Graceful database and executor shutdown


## Architecture

```text
Application
    │
    ▼
CustomDatabaseManager
    │
    ▼
DatabaseExecutor(`DatabaseThread`)
    │
    ▼
HikariCP
    │
    ▼
MySQL
```

Database queries return `CompletableFuture` so callers do not need to block
while waiting for the database.

## Design Goals

MiniBase is designed to:

- Keep database operations away from the main application thread.
- Reuse connections efficiently through HikariCP.
- Provide a small and predictable database API.
- Reduce repetitive JDBC boilerplate.
- Make database operations easy to compose with `CompletableFuture`.