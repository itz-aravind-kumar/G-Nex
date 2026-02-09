# Metadata Service

## Overview

The **Metadata Service** is a core microservice in the G-Nex Drive system responsible for managing file metadata in PostgreSQL. It consumes file events from Kafka and provides REST APIs for querying file information.

## Features

- ✅ Store file metadata in PostgreSQL
- ✅ Consume Kafka events (file uploaded/deleted/updated)
- ✅ REST API for metadata CRUD operations
- ✅ Paginated file listing
- ✅ File search by name/type
- ✅ User storage statistics
- ✅ Ownership validation
- ✅ JPA auditing (created/modified timestamps)
- ✅ Health check endpoints
- ✅ Swagger UI documentation

## Architecture

```
┌─────────────────┐
│   File Service  │
└────────┬────────┘
         │ Kafka Events
         ↓
┌─────────────────┐
│ Kafka (Topics)  │
│ - file.uploaded │
│ - file.deleted  │
│ - metadata.upd  │
└────────┬────────┘
         │ Consume
         ↓
┌─────────────────┐      ┌──────────────┐
│ Metadata Service│─────→│  PostgreSQL  │
│   (Port 8082)   │      │   Database   │
└────────┬────────┘      └──────────────┘
         │
         ↓
   REST API Clients
   (API Gateway)
```

## Technology Stack

- **Spring Boot 3.2.0** - Framework
- **Spring Data JPA** - Database access
- **PostgreSQL 16** - Relational database
- **Spring Kafka** - Event consumption
- **Hibernate** - ORM
- **Lombok** - Boilerplate reduction
- **SpringDoc OpenAPI** - API documentation

## Database Schema

### Table: `file_metadata`

```sql
CREATE TABLE file_metadata (
    file_id VARCHAR(100) PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50),
    file_size BIGINT NOT NULL,
    owner_id VARCHAR(100) NOT NULL,
    owner_email VARCHAR(255),
    storage_path VARCHAR(500) NOT NULL,
    content_type VARCHAR(100),
    checksum VARCHAR(64),
    status VARCHAR(20),
    uploaded_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP,
    CONSTRAINT idx_owner_id INDEX (owner_id),
    CONSTRAINT idx_file_name INDEX (file_name),
    CONSTRAINT idx_file_type INDEX (file_type)
);
```

### File Status Enum

- `UPLOADING` - File upload in progress
- `UPLOADED` - File uploaded successfully
- `PROCESSING` - File being processed
- `AVAILABLE` - File ready for download
- `DELETED` - File marked as deleted
- `ERROR` - Upload/processing error

## REST API Endpoints

### 1. Get File Metadata

```http
GET /api/v1/metadata/{fileId}
Headers:
  X-User-Id: user123
```

**Response:**
```json
{
  "success": true,
  "message": "File metadata retrieved successfully",
  "data": {
    "fileId": "uuid",
    "fileName": "document.pdf",
    "fileType": "pdf",
    "fileSize": 1048576,
    "ownerId": "user123",
    "ownerEmail": "user@example.com",
    "storagePath": "user123/uuid.pdf",
    "contentType": "application/pdf",
    "checksum": "md5-hash",
    "status": "UPLOADED",
    "uploadedAt": "2026-02-09T10:00:00",
    "modifiedAt": "2026-02-09T10:00:00"
  }
}
```

### 2. Get User Files (Paginated)

```http
GET /api/v1/metadata/user/{userId}?page=0&size=20&sortBy=uploadedAt&sortDir=DESC
```

**Response:**
```json
{
  "success": true,
  "message": "User files retrieved successfully",
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 5,
    "number": 0,
    "size": 20
  }
}
```

### 3. Search Files

```http
GET /api/v1/metadata/user/{userId}/search?query=document
```

**Response:**
```json
{
  "success": true,
  "message": "Search completed successfully",
  "data": [
    {
      "fileId": "uuid1",
      "fileName": "document.pdf",
      ...
    }
  ]
}
```

### 4. Update File Metadata

