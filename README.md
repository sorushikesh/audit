# Audit Service

This project provides a robust **Audit Logging Microservice** built with **Spring Boot**. It enables persistent tracking of changes across your application using **JaVers** and **MongoDB**, with **Kafka** for asynchronous, event-driven audit ingestion.

## 🧩 Features

- ✅ **Audit Event Ingestion**  
  - `AuditEventProducer` publishes events to Kafka.  
  - `AuditKafkaListener` listens and converts these into **JaVers commits**.

- 🌐 **REST API**  
  Exposes endpoints via `AuditController` to:
  - Log audit events.
  - Fetch audit logs by entity or user.
  - Retrieve full entity history.

- ⏳ **TTL (Time-to-Live) Management**  
  - Mongock migrations add a `TTL_DATE` field to support expiry.  
  - `TtlCleanupScheduler` regularly purges expired audit records.

- 🐳 **Dockerized Kafka**  
  A self-contained development setup using `docker-compose.yaml`.

- 🧪 **Comprehensive Test Suite**  
  - Integration with **JUnit 5**.  
  - Uses **Testcontainers** for Kafka and MongoDB tests.

---

## 🔧 Requirements

- Java 21  
- Maven 3.9+  
- Docker (for Kafka and Testcontainers)  
- Local MongoDB instance (`localhost:27017`)

---

## 🚀 Getting Started

### 📦 Build the Project

```bash
./mvnw clean package
```

### 🟢 Run Locally

1. **Start Kafka using Docker Compose**:

```bash
docker-compose up -d
```

2. **Ensure MongoDB is Running**

Update `spring.data.mongodb.uri` in `application-dev.yml` if needed.

3. **Start the Application**

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The app runs at: [http://localhost:8080](http://localhost:8080)

---

## 📡 API Usage

**Base URL**: `/api/auditService`

| Method | Endpoint                                      | Description                         |
|--------|-----------------------------------------------|-------------------------------------|
| POST   | `/log`                                        | Log a new audit event               |
| GET    | `/audit`                                      | Get all audit records               |
| GET    | `/audit/{entityType}/{entityId}`              | Get records by entity               |
| GET    | `/audit/user/{userId}`                        | Get records by user ID              |
| GET    | `/audit/history/{entityType}/{entityId}`      | Get entity change history           |

### 📘 Example Request

```bash
curl -X POST http://localhost:8080/api/auditService/log \
  -H 'Content-Type: application/json' \
  -d '{
    "id": "1",
    "entityType": "invoice",
    "entityId": "42",
    "changedDate": "2024-01-01T00:00:00",
    "author": "system",
    "authorEmail": "system@example.com",
    "operation": "Create",
    "newVal": {
      "status": "PAID"
    }
  }'
```

---

## 🧪 Testing

Run unit and integration tests using:

```bash
./mvnw test
```

Tests using Testcontainers require Docker to be running.

---

## 👨‍💻 About the Developer

**Rushikesh Sonawane**
- Java Backend Developer
- 📧 Email: [sorushikesh07@gmail.com](mailto:sorushikesh07@gmail.com)
- 🔗 LinkedIn: [Rushikesh Sonawane](https://www.linkedin.com/in/rushikeshsonawane2104/)
