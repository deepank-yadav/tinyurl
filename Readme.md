# 🔗 TinyURL — Spring Boot URL Shortener

[![Java](https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Lombok](https://img.shields.io/badge/Lombok-1.18.32-pink)](https://projectlombok.org/)
[![Swagger UI](https://img.shields.io/badge/Swagger-UI-85EA2D?logo=swagger&logoColor=black)](http://localhost:8080/swagger-ui.html)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

A clean, production-ready **URL shortening service** built with Spring Boot 3, Spring Data JPA, Swagger UI, and Spring Boot DevTools. Swap the bundled H2 in-memory database for MySQL or PostgreSQL with a single config change.

---

## ✨ Features

| Feature | Details |
|---|---|
| 🔗 URL Shortening | Accepts any valid `http://` or `https://` URL |
| 🏷️ Custom Aliases | Define your own short code, e.g. `/my-blog` |
| ⏱️ Link Expiry | Optional TTL in days — expired links return `410 Gone` |
| ♻️ De-duplication | Same long URL always returns the same short code |
| 📊 Click Tracking | Atomic counter incremented on every redirect |
| 📈 Stats API | Click count, creation date, and expiry per link |
| 📖 Swagger UI | Interactive docs at `/swagger-ui.html` |
| ⚡ DevTools | Hot restart + LiveReload in development |
| 🗄️ H2 Console | In-browser DB viewer at `/h2-console` |

---

## 🛠️ Getting Started

### Prerequisites

- Java **17+** (tested on JDK 17 and JDK 21)
- Maven **3.8+**

### Clone & Run

```bash
git clone https://github.com/your-org/tinyurl.git
cd tinyurl
mvn spring-boot:run
```

The app starts on **`http://localhost:8080`**.

### Key URLs

| URL | Description |
|---|---|
| `http://localhost:8080/swagger-ui.html` | Interactive Swagger UI |
| `http://localhost:8080/v3/api-docs` | Raw OpenAPI 3 JSON spec |
| `http://localhost:8080/h2-console` | H2 in-memory DB console |

> **H2 Console settings:** JDBC URL `jdbc:h2:mem:tinyurldb` · User `sa` · Password *(blank)*

---

## 📡 API Reference

### POST `/api/shorten` — Shorten a URL

```http
POST /api/shorten
Content-Type: application/json
```

```json
{
  "url":         "https://www.example.com/some/very/long/path?query=value",
  "customAlias": "my-link",
  "expiryDays":  30
}
```

> `customAlias` and `expiryDays` are both **optional**.

**Response `201 Created`**

```json
{
  "originalUrl": "https://www.example.com/some/very/long/path?query=value",
  "shortUrl":    "http://localhost:8080/my-link",
  "shortCode":   "my-link",
  "createdAt":   "2026-03-01T10:30:00",
  "expiresAt":   "2026-03-31T10:30:00"
}
```

---

### GET `/{shortCode}` — Redirect

Returns **`302 Found`** with a `Location` header pointing to the original URL. The click counter is incremented atomically on every call.

```bash
curl -L http://localhost:8080/my-link
```

---

### GET `/api/stats/{shortCode}` — Statistics

```http
GET /api/stats/my-link
```

**Response `200 OK`**

```json
{
  "shortCode":   "my-link",
  "originalUrl": "https://www.example.com/...",
  "clickCount":  42,
  "createdAt":   "2026-03-01T10:30:00",
  "expiresAt":   "2026-03-31T10:30:00"
}
```

---

## ⚠️ Error Responses

| Scenario | HTTP Status |
|---|---|
| Invalid URL format | `400 Bad Request` |
| URL / short code not found | `400 Bad Request` |
| Custom alias already taken | `400 Bad Request` |
| Short URL has expired | `410 Gone` |

---

## 🗄️ Switch to MySQL / PostgreSQL

1. Replace the H2 dependency in `pom.xml` with your JDBC driver.
2. Update `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tinyurl
spring.datasource.username=root
spring.datasource.password=secret
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.h2.console.enabled=false
```

---

## 🏗️ Project Structure

```
tinyurl/
├── pom.xml
└── src/main/
    ├── java/com/tinyurl/
    │   ├── TinyUrlApplication.java              # Entry point
    │   ├── config/
    │   │   └── OpenApiConfig.java               # Swagger / OpenAPI 3 metadata
    │   ├── controller/
    │   │   ├── UrlController.java               # REST endpoints
    │   │   └── GlobalExceptionHandler.java      # Error handling
    │   ├── service/
    │   │   └── UrlService.java                  # Business logic & code generation
    │   ├── model/
    │   │   ├── UrlMapping.java                  # JPA @Entity
    │   │   └── UrlDto.java                      # Request / Response DTOs
    │   └── repository/
    │       └── UrlMappingRepository.java        # Spring Data JPA repository
    └── resources/
        └── application.properties
```

---

## ⚙️ Configuration Reference

```properties
# Server
server.port=8080
app.base-url=http://localhost:8080

# Database (H2 default)
spring.datasource.url=jdbc:h2:mem:tinyurldb;DB_CLOSE_DELAY=-1
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Swagger UI
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.try-it-out-enabled=true
springdoc.swagger-ui.filter=true

# DevTools
spring.devtools.restart.enabled=true
spring.devtools.livereload.enabled=true
```

---

## 🐛 Known Issues

- **H2 is in-memory only** — all data is lost on restart. Use MySQL/PostgreSQL for persistence.
- **No authentication** — all endpoints are publicly accessible.
- **No rate limiting** — endpoints are susceptible to abuse without an API gateway.

---

## 🗺️ Roadmap

- `v1.2.0` — API key auth + per-key rate limiting via Spring Security
- `v1.2.0` — MySQL/PostgreSQL profile with Flyway migrations
- `v1.3.0` — Redis caching for high-throughput redirects
- `v1.3.0` — QR code generation per short URL
- `v1.4.0` — Time-series click analytics dashboard
- `v2.0.0` — Reactive rewrite with Spring WebFlux + R2DBC

---

## 📄 License

Released under the [MIT License](https://opensource.org/licenses/MIT).