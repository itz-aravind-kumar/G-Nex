# Project Structure

```
mini-google-drive/
│
├── README.md                      # Main project overview
├── ARCHITECTURE.md                # Detailed architecture documentation
├── QUICKSTART.md                  # Quick start guide
├── TODO.md                        # Implementation checklist
├── .gitignore                     # Git ignore rules
├── pom.xml                        # Root Maven POM (parent)
├── docker-compose.yml             # Docker Compose configuration
│
├── common-lib/                    # Shared library module
│   ├── pom.xml
│   └── src/main/java/com/gnexdrive/common/
│       ├── dto/
│       │   ├── ApiResponse.java              # Standard API response wrapper
│       │   ├── FileMetadataDto.java          # File metadata DTO
│       │   └── FileActivityDto.java          # Activity DTO
│       ├── event/
│       │   └── FileEvent.java                # Kafka event structure
│       ├── exception/
│       │   ├── ResourceNotFoundException.java
│       │   ├── FileStorageException.java
│       │   └── GlobalExceptionHandler.java   # Global exception handler
│       ├── util/
│       │   └── FileUtils.java                # File utility methods
│       └── constant/
│           └── AppConstants.java             # Application constants
│
├── api-gateway/                   # API Gateway Service (Port 8080)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/com/gnexdrive/gateway/
│       │   ├── ApiGatewayApplication.java    # Main application class
│       │   ├── config/
│       │   │   └── SecurityConfig.java       # Security configuration
│       │   ├── filter/
│       │   │   ├── JwtAuthenticationFilter.java  # JWT validation filter
│       │   │   └── RateLimitFilter.java         # Rate limiting filter
│       │   └── util/
│       │       └── JwtUtil.java              # JWT utility methods
│       └── resources/
│           ├── application.yml               # Default configuration
│           └── application-docker.yml        # Docker configuration
│
├── file-service/                  # File Service (Port 8081)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/com/gnexdrive/fileservice/
│       │   ├── FileServiceApplication.java   # Main application class
│       │   ├── controller/
│       │   │   └── FileController.java       # REST endpoints
│       │   ├── service/
│       │   │   ├── FileService.java          # Service interface
│       │   │   ├── ObjectStorageService.java # Storage interface
│       │   │   ├── KafkaProducerService.java # Kafka interface
│       │   │   └── impl/
│       │   │       ├── FileServiceImpl.java
│       │   │       ├── MinioStorageService.java
│       │   │       └── KafkaProducerServiceImpl.java
│       │   └── config/
│       │       ├── MinioConfig.java          # MinIO configuration
│       │       └── KafkaProducerConfig.java  # Kafka configuration
│       └── resources/
│           ├── application.yml
│           └── application-docker.yml
│
├── metadata-service/              # Metadata Service (Port 8082)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/com/gnexdrive/metadataservice/
│       │   ├── MetadataServiceApplication.java
│       │   ├── controller/
│       │   │   └── MetadataController.java   # REST endpoints
│       │   ├── entity/
│       │   │   └── FileMetadata.java         # JPA entity
│       │   ├── repository/
│       │   │   └── FileMetadataRepository.java  # JPA repository
│       │   ├── service/
│       │   │   ├── MetadataService.java      # Service interface
│       │   │   └── impl/
│       │   │       └── MetadataServiceImpl.java
│       │   ├── kafka/
│       │   │   └── FileEventConsumer.java    # Kafka consumer
│       │   └── mapper/
│       │       └── FileMetadataMapper.java   # DTO/Entity mapper
│       └── resources/
│           ├── application.yml
│           └── application-docker.yml
│
├── search-service/                # Search Service (Port 8083)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/com/gnexdrive/searchservice/
│       │   ├── SearchServiceApplication.java
│       │   ├── controller/
│       │   │   └── SearchController.java     # REST endpoints
│       │   ├── document/
│       │   │   └── FileDocument.java         # Elasticsearch document
│       │   ├── dto/
│       │   │   ├── SearchRequest.java        # Search request DTO
│       │   │   └── SearchResponse.java       # Search response DTO
│       │   ├── service/
│       │   │   ├── SearchService.java        # Service interface
│       │   │   └── impl/
│       │   │       └── SearchServiceImpl.java
│       │   ├── kafka/
│       │   │   └── FileIndexConsumer.java    # Kafka consumer
│       │   └── config/
│       │       ├── ElasticsearchConfig.java  # Elasticsearch config
│       │       └── RedisConfig.java          # Redis cache config
│       └── resources/
│           ├── application.yml
│           └── application-docker.yml
│
├── activity-service/              # Activity Service (Port 8084)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/com/gnexdrive/activityservice/
│       │   ├── ActivityServiceApplication.java
│       │   ├── controller/
│       │   │   └── ActivityController.java   # REST endpoints
│       │   ├── entity/
│       │   │   └── FileActivity.java         # JPA entity
│       │   ├── repository/
│       │   │   └── FileActivityRepository.java  # JPA repository
│       │   ├── service/
│       │   │   ├── ActivityService.java      # Service interface
│       │   │   └── impl/
│       │   │       └── ActivityServiceImpl.java
│       │   └── kafka/
│       │       └── ActivityEventConsumer.java  # Kafka consumer
│       └── resources/
│           ├── application.yml
│           └── application-docker.yml
│
└── k8s/                           # Kubernetes Deployment Files
    ├── README.md                  # K8s deployment guide
    ├── namespace.yaml             # Namespace definition
    ├── postgres.yaml              # PostgreSQL deployment
    ├── redis.yaml                 # Redis deployment
    ├── elasticsearch.yaml         # Elasticsearch deployment
    ├── kafka.yaml                 # Kafka & Zookeeper deployment
    ├── minio.yaml                 # MinIO deployment
    ├── api-gateway.yaml           # API Gateway deployment
    ├── file-service.yaml          # File Service deployment
    ├── metadata-service.yaml      # Metadata Service deployment
    ├── search-service.yaml        # Search Service deployment
    └── activity-service.yaml      # Activity Service deployment
```

