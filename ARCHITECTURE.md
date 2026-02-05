# Mini Google Drive - Architecture Documentation

## System Overview

This is a microservices-based file storage platform demonstrating production-ready system design patterns for technical interviews.

## Architecture Diagram

```
┌──────────┐
│  Client  │
└────┬─────┘
     │
     ▼
┌────────────────┐
│  API Gateway   │ (Authentication, Rate Limiting, Routing)
│   Port: 8080   │
└────┬───────────┘
     │
     ├─────────────┬─────────────┬─────────────┬─────────────┐
     ▼             ▼             ▼             ▼             ▼
┌─────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
│  File   │  │ Metadata │  │  Search  │  │ Activity │  │  ...     │
│ Service │  │ Service  │  │ Service  │  │ Service  │  │          │
│  8081   │  │   8082   │  │   8083   │  │   8084   │  │          │
└────┬────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘  └──────────┘
     │            │              │              │
     │            │              │              │
     └────────────┴──────────────┴──────────────┘
                   │
                   ▼
            ┌──────────┐
            │  Kafka   │ (Event Bus)
            └──────────┘
                   │
     ┌─────────────┼─────────────┬─────────────┐
     ▼             ▼             ▼             ▼
┌─────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
│ MinIO/  │  │PostgreSQL│  │Elastic   │  │  Redis   │
│   S3    │  │          │  │ search   │  │ (Cache)  │
└─────────┘  └──────────┘  └──────────┘  └──────────┘
```

## Microservices

### 1. API Gateway (Port 8080)
**Purpose**: Single entry point for all client requests

**Responsibilities**:
- Route requests to appropriate microservices
- JWT authentication and authorization
- Rate limiting (Redis-based)
- Request/response logging
- CORS handling
- Circuit breaker pattern

**Technology Stack**:
- Spring Cloud Gateway
- Spring Security
- JWT (jsonwebtoken)
- Redis (rate limiting)
- Resilience4j (circuit breaker)

**Key Classes**:
- `ApiGatewayApplication.java` - Main application class
- `SecurityConfig.java` - Security configuration
- `JwtAuthenticationFilter.java` - JWT validation filter
- `RateLimitFilter.java` - Rate limiting configuration
- `JwtUtil.java` - JWT utility methods

---

### 2. File Service (Port 8081)
**Purpose**: Handle file upload/download operations

**Responsibilities**:
- Upload files to object storage (MinIO/S3)
- Download files from object storage
- Delete files from storage
- Generate pre-signed URLs
- Validate file size, type, and content
- Publish file events to Kafka

**Technology Stack**:
- Spring Boot Web
- MinIO SDK (object storage)
- AWS S3 SDK (alternative)
- Apache Kafka (event publishing)

**Key Classes**:
- `FileServiceApplication.java` - Main application class
- `FileController.java` - REST API endpoints
- `FileService.java` - Business logic interface
- `FileServiceImpl.java` - Business logic implementation
- `ObjectStorageService.java` - Storage operations interface
- `MinioStorageService.java` - MinIO implementation
- `KafkaProducerService.java` - Event publishing

**API Endpoints**:
- `POST /api/v1/files/upload` - Upload file
- `GET /api/v1/files/{fileId}/download` - Download file
- `DELETE /api/v1/files/{fileId}` - Delete file
- `GET /api/v1/files/{fileId}` - Get file info

**Kafka Topics Published**:
- `file.uploaded` - File upload events
- `file.deleted` - File deletion events
- `file.downloaded` - File download events

---

### 3. Metadata Service (Port 8082)
**Purpose**: Manage file metadata in relational database

**Responsibilities**:
- Store and retrieve file metadata
- Track file ownership
- Provide file search by metadata
- Calculate user storage statistics
- Consume file events from Kafka
- Update metadata on file operations

**Technology Stack**:
- Spring Boot Web
- Spring Data JPA
- PostgreSQL
- Apache Kafka (event consumption)

**Key Classes**:
- `MetadataServiceApplication.java` - Main application class
- `MetadataController.java` - REST API endpoints
- `MetadataService.java` - Business logic interface
- `MetadataServiceImpl.java` - Business logic implementation
- `FileMetadata.java` - JPA entity
- `FileMetadataRepository.java` - Data access layer
- `FileEventConsumer.java` - Kafka consumer
- `FileMetadataMapper.java` - DTO/Entity mapper

**API Endpoints**:
- `GET /api/v1/metadata/{fileId}` - Get file metadata
- `GET /api/v1/metadata/user/{userId}` - Get user's files
- `PUT /api/v1/metadata/{fileId}` - Update metadata
- `GET /api/v1/metadata/user/{userId}/stats` - Get storage stats

