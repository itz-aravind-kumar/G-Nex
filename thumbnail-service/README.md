# Thumbnail Service

Asynchronous thumbnail generation service for the G Nex Drive platform.

## Overview

The Thumbnail Service generates and serves thumbnails for uploaded files asynchronously. It consumes file events from Kafka, generates thumbnails in multiple sizes, stores them in object storage, and provides APIs for retrieving thumbnails.

## Features

- **Asynchronous Generation**: Thumbnails are generated in the background without blocking file uploads
- **Multiple Sizes**: Generates small (150x150), grid (200x200), and preview (400x400) thumbnails
- **Format Optimization**: Prefers WebP format for smaller file sizes and faster loading
- **Content Type Support**:
  - Images: JPEG, PNG, WebP, GIF (using Thumbnailator)
  - PDFs: First page preview (using Apache PDFBox)
  - Videos: Frame extraction (using FFmpeg)
- **Caching**: Redis caching for fast thumbnail URL retrieval
- **Presigned URLs**: Secure, time-limited access to thumbnail files
- **Retry Logic**: Automatic retry for failed thumbnail generation
- **Event-Driven**: Kafka integration for file lifecycle events

## Architecture

```
File Upload → File Service → Kafka (file.uploaded)
                                ↓
                         Thumbnail Service
                                ↓
                    [Generate Thumbnails]
                                ↓
                    MinIO/S3 Storage + PostgreSQL Metadata
                                ↓
                    Kafka (thumbnail.ready)
                                ↓
                    UI requests thumbnail → Fast URL retrieval
```

## API Endpoints

### Get Thumbnail
```http
GET /api/v1/thumbnails/{fileId}?size={SMALL|GRID|PREVIEW}
```
Returns thumbnail URL or 303 redirect to presigned URL

### Get Thumbnail Status
```http
GET /api/v1/thumbnails/{fileId}/status
```
Returns generation status for all sizes

### Get All Thumbnails
```http
GET /api/v1/thumbnails/{fileId}/all
```
Returns all available thumbnails for a file

### Request Thumbnail Generation (On-Demand)
```http
POST /api/v1/thumbnails/request
Content-Type: application/json

{
  "fileId": "abc123",
  "sizes": ["SMALL", "GRID", "PREVIEW"],
  "force": false
}
```

### Delete Thumbnails
```http
DELETE /api/v1/thumbnails/{fileId}
```

## Configuration

### Thumbnail Sizes
- **SMALL**: 150x150 (grid view)
- **GRID**: 200x200 (default grid)
- **PREVIEW**: 400x400 (preview modal)

### Storage Paths
```
thumbnails/{ownerId}/{fileId}_small.webp
thumbnails/{ownerId}/{fileId}_grid.webp
thumbnails/{ownerId}/{fileId}_preview.webp
```

### Environment Variables
```yaml
POSTGRES_URL: jdbc:postgresql://postgres:5432/gdrive_metadata
KAFKA_BOOTSTRAP_SERVERS: kafka:9092
REDIS_HOST: redis
MINIO_ENDPOINT: http://minio:9000
MINIO_ACCESS_KEY: minioadmin
MINIO_SECRET_KEY: minioadmin
```

## Kafka Topics

### Consumed
- `file.uploaded` - Trigger thumbnail generation
- `file.deleted` - Remove thumbnails

### Produced
- `thumbnail.ready` - Thumbnail generation completed
- `thumbnail.failed` - Thumbnail generation failed

## Database Schema

### thumbnail_metadata table
```sql
CREATE TABLE thumbnail_metadata (
    id VARCHAR(36) PRIMARY KEY,
    file_id VARCHAR(100) NOT NULL,
    owner_id VARCHAR(100) NOT NULL,
    size VARCHAR(20) NOT NULL,
    storage_path VARCHAR(500),
    url VARCHAR(500),
    format VARCHAR(10),
    width INT,
    height INT,
    file_size BIGINT,
    status VARCHAR(20) NOT NULL,
    attempt_count INT DEFAULT 0,
    last_error VARCHAR(1000),
    version INT DEFAULT 1,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    
    INDEX idx_file_id (file_id),
    INDEX idx_owner_id (owner_id),
    INDEX idx_status (status),
    INDEX idx_file_size (file_id, size)
);
```

## Build & Run

### Local Development
```bash
# Build
mvn clean install -DskipTests

# Run
mvn spring-boot:run
```

### Docker
```bash
# Build image
docker build -t thumbnail-service:1.0.0 .

# Run container
docker run -p 8085:8085 \
  -e POSTGRES_URL=jdbc:postgresql://postgres:5432/gdrive_metadata \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  thumbnail-service:1.0.0
```

### Docker Compose
```bash
docker-compose up thumbnail-service
```

## Dependencies

- **Thumbnailator**: Image resizing and processing
- **Apache PDFBox**: PDF rendering and page extraction
- **FFmpeg**: Video frame extraction (system dependency)
- **MinIO SDK**: Object storage operations
- **Spring Kafka**: Event-driven processing
- **Spring Data JPA**: Metadata persistence
- **Spring Data Redis**: Caching

## Implementation TODOs

### High Priority
- [ ] Implement thumbnail generation logic (Thumbnailator for images)
- [ ] Implement PDF page extraction (PDFBox)
- [ ] Implement video frame extraction (FFmpeg wrapper)
- [ ] Implement storage adapter (MinIO client)
- [ ] Implement Kafka consumer event handling
- [ ] Implement presigned URL generation

### Medium Priority
- [ ] Implement Redis caching for thumbnail URLs
- [ ] Implement retry logic with exponential backoff
- [ ] Implement distributed locking (Redis) for idempotency
- [ ] Add metrics and monitoring
- [ ] Add health checks

### Low Priority
- [ ] Add unit tests for generator service
- [ ] Add integration tests for Kafka consumers
- [ ] Optimize image quality vs size tradeoff
- [ ] Add support for animated GIF thumbnails
- [ ] Add support for AVIF format

## Performance Considerations

- **Worker Pool**: Configure based on CPU cores and memory
- **Async Processing**: Non-blocking thumbnail generation
- **Batch Processing**: Process multiple sizes in parallel
- **CDN Integration**: Serve thumbnails from CDN in production
- **Cache Strategy**: Cache thumbnail URLs in Redis with TTL

## Security

- **Access Control**: Validate user ownership before returning URLs
- **Presigned URLs**: Time-limited (1 hour) access to thumbnail files
- **Input Validation**: Sanitize file paths and content types
- **Resource Limits**: Limit max file size for processing

## Monitoring

- **Metrics**: Jobs processed, failures, latency, cache hit rate
- **Logs**: Correlation IDs for tracing thumbnail pipeline
- **Alerts**: High failure rate, slow processing, queue backlog

## Port

- **8085**: HTTP API and health checks