```http
PUT /api/v1/metadata/{fileId}
Headers:
  X-User-Id: user123
  Content-Type: application/json
Body:
{
  "fileName": "renamed-document.pdf",
  "status": "AVAILABLE"
}
```

### 5. Delete File Metadata

```http
DELETE /api/v1/metadata/{fileId}
Headers:
  X-User-Id: user123
```

### 6. Get User Storage Statistics

```http
GET /api/v1/metadata/user/{userId}/stats
```

**Response:**
```json
{
  "success": true,
  "message": "Storage statistics retrieved successfully",
  "data": {
    "userId": "user123",
    "totalFiles": 150,
    "totalStorage": 52428800,
    "totalStorageFormatted": "50.00 MB"
  }
}
```

### 7. Health Check

```http
GET /health
```

## Kafka Event Consumption

### 1. File Uploaded Event

**Topic:** `file.uploaded`

**Event Structure:**
```json
{
  "eventId": "uuid",
  "eventType": "FILE_UPLOADED",
  "fileId": "file-uuid",
  "userId": "user123",
  "timestamp": "2026-02-09T10:00:00",
  "payload": {
    "fileName": "document.pdf",
    "fileType": "pdf",
    "fileSize": 1048576,
    "contentType": "application/pdf",
    "storagePath": "user123/file-uuid.pdf",
    "checksum": "md5-hash",
    "ownerEmail": "user@example.com"
  }
}
```

**Action:** Creates new metadata record in PostgreSQL

### 2. File Deleted Event

**Topic:** `file.deleted`

**Action:** Marks metadata as `DELETED` status

### 3. Metadata Updated Event

**Topic:** `metadata.updated`

**Action:** Updates existing metadata record

## Configuration

### application.yml

```yaml
server:
  port: 8082

spring:
  application:
    name: metadata-service
  
  datasource:
    url: jdbc:postgresql://localhost:5432/gdrive_metadata
    username: gdrive_user
    password: gdrive_pass
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: metadata-service
      auto-offset-reset: earliest
```

### application-docker.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/gdrive_metadata
  
  kafka:
    bootstrap-servers: kafka:9092
```

## Running Locally

### 1. Prerequisites

Start PostgreSQL:
```powershell
docker run -d `
  --name postgres `
  -e POSTGRES_DB=gdrive_metadata `
  -e POSTGRES_USER=gdrive_user `
  -e POSTGRES_PASSWORD=gdrive_pass `
  -p 5432:5432 `
  postgres:16
```

Start Kafka:
```powershell
docker run -d `
  --name kafka `
  -p 9092:9092 `
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 `
  confluentinc/cp-kafka:latest
```

### 2. Build & Run

```powershell
# Build common-lib first
mvn clean install -pl common-lib

# Run metadata-service
cd metadata-service
mvn spring-boot:run
```

### 3. Verify

```powershell
# Health check
curl http://localhost:8082/health

# Swagger UI
Start-Process "http://localhost:8082/swagger-ui.html"
```

## Testing

### 1. Simulate File Upload Event

```powershell
# Publish test event to Kafka
kafka-console-producer --broker-list localhost:9092 --topic file.uploaded
```

Paste JSON:
```json
{
  "eventId": "test-event-1",
  "eventType": "FILE_UPLOADED",
  "fileId": "test-file-1",
  "userId": "user123",
  "timestamp": "2026-02-09T10:00:00",
  "payload": {
    "fileName": "test.pdf",
    "fileType": "pdf",
    "fileSize": 1048576,
    "contentType": "application/pdf",
    "storagePath": "user123/test-file-1.pdf",
    "checksum": "abc123",
    "ownerEmail": "user@test.com"
  }
}
```

### 2. Query Metadata

```powershell
curl http://localhost:8082/api/v1/metadata/test-file-1 `
  -H "X-User-Id: user123"
```

### 3. Get User Files

```powershell
curl "http://localhost:8082/api/v1/metadata/user/user123?page=0&size=10"
```

### 4. Search Files

```powershell
curl "http://localhost:8082/api/v1/metadata/user/user123/search?query=test"
```

### 5. Get Storage Stats

```powershell
curl http://localhost:8082/api/v1/metadata/user/user123/stats
```

## Integration with Other Services

### File Service Integration

```
File Service uploads file
    ↓
