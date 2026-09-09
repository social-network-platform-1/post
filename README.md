# Post Service

Microservice responsible for creating, managing and retrieving posts in the social network platform.

## Overview

The Post Service manages user posts and their visibility.

The service supports public, private and friends-only posts. For friends-only visibility, it synchronously communicates with the Friendship Service to verify the relationship between users.

Redis is used for caching, while Kafka is used for publishing post-related events.

## Features

- Create posts
- Get posts
- Update posts
- Delete posts
- Get posts by author
- Pagination for posts by author
- Public, private and friends-only visibility
- Redis caching
- Kafka event publishing
- JWT-based authentication
- PostgreSQL persistence
- Liquibase database migrations
- Internal API for retrieving post ownership information
- Integration testing with Testcontainers

## Post Visibility

Posts support three visibility levels:

- `PUBLIC` — accessible to users according to the public visibility rules
- `PRIVATE` — accessible only to the post owner
- `FRIENDS` — accessible to friends of the post owner

For `FRIENDS` posts, the service uses the Friendship Service internal API to check whether the requesting user and post owner are friends.

## Architecture

The service follows a layered architecture:

- Controller layer — handles HTTP requests
- Service layer — contains post business logic
- Repository layer — handles PostgreSQL access
- Mapper layer — converts entities and DTOs
- Security layer — validates JWT access tokens
- Cache layer — provides Redis caching
- Kafka integration — publishes post-related events
- Client layer — communicates with the Friendship Service

### Communication

The Post Service communicates with other services through:

- REST API for client requests
- Internal REST API for service-to-service communication
- Kafka for asynchronous event publishing

## Events

The service publishes events for post state changes.

Supported event types include:

- `PostCreatedEvent`
- `PostUpdatedEvent`
- `PostDeletedEvent`

Kafka is used to deliver these events to other services.

## API

### Public API

Base path:

`/api/v1/posts`

Available operations:

- Create a post
- Get a post
- Update a post
- Delete a post
- Get posts by author with pagination

### Internal API

Internal endpoint:

`/internal/posts/{postId}/owner`

This endpoint provides post ownership information for internal service-to-service communication.

## Authentication

The service uses JWT Bearer authentication.

JWT tokens are issued by the User Service and validated by the Post Service as an OAuth2 Resource Server.

Configuration:

- JWT issuer: `user-service`
- Access token TTL: 15 minutes
- JWT secret: `SECURITY_JWT_SECRET`

## Caching

Redis is used for caching post-related data.

The cache reduces repeated database access for frequently requested posts.

Redis configuration:

- Host: `redis`
- Port: `6379`

## Database

The service uses PostgreSQL as its primary relational database.

Database configuration:

- Host: `post-postgres`
- Port: `5432`
- Database: `postdb`

Liquibase is used for database schema management and versioned migrations.

Hibernate schema validation is enabled:

`ddl-auto: validate`

Database migrations are stored under:

`src/main/resources/db/changelog`

## Security

The service is protected with Spring Security and OAuth2 Resource Server.

Main security components:

- Spring Security
- OAuth2 Resource Server
- JWT
- Request validation

## Observability

The project includes Spring Boot testing support for Actuator.

## Testing

The project uses:

- JUnit
- Spring Security Test
- Spring Boot test support
- Testcontainers
- PostgreSQL Testcontainer

Integration tests can run PostgreSQL in a containerized environment.

## Docker

The service includes Docker and Docker Compose support.

The local infrastructure includes:

- Post Service
- PostgreSQL
- Redis
- Kafka

The service runs on port `8080`.

## Technologies

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Security
- OAuth2 Resource Server
- JWT
- Spring Data JPA
- PostgreSQL
- Liquibase
- Redis
- Apache Kafka
- MapStruct
- Lombok
- Gradle
- Docker
- Docker Compose
- JUnit
- Testcontainers

## Project Structure

```text
post/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/post/
│   │   └── resources/
│   │       └── db/
│   │           └── changelog/
│   └── test/
├── gradle/
├── Dockerfile
├── compose.yaml
├── build.gradle
├── settings.gradle
├── gradlew
└── README.md
```

## Running Locally

### Prerequisites

- Java 21
- Docker
- Docker Compose

### Configuration

Set the JWT secret:

`SECURITY_JWT_SECRET=<your-secret>`

The service uses the following infrastructure configuration:

- PostgreSQL: `post-postgres:5432`
- Redis: `redis:6379`
- Kafka: `kafka:9092`

### Run with Gradle

On Windows:

```powershell
.\gradlew.bat bootRun
```

### Run tests

```powershell
.\gradlew.bat test
```

## Part of Social Network Platform

Post Service is one of the microservices of the Social Network Platform.

Related services:

- User Service
- Friendship Service
- Like Service
- Comment Service
- Notification Service