## Module Summary

### Common Library (common-lib)
**Purpose**: Shared utilities, DTOs, exceptions, and constants used across all microservices  
**Dependencies**: Spring Boot, Kafka, Jackson, Lombok  
**Key Components**:
- Standard API response wrapper
- File metadata and activity DTOs
- Kafka event structures
- Custom exceptions
- Global exception handler
- File utility methods
- Application constants

---

### API Gateway (api-gateway) - Port 8080
**Purpose**: Single entry point for all client requests  
**Dependencies**: Spring Cloud Gateway, Spring Security, JWT, Redis, Resilience4j  
**Key Features**:
- Request routing to microservices
- JWT authentication
- Rate limiting (Redis-based)
- Circuit breaker pattern
- CORS handling

**Routes**:
- `/api/v1/files/**` → File Service
- `/api/v1/metadata/**` → Metadata Service
- `/api/v1/search/**` → Search Service
- `/api/v1/activities/**` → Activity Service

---

### File Service (file-service) - Port 8081
**Purpose**: Handle file upload, download, and deletion  
**Dependencies**: Spring Web, MinIO SDK, AWS S3 SDK, Kafka  
**Key Features**:
- File upload to object storage (MinIO/S3)
- File download with streaming
- File deletion
- File validation (size, type)
- Pre-signed URL generation
- Kafka event publishing

**Endpoints**:
- `POST /api/v1/files/upload` - Upload file
- `GET /api/v1/files/{id}/download` - Download file
- `DELETE /api/v1/files/{id}` - Delete file
- `GET /api/v1/files/{id}` - Get file info

**Events Published**:
- `file.uploaded`
- `file.deleted`
- `file.downloaded`

---

### Metadata Service (metadata-service) - Port 8082
**Purpose**: Manage file metadata in PostgreSQL  
**Dependencies**: Spring Web, Spring Data JPA, PostgreSQL, Kafka  
**Key Features**:
- CRUD operations on file metadata
- User file listing with pagination
- Storage statistics calculation
- Kafka event consumption
- Metadata search

**Endpoints**:
- `GET /api/v1/metadata/{id}` - Get metadata
- `GET /api/v1/metadata/user/{userId}` - List user files
- `PUT /api/v1/metadata/{id}` - Update metadata
- `GET /api/v1/metadata/user/{userId}/stats` - Storage stats

**Events Consumed**:
- `file.uploaded` → Save metadata
- `file.deleted` → Update status
- `metadata.updated` → Update fields

**Database**: PostgreSQL (gdrive_metadata)  
**Tables**: file_metadata

---

### Search Service (search-service) - Port 8083
**Purpose**: Fast file search using Elasticsearch  
**Dependencies**: Spring Web, Elasticsearch, Redis, Kafka  
**Key Features**:
- Full-text search on file names
- Advanced filtering (type, size, date)
- Search suggestions/autocomplete
- Redis result caching
- Elasticsearch indexing

**Endpoints**:
- `GET /api/v1/search?query={q}` - Simple search
- `POST /api/v1/search/advanced` - Advanced search
- `GET /api/v1/search/suggestions?query={q}` - Suggestions

**Events Consumed**:
- `file.uploaded` → Index file
- `file.deleted` → Remove from index
- `metadata.updated` → Update index

**Cache Strategy**:
- Search results: 5 minutes TTL
- Suggestions: 10 minutes TTL

---

### Activity Service (activity-service) - Port 8084
**Purpose**: Track and log all file-related activities  
**Dependencies**: Spring Web, Spring Data JPA, PostgreSQL, Kafka  
**Key Features**:
- Activity logging
- Activity history retrieval
- Activity statistics
- Audit trail
- User behavior tracking