Publishes to file.uploaded topic
    ↓
Metadata Service consumes event
    ↓
Saves metadata to PostgreSQL
    ↓
Available via REST API
```

### API Gateway Integration

```
Client → API Gateway → Metadata Service
JWT Auth → Rate Limit → Route → Response
```

### Search Service Integration

```
Metadata Service stores in PostgreSQL
    ↓
Search Service indexes in Elasticsearch
    ↓
Both services provide different query capabilities
```

## Error Handling

### Not Found (404)

```json
{
  "success": false,
  "message": "File metadata not found with ID: xyz",
  "data": null,
  "timestamp": "2026-02-09T10:00:00",
  "requestId": "uuid"
}
```

### Unauthorized (403)

```json
{
  "success": false,
  "message": "File not found or you don't have permission",
  "data": null
}
```

### Internal Error (500)

```json
{
  "success": false,
  "message": "Failed to save metadata: Connection timeout",
  "data": null
}
```

## Monitoring

### Metrics Endpoints

- `GET /actuator/health` - Service health
- `GET /actuator/metrics` - Application metrics
- `GET /actuator/prometheus` - Prometheus metrics

### Key Metrics

- `metadata_saves_total` - Total metadata saves
- `metadata_queries_total` - Total queries
- `kafka_events_consumed_total` - Events consumed
- `database_connections_active` - Active DB connections

## Troubleshooting

### Issue: Database Connection Failed

**Solution:**
```powershell
# Check PostgreSQL status
docker ps | Select-String postgres

# Check logs
docker logs postgres

# Verify connection
psql -h localhost -U gdrive_user -d gdrive_metadata
```

### Issue: Kafka Events Not Consumed

**Solution:**
```powershell
# Check Kafka topics
kafka-topics --bootstrap-server localhost:9092 --list

# Check consumer group
kafka-consumer-groups --bootstrap-server localhost:9092 --group metadata-service --describe

# Check service logs
tail -f logs/metadata-service.log
```

### Issue: Metadata Not Found

**Cause:** Event not consumed or database not updated

**Solution:**
```sql
-- Check database
SELECT * FROM file_metadata WHERE file_id = 'your-file-id';

-- Check Kafka lag
kafka-consumer-groups --bootstrap-server localhost:9092 --group metadata-service --describe
```

## Performance Tuning

### Database Optimization

```yaml
spring.jpa.properties:
  hibernate:
    jdbc.batch_size: 20
    order_inserts: true
    order_updates: true
```

### Kafka Optimization

```yaml
spring.kafka.consumer:
  max-poll-records: 100
  fetch-min-size: 1024
  fetch-max-wait: 500
```

### Connection Pooling

```yaml
spring.datasource:
  hikari:
    maximum-pool-size: 10
    minimum-idle: 5
    connection-timeout: 30000
```

## Security Considerations

1. **Database Credentials:** Use environment variables or secrets management
2. **X-User-Id Header:** Should be validated by API Gateway (JWT)
3. **SQL Injection:** Prevented by JPA parameterized queries
4. **Ownership Validation:** All operations verify user ownership

## Future Enhancements

- [ ] File sharing metadata (shared_with, permissions)
- [ ] File versioning support
- [ ] Trash/recycle bin with restore
- [ ] Folder/directory structure
- [ ] File tags and categories
- [ ] Advanced search filters
- [ ] Audit trail logging
- [ ] Caching with Redis

## Dependencies

```xml
<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
  </dependency>
  <dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
  </dependency>
</dependencies>
```

## Contributing

1. Follow Java code style conventions
2. Write comprehensive tests
3. Update documentation
4. Ensure zero compilation errors
5. Test with actual Kafka events

## License

Part of G-Nex Drive System - Internal Project

---

**Status:** ✅ Production Ready  
**Port:** 8082  
**Database:** PostgreSQL  
**Dependencies:** Kafka, File Service