**Database Schema**:
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
    INDEX idx_owner_id (owner_id),
    INDEX idx_file_name (file_name),
    INDEX idx_file_type (file_type)
);
```

---

### 4. Search Service (Port 8083)
**Purpose**: Provide fast file search capabilities

**Responsibilities**:
- Index file metadata in Elasticsearch
- Perform full-text search on file names
- Filter by file type, size, date
- Provide search suggestions/autocomplete
- Cache search results in Redis
- Consume file events for indexing

**Technology Stack**:
- Spring Boot Web
- Elasticsearch (search engine)
- Redis (result caching)
- Apache Kafka (event consumption)

**Key Classes**:
- `SearchServiceApplication.java` - Main application class
- `SearchController.java` - REST API endpoints
- `SearchService.java` - Business logic interface
- `SearchServiceImpl.java` - Business logic implementation
- `FileDocument.java` - Elasticsearch document
- `FileIndexConsumer.java` - Kafka consumer
- `ElasticsearchConfig.java` - Elasticsearch configuration
- `RedisConfig.java` - Cache configuration

**API Endpoints**:
- `GET /api/v1/search?query={q}&type={t}` - Simple search
- `POST /api/v1/search/advanced` - Advanced search with filters
- `GET /api/v1/search/suggestions?query={q}` - Get suggestions

**Elasticsearch Index Schema**:
```json
{
  "mappings": {
    "properties": {
      "fileId": { "type": "keyword" },
      "fileName": { "type": "text", "analyzer": "standard" },
      "fileType": { "type": "keyword" },
      "fileSize": { "type": "long" },
      "ownerId": { "type": "keyword" },
      "ownerEmail": { "type": "keyword" },
      "contentType": { "type": "keyword" },
      "uploadedAt": { "type": "date" },
      "modifiedAt": { "type": "date" },
      "tags": { "type": "keyword" },
      "status": { "type": "keyword" }
    }
  }
}
```

**Cache Strategy**:
- Search results cached for 5 minutes
- Suggestions cached for 10 minutes
- Cache invalidation on file updates

---

### 5. Activity Service (Port 8084)
**Purpose**: Track and log all file-related activities

**Responsibilities**:
- Log file upload/download/delete activities
- Track user behavior and patterns
- Provide activity history and audit logs
- Generate activity reports and statistics
- Consume all file events asynchronously

**Technology Stack**:
- Spring Boot Web
- Spring Data JPA
- PostgreSQL
- Apache Kafka (event consumption)

**Key Classes**:
- `ActivityServiceApplication.java` - Main application class
- `ActivityController.java` - REST API endpoints
- `ActivityService.java` - Business logic interface
- `ActivityServiceImpl.java` - Business logic implementation
- `FileActivity.java` - JPA entity
- `FileActivityRepository.java` - Data access layer
- `ActivityEventConsumer.java` - Kafka consumer

**API Endpoints**:
- `GET /api/v1/activities/user/{userId}` - Get user activities
- `GET /api/v1/activities/file/{fileId}` - Get file activities
- `GET /api/v1/activities/user/{userId}/recent` - Recent activities
- `GET /api/v1/activities/user/{userId}/stats` - Activity statistics
- `GET /api/v1/activities/user/{userId}/range` - Activities by date range

**Database Schema**:
```sql
CREATE TABLE file_activities (
    activity_id UUID PRIMARY KEY,
    file_id VARCHAR(100) NOT NULL,
    file_name VARCHAR(255),
    user_id VARCHAR(100) NOT NULL,
    user_email VARCHAR(255),
    activity_type VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    timestamp TIMESTAMP NOT NULL,
    metadata TEXT,
    INDEX idx_user_id (user_id),
    INDEX idx_file_id (file_id),
    INDEX idx_activity_type (activity_type),
    INDEX idx_timestamp (timestamp)
);
```

---

## Common Library

Shared across all microservices to ensure consistency.

**Components**:
- `ApiResponse<T>` - Standard API response wrapper
- `FileMetadataDto` - File metadata data transfer object
- `FileActivityDto` - Activity data transfer object
- `FileEvent` - Kafka event structure
- `ResourceNotFoundException` - Custom exception
- `FileStorageException` - Custom exception
- `GlobalExceptionHandler` - Centralized exception handling
- `FileUtils` - Utility methods for file operations
- `AppConstants` - Application-wide constants

---

## Infrastructure Services

### Apache Kafka
**Purpose**: Event-driven communication between services

**Topics**:
- `file.uploaded` - File upload notifications
- `file.deleted` - File deletion notifications
- `file.downloaded` - File download notifications
- `metadata.updated` - Metadata change notifications
- `activity.log` - General activity logging

**Configuration**:
- Single broker for development
- Replication factor: 1 (increase in production)
- Auto-create topics enabled
- JSON serialization for events

---

### PostgreSQL
**Purpose**: Persistent storage for metadata and activities

**Databases**:
- `gdrive_metadata` - Shared database for metadata and activities

**Tables**:
- `file_metadata` - File metadata information
- `file_activities` - User activity logs

**Connection Pooling**:
- HikariCP (default Spring Boot)
- Max pool size: 10

---

### Elasticsearch
**Purpose**: Fast full-text search and filtering

**Indices**:
- `files` - File metadata for search

**Configuration**:
- Single-node cluster for development
- No security (xpack.security.enabled=false)
- Heap size: 512MB

---

### Redis
**Purpose**: Caching and rate limiting

**Use Cases**:
1. **Caching**:
   - Search results (TTL: 5 minutes)
   - File metadata (TTL: 10 minutes)
   - Search suggestions (TTL: 10 minutes)

2. **Rate Limiting**:
   - User-based limits
   - IP-based limits
   - Sliding window algorithm

---

### MinIO (Object Storage)
**Purpose**: Store actual file binaries

**Configuration**:
- Bucket: `gdrive-files`
- Access mode: Private
- Pre-signed URL expiry: 1 hour

**Features**:
- S3-compatible API
- Multi-part upload support
- Erasure coding for data protection

---

## Event Flow

### File Upload Flow
```
1. Client uploads file to API Gateway
2. API Gateway authenticates request → routes to File Service
3. File Service validates file → uploads to MinIO
4. File Service publishes "file.uploaded" event to Kafka
5. Metadata Service consumes event → saves metadata to PostgreSQL
6. Search Service consumes event → indexes file in Elasticsearch
7. Activity Service consumes event → logs activity to PostgreSQL
8. Client receives success response with file metadata
```

### File Search Flow
```
1. Client sends search query to API Gateway
2. API Gateway routes to Search Service
3. Search Service checks Redis cache
   - Cache hit: Return cached results
   - Cache miss:
     a. Query Elasticsearch
     b. Cache results in Redis (TTL: 5 min)
     c. Return results to client