---

# Русская версия

## Обзор

Post Service отвечает за создание, получение, изменение и удаление публикаций в социальной сети.

Сервис поддерживает несколько уровней видимости постов: публичные, приватные и доступные друзьям.

Для постов с видимостью `FRIENDS` сервис синхронно обращается к Friendship Service для проверки дружбы между пользователями.

Для кэширования используется Redis, а для публикации событий — Kafka.

## Возможности

- Создание постов
- Получение постов
- Изменение постов
- Удаление постов
- Получение постов пользователя
- Пагинация постов автора
- Публичные посты
- Приватные посты
- Посты только для друзей
- Redis-кэширование
- Публикация событий через Kafka
- JWT-аутентификация
- PostgreSQL
- Liquibase
- Internal API для получения владельца поста
- Интеграционное тестирование с Testcontainers

## Видимость постов

Поддерживаются три уровня видимости:

- `PUBLIC` — публичный пост
- `PRIVATE` — пост доступен только владельцу
- `FRIENDS` — пост доступен друзьям владельца

Для `FRIENDS` сервис использует внутренний API Friendship Service и проверяет, являются ли пользователь и владелец поста друзьями.

## Архитектура

Сервис построен с использованием многоуровневой архитектуры:

- Controller — обработка HTTP-запросов
- Service — бизнес-логика работы с постами
- Repository — работа с PostgreSQL
- Mapper — преобразование Entity и DTO
- Security — проверка JWT
- Cache — кэширование в Redis
- Kafka integration — публикация событий
- Client — взаимодействие с Friendship Service

### Взаимодействие

Post Service взаимодействует с другими сервисами через:

- REST API
- Internal REST API
- Kafka

## События

Сервис публикует события при изменении состояния постов.

Используются события:

- `PostCreatedEvent`
- `PostUpdatedEvent`
- `PostDeletedEvent`

Для передачи событий используется Kafka.

## API

### Public API

Базовый путь:

`/api/v1/posts`

Основные операции:

- Создание поста
- Получение поста
- Изменение поста
- Удаление поста
- Получение постов автора с пагинацией

### Internal API

Endpoint:

`/internal/posts/{postId}/owner`

Используется для получения информации о владельце поста при внутреннем взаимодействии между сервисами.

## Аутентификация

Сервис использует JWT Bearer Authentication.

JWT-токены выдаются User Service и проверяются Post Service как OAuth2 Resource Server.

Конфигурация:

- JWT issuer: `user-service`
- Access token TTL: 15 минут
- JWT secret: `SECURITY_JWT_SECRET`

## Кэширование

Redis используется для кэширования данных о постах и уменьшения количества повторных запросов к PostgreSQL.

Конфигурация:

- Host: `redis`
- Port: `6379`

## База данных

Основная база данных — PostgreSQL.

Конфигурация:

- Host: `post-postgres`
- Port: `5432`
- Database: `postdb`

Liquibase используется для управления миграциями.

Hibernate работает в режиме проверки схемы:

`ddl-auto: validate`

Миграции находятся в:

`src/main/resources/db/changelog`

## Безопасность

Для защиты API используются:

- Spring Security
- OAuth2 Resource Server
- JWT
- Валидация входных данных

## Тестирование

В проекте используются:

- JUnit
- Spring Security Test
- Spring Boot Test
- Testcontainers
- PostgreSQL Testcontainer

## Docker

Сервис поддерживает Docker и Docker Compose.

Используемая инфраструктура:

- Post Service
- PostgreSQL
- Redis
- Kafka

Порт сервиса: `8080`.

## Технологии

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Security
- OAuth2 Resource Server
- JWT
- Spring Data JPA
- PostgreSQL
- Liquibase
- Redis
- Apache Kafka
- MapStruct
- Lombok
- Gradle
- Docker
- Docker Compose
- JUnit
- Testcontainers

## Структура проекта

```text
post/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/post/
│   │   └── resources/
│   │       └── db/
│   │           └── changelog/
│   └── test/
├── gradle/
├── Dockerfile
├── compose.yaml
├── build.gradle
├── settings.gradle
├── gradlew
└── README.md
```

## Локальный запуск

### Требования

- Java 21
- Docker
- Docker Compose

### Конфигурация

Установить JWT secret:

`SECURITY_JWT_SECRET=<your-secret>`

Инфраструктура:

- PostgreSQL: `post-postgres:5432`
- Redis: `redis:6379`
- Kafka: `kafka:9092`

### Запуск

Для Windows:

```powershell
.\gradlew.bat bootRun
```

### Тесты

```powershell
.\gradlew.bat test
```

## Часть Social Network Platform

Post Service является одним из микросервисов Social Network Platform.

Связанные сервисы:

- User Service
- Friendship Service
- Like Service
- Comment Service
- Notification Service