**Endpoints**:
- `GET /api/v1/activities/user/{userId}` - User activities
- `GET /api/v1/activities/file/{fileId}` - File activities
- `GET /api/v1/activities/user/{userId}/recent` - Recent activities
- `GET /api/v1/activities/user/{userId}/stats` - Statistics

**Events Consumed**:
- `file.uploaded` → Log upload activity
- `file.deleted` → Log delete activity
- `file.downloaded` → Log download activity
- `activity.log` → Log general activity

**Database**: PostgreSQL (gdrive_metadata)  
**Tables**: file_activities

---

## Infrastructure Services

### PostgreSQL
- **Database**: gdrive_metadata
- **Tables**: file_metadata, file_activities
- **Port**: 5432

### Redis
- **Purpose**: Caching and rate limiting
- **Port**: 6379

### Elasticsearch
- **Purpose**: Full-text search
- **Indices**: files
- **Ports**: 9200 (HTTP), 9300 (Transport)

### Apache Kafka
- **Purpose**: Event streaming
- **Topics**: file.uploaded, file.deleted, file.downloaded, metadata.updated, activity.log
- **Port**: 9092

### Zookeeper
- **Purpose**: Kafka coordination
- **Port**: 2181

### MinIO
- **Purpose**: Object storage
- **Bucket**: gdrive-files
- **Ports**: 9000 (API), 9001 (Console)

---

## Technology Stack Summary

| Component | Technology |
|-----------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.0 |
| Cloud | Spring Cloud 2023.0.0 |
| Build Tool | Maven 3.8+ |
| API Gateway | Spring Cloud Gateway |
| Security | Spring Security + JWT |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA |
| Cache | Redis 7 |
| Search | Elasticsearch 8.11 |
| Message Queue | Apache Kafka 3.6 |
| Object Storage | MinIO (S3-compatible) |
| Documentation | SpringDoc OpenAPI 3 |
| Monitoring | Spring Boot Actuator |
| Containerization | Docker |
| Orchestration | Kubernetes |

---

## Port Summary

| Service | Port | Type |
|---------|------|------|
| API Gateway | 8080 | External |
| File Service | 8081 | Internal |
| Metadata Service | 8082 | Internal |
| Search Service | 8083 | Internal |
| Activity Service | 8084 | Internal |
| PostgreSQL | 5432 | Internal |
| Redis | 6379 | Internal |
| Elasticsearch | 9200, 9300 | Internal |
| Kafka | 9092 | Internal |
| Zookeeper | 2181 | Internal |
| MinIO | 9000, 9001 | Internal |

---

## Kafka Topics

| Topic | Producer | Consumers |
|-------|----------|-----------|
| file.uploaded | File Service | Metadata, Search, Activity |
| file.deleted | File Service | Metadata, Search, Activity |
| file.downloaded | File Service | Activity |
| metadata.updated | Metadata Service | Search |
| activity.log | All Services | Activity |

---

## Database Schema

### file_metadata Table
```sql
file_id (PK) VARCHAR(100)
file_name VARCHAR(255)
file_type VARCHAR(50)
file_size BIGINT
owner_id VARCHAR(100) [INDEXED]
owner_email VARCHAR(255)
storage_path VARCHAR(500)
content_type VARCHAR(100)
checksum VARCHAR(64)
status VARCHAR(20)
uploaded_at TIMESTAMP
modified_at TIMESTAMP
```

### file_activities Table
```sql
activity_id (PK) UUID
file_id VARCHAR(100) [INDEXED]
file_name VARCHAR(255)
user_id VARCHAR(100) [INDEXED]
user_email VARCHAR(255)
activity_type VARCHAR(50) [INDEXED]
ip_address VARCHAR(45)
user_agent VARCHAR(500)
timestamp TIMESTAMP [INDEXED]
metadata TEXT
```

---

## Build Commands

```bash
# Build all modules
mvn clean install

# Build specific module
mvn clean install -pl file-service -am

# Skip tests
mvn clean install -DskipTests

# Build Docker images
docker-compose build

# Run all services
docker-compose up

# Deploy to Kubernetes
kubectl apply -f k8s/
```

---

## Key Files

- **README.md** - Project overview and getting started
- **ARCHITECTURE.md** - Detailed system architecture documentation
- **QUICKSTART.md** - Quick start guide with commands
- **TODO.md** - Implementation checklist
- **pom.xml** - Root Maven configuration
- **docker-compose.yml** - Local development environment
- **k8s/** - Kubernetes deployment manifests

---

**Total Lines of Code**: ~5,000 (skeleton only)  
**Total Files**: ~80  
**Microservices**: 5  
**Infrastructure Services**: 6  
**REST Endpoints**: ~20  
**Kafka Topics**: 5  
**Database Tables**: 2

This is a production-ready skeleton structure for a Mini Google Drive system, designed specifically for system design interviews and demonstrating enterprise-level Spring Boot microservices architecture.
