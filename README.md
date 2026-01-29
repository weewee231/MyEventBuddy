# EventBuddy

REST API сервис для управления проектами и задачами с аутентификации.

## Стек технологий

- **Java 17**
- **Spring Boot 3.5**
- **Spring Security** — JWT аутентификация
- **Spring Data JPA** — работа с БД
- **PostgreSQL** — основная база данных
- **Redis** — кэширование
- **Spring Mail** — отправка email
- **Swagger/OpenAPI** — документация API
- **Docker & Docker Compose** — контейнеризация
- **Lombok** — сокращение boilerplate кода

## Функционал

### Аутентификация
- Регистрация с подтверждением email
- Логин с JWT токенами (access + refresh)
- Auto-login по ссылке из письма
- Refresh токены в httpOnly cookies
- Восстановление пароля через email
- Logout с инвалидацией токенов

### Пользователи
- Получение/обновление профиля
- Загрузка аватара
- Удаление аккаунта
- Роли: INDIVIDUAL, COMPANY

### Проекты
- CRUD операции
- Статусы: DRAFT, ACTIVE, COMPLETED, ARCHIVED
- Поиск по названию
- Фильтрация по статусу и дедлайну
- Сортировка по любому полю

## Быстрый старт

### Требования
- Java 17+
- Docker & Docker Compose
- Maven

### Запуск через Docker Compose

```bash
# Клонировать репозиторий
git clone https://github.com/weewee231/Auth_JWT.git
cd Auth_JWT

# Запустить
docker-compose up -d
```

### Локальный запуск

```bash
# Запустить PostgreSQL и Redis
docker-compose up -d postgres redis

# Запустить приложение
./mvnw spring-boot:run
```

Приложение будет доступно на `http://localhost:8080`

## Переменные окружения

| Переменная | Описание | Пример |
|------------|----------|--------|
| `DB_URL` | URL базы данных | `jdbc:postgresql://localhost:5432/eventbuddy` |
| `DB_USERNAME` | Пользователь БД | `postgres` |
| `DB_PASSWORD` | Пароль БД | `password` |
| `JWT_SECRET` | Секретный ключ для JWT | `your-secret-key` |
| `JWT_EXPIRATION` | Время жизни access токена (мс) | `3600000` |
| `MAIL_HOST` | SMTP сервер | `smtp.gmail.com` |
| `MAIL_PORT` | Порт SMTP | `587` |
| `MAIL_USERNAME` | Email отправителя | `your@email.com` |
| `MAIL_PASSWORD` | Пароль приложения | `app-password` |

## API Endpoints

### Аутентификация `/auth`

| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/auth/signup` | Регистрация |
| POST | `/auth/login` | Авторизация |
| POST | `/auth/verify` | Подтверждение email |
| POST | `/auth/refresh` | Обновление токена |
| POST | `/auth/logout` | Выход |
| POST | `/auth/recovery` | Запрос восстановления пароля |
| POST | `/auth/reset-password` | Сброс пароля |
| GET/POST | `/auth/auto-login` | Автоматический вход по ссылке |

### Пользователи `/users`

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/users/me` | Получить профиль |
| PUT | `/users/me` | Обновить профиль |
| DELETE | `/users/me` | Удалить аккаунт |
| GET | `/users/` | Список всех пользователей |
| POST | `/user/avatar` | Загрузить аватар |

### Проекты `/projects`

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/projects` | Все проекты пользователя |
| GET | `/projects/search` | Поиск с фильтрами |
| GET | `/projects/{id}` | Получить проект |
| POST | `/projects` | Создать проект |
| PUT | `/projects/{id}` | Обновить проект |
| DELETE | `/projects/{id}` | Удалить проект |

### Параметры поиска `/projects/search`

| Параметр | Тип | Описание |
|----------|-----|----------|
| `search` | String | Поиск по названию |
| `status` | Enum | DRAFT, ACTIVE, COMPLETED, ARCHIVED |
| `deadlineFrom` | DateTime | Дедлайн от |
| `deadlineTo` | DateTime | Дедлайн до |
| `sortBy` | String | Поле сортировки |
| `sortDirection` | String | ASC / DESC |

## Документация API

Swagger UI доступен по адресу: `http://localhost:8080/swagger-ui.html`

## Структура проекта

```
src/main/java/com/eventbuddy/eventbuddydemo/
├── config/          # Конфигурации (Security, JWT, CORS, Mail)
├── controller/      # REST контроллеры
├── dto/             # Data Transfer Objects
├── exception/       # Обработка ошибок
├── model/           # JPA сущности
├── repository/      # Репозитории и спецификации
├── service/         # Бизнес-логика
└── validation/      # Кастомные валидаторы
```