```

### File Download Flow
```
1. Client requests file download from API Gateway
2. API Gateway routes to File Service
3. File Service validates access permissions
4. File Service retrieves file from MinIO
5. File Service publishes "file.downloaded" event to Kafka
6. Activity Service logs download activity
7. File Service streams file to client
```

---

## Deployment

### Docker Compose (Local Development)
```bash
docker-compose up -d
```

All services run in isolated containers with shared network.

### Kubernetes (Production)
```bash
kubectl apply -f k8s/
```

Features:
- Horizontal Pod Autoscaling
- Health checks (liveness & readiness probes)
- Resource limits
- Rolling updates
- Service discovery
- Load balancing

---

## Scalability Considerations

### Horizontal Scaling
- All microservices are stateless
- Can scale independently based on load
- Kubernetes handles load balancing

### Database Scaling
- Read replicas for PostgreSQL
- Sharding for high-volume data
- Connection pooling

### Cache Strategy
- Redis cluster for high availability
- Cache invalidation on updates
- TTL-based expiration

### Object Storage
- MinIO distributed mode
- Automatic data replication
- Erasure coding

---

## Security

### Authentication
- JWT-based stateless authentication
- Token expiry: 1 hour
- Refresh token mechanism

### Authorization
- Role-based access control (RBAC)
- File ownership validation
- Admin privileges

### Data Protection
- Files encrypted at rest (MinIO)
- HTTPS/TLS in transit
- Secure password hashing (BCrypt)

### Rate Limiting
- Per-user limits: 100 req/min
- Per-IP limits: 200 req/min
- Burst handling with token bucket

---

## Monitoring & Observability

### Health Checks
- Spring Boot Actuator endpoints
- `/actuator/health` - Service health
- `/actuator/metrics` - Application metrics

### Logging
- Centralized logging (future: ELK stack)
- Structured JSON logs
- Correlation IDs for request tracing

### Metrics
- Prometheus-compatible metrics
- JVM metrics, HTTP metrics
- Custom business metrics

---

## Interview Talking Points

This project demonstrates:

1. **Microservices Architecture** - Independent, scalable services
2. **Event-Driven Design** - Asynchronous processing via Kafka
3. **CQRS Pattern** - Separate read (Search) and write (Metadata) models
4. **API Gateway Pattern** - Single entry point, security, routing
5. **Cache-Aside Pattern** - Redis for performance optimization
6. **Repository Pattern** - Data access abstraction
7. **DTO Pattern** - Data transfer between layers
8. **Circuit Breaker** - Fault tolerance
9. **Database Per Service** - Data isolation
10. **Container Orchestration** - Kubernetes deployment

### System Design Questions Covered:
- ✅ Design Google Drive
- ✅ Design File Upload System
- ✅ Design Event-Driven Architecture
- ✅ Design Scalable Backend
- ✅ Design Distributed System

---

## Future Enhancements

- [ ] Add Spring Cloud Config for centralized configuration
- [ ] Implement distributed tracing (Zipkin/Jaeger)
- [ ] Add API versioning
- [ ] Implement file sharing and permissions
- [ ] Add file versioning support
- [ ] Implement thumbnail generation
- [ ] Add full-text content search (OCR)
- [ ] Implement webhook notifications
- [ ] Add GraphQL API
- [ ] Implement multi-tenancy
