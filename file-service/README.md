# File Service

## Overview
The File Service handles all file upload, download, and deletion operations in the G-Nex system. It integrates with MinIO for object storage and publishes events to Kafka for downstream processing.

## Features
- ✅ **File Upload** - Upload files to MinIO object storage (up to 100MB)
- ✅ **File Download** - Stream files from object storage
- ✅ **File Deletion** - Remove files from storage
- ✅ **File Validation** - Size, type, and name validation
- ✅ **Kafka Events** - Publishes upload/download/delete events
- ✅ **Checksum Calculation** - MD5 checksum for integrity
- ✅ **Automatic Sanitization** - Safe filename handling

## Architecture Components

### 1. File Controller (`FileController`)
REST API endpoints for file operations:
- `POST /api/v1/files/upload` - Upload file
- `GET /api/v1/files/{fileId}/download` - Download file
- `DELETE /api/v1/files/{fileId}` - Delete file
- `GET /api/v1/files/{fileId}` - Get file metadata

### 2. File Service (`FileServiceImpl`)
Core business logic:
- File validation (size, type, name)
- Unique file ID generation (UUID)
- Filename sanitization
- Checksum calculation
- Event publishing to Kafka

### 3. Object Storage Service (`MinioStorageService`)
MinIO integration:
- Upload files to buckets
- Download files as streams
- Delete files
- Check file existence
- Generate pre-signed URLs (1 hour expiry)
- Retrieve file metadata

### 4. Kafka Producer Service (`KafkaProducerServiceImpl`)
Event publishing:
- `file.uploaded` topic
- `file.downloaded` topic
- `file.deleted` topic

### 5. Configuration
- `MinioConfig` - MinIO client setup, bucket initialization
- `KafkaProducerConfig` - Kafka producer with reliability settings

## Configuration

### MinIO Settings (`application.yml`)
```yaml
minio:
  url: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: gdrive-files
```

### File Constraints
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB
```

### Kafka Settings
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      acks: all
      retries: 3
      enable-idempotence: true
```

## API Endpoints

### Upload File
```bash
POST /api/v1/files/upload
Content-Type: multipart/form-data
X-User-Id: user123

Form Data:
file: <binary file data>

Response (201 Created):
{
  "success": true,
  "message": "File uploaded successfully",
  "data": {
    "fileId": "123e4567-e89b-12d3-a456-426614174000",
    "fileName": "document.pdf",
    "fileType": "pdf",
    "fileSize": 1048576,
    "ownerId": "user123",
    "storagePath": "user123/123e4567-e89b-12d3-a456-426614174000.pdf",
    "contentType": "application/pdf",
    "checksum": "5d41402abc4b2a76b9719d911017c592",
    "uploadedAt": "2026-02-09T10:30:00",
    "status": "UPLOADED"
  },
  "timestamp": "2026-02-09T10:30:00",
  "requestId": "req-uuid"
}
```

### Download File
```bash
GET /api/v1/files/{fileId}/download
X-User-Id: user123

Response (200 OK):
Content-Type: application/octet-stream
Content-Disposition: attachment; filename="fileId"
<binary file data>
```

### Delete File
```bash
DELETE /api/v1/files/{fileId}
X-User-Id: user123

Response (200 OK):
{
  "success": true,
  "message": "File deleted successfully",
  "data": null
}
```

## File Upload Flow

```
1. Client uploads file via API Gateway
   ↓
2. FileController receives multipart file
   ↓
3. FileService validates file:
   - Size ≤ 100MB
   - Valid filename
   - Supported type (optional)
   ↓
4. Generate unique file ID (UUID)
   ↓
5. Sanitize filename (remove special chars)
   ↓
6. Calculate MD5 checksum
   ↓
7. Upload to MinIO:
   - Path: userId/fileId.extension
   - Bucket: gdrive-files
   ↓
8. Build FileMetadataDto
   ↓
9. Publish Kafka event:
   - Topic: file.uploaded
   - Contains: fileId, userId, metadata
   ↓
10. Return metadata to client (201 Created)
```

## Event Publishing

### File Uploaded Event
```json
{
  "eventId": "evt-uuid",
  "eventType": "FILE_UPLOADED",
  "fileId": "123e4567-e89b-12d3-a456-426614174000",
  "fileName": "document.pdf",
  "userId": "user123",
  "timestamp": "2026-02-09T10:30:00",
  "payload": {
    "fileName": "document.pdf",
    "fileSize": 1048576,
    "fileType": "pdf",
    "contentType": "application/pdf",
    "storagePath": "user123/123e4567.pdf",
    "checksum": "5d41402abc4b2a76b9719d911017c592"
  },
  "source": "file-service"
}
```

### Consumers
These events are consumed by:
- **Metadata Service** - Saves metadata to PostgreSQL
- **Search Service** - Indexes file in Elasticsearch
- **Activity Service** - Logs user activity

## Running the Service

### Prerequisites
- Java 17+
- MinIO running on localhost:9000
- Kafka running on localhost:9092
- Maven 3.8+

### Start MinIO
```powershell
docker run -d \
  -p 9000:9000 \
  -p 9001:9001 \
  --name gdrive-minio \
  -e MINIO_ROOT_USER=minioadmin \
  -e MINIO_ROOT_PASSWORD=minioadmin \
  minio/minio server /data --console-address ":9001"
```

### Start Kafka
```powershell
# Start Zookeeper
docker run -d --name zookeeper -p 2181:2181 confluentinc/cp-zookeeper:7.5.0 \
  -e ZOOKEEPER_CLIENT_PORT=2181

# Start Kafka
docker run -d --name kafka -p 9092:9092 \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  confluentinc/cp-kafka:7.5.0
```

### Build and Run
```powershell
# Build common-lib first
cd "c:\Users\ARAVIND KUMAR\Desktop\G-Nex"
mvn clean install -pl common-lib

# Run file-service
cd file-service
mvn spring-boot:run
```

The service will start on **http://localhost:8081**

## Testing

### 1. Health Check
```powershell
curl http://localhost:8081/health
```

### 2. Upload File
```powershell
curl -X POST http://localhost:8081/api/v1/files/upload \
  -H "X-User-Id: user123" \
  -F "file=@C:\path\to\file.pdf"
```

### 3. Download File
```powershell
curl -o downloaded-file.pdf \
  http://localhost:8081/api/v1/files/{fileId}/download \
  -H "X-User-Id: user123"
```

### 4. Delete File
```powershell
curl -X DELETE http://localhost:8081/api/v1/files/{fileId} \
  -H "X-User-Id: user123"
```

### 5. Swagger UI
Access interactive API docs:
```
http://localhost:8081/swagger-ui.html
```

## Validation Rules

### File Size
- Maximum: 100MB (104,857,600 bytes)
- Validated before upload starts
- Returns 400 Bad Request if exceeded

### File Name
- Cannot be empty or null
- Automatically sanitized:
  - Special characters → underscores
  - Multiple underscores → single underscore
  - Leading/trailing underscores removed

### File Type (Optional)
Allowed types (can be configured):
- Images: `image/jpeg`, `image/png`, `image/gif`
- Documents: `application/pdf`, `application/msword`, `text/plain`
- Videos: `video/mp4`

## Storage Structure

Files stored in MinIO with path:
```
bucket: gdrive-files
path: {userId}/{fileId}.{extension}

Example:
bucket: gdrive-files
path: user123/123e4567-e89b-12d3-a456-426614174000.pdf
```

## Error Handling

### Common Errors
| Error | Status | Message |
|-------|--------|---------|
| File too large | 400 | File size exceeds maximum allowed size |
| Empty file | 400 | File is empty or null |
| Invalid filename | 400 | File name is empty |
| Upload failed | 500 | Failed to upload file to object storage |
| File not found | 404 | File not found |
| Download failed | 500 | Failed to download file from object storage |

## Monitoring

### Actuator Endpoints
- `/actuator/health` - Service health
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics

### Key Metrics
- File upload count
- File download count
- File deletion count
- Upload/download durations
- Error rates
- MinIO connection status
- Kafka producer metrics

## Security Notes

⚠️ **Production Considerations:**
1. **MinIO Credentials** - Use secure credentials, not defaults
2. **File Type Validation** - Enable strict validation
3. **Virus Scanning** - Add antivirus scanning before storage
4. **Access Control** - Validate user permissions before operations
5. **Rate Limiting** - Implement upload/download rate limits
6. **Encryption** - Enable encryption at rest in MinIO
7. **Audit Logging** - Log all file operations

## File Structure
```
file-service/
├── src/main/java/com/gnexdrive/fileservice/
│   ├── FileServiceApplication.java
│   ├── config/
│   │   ├── MinioConfig.java
│   │   └── KafkaProducerConfig.java
│   ├── controller/
│   │   ├── FileController.java
│   │   └── HealthController.java
│   ├── service/
│   │   ├── FileService.java
│   │   ├── ObjectStorageService.java
│   │   ├── KafkaProducerService.java
│   │   └── impl/
│   │       ├── FileServiceImpl.java
│   │       ├── MinioStorageService.java
│   │       └── KafkaProducerServiceImpl.java
└── src/main/resources/
    ├── application.yml
    └── application-docker.yml
```

## Troubleshooting

### MinIO Connection Failed
```
Error: Unable to connect to MinIO
Solution: Ensure MinIO is running on localhost:9000
Check: docker ps | grep minio
```

### Kafka Connection Failed
```
Error: Failed to send message to Kafka
Solution: Ensure Kafka and Zookeeper are running
Check: docker ps | grep kafka
```

### File Upload Fails
```
Error: Failed to upload file to object storage
Check:
1. MinIO bucket exists (gdrive-files)
2. MinIO credentials are correct
3. File size within limits
4. Sufficient disk space
```

### Bucket Not Found
```
Error: Bucket 'gdrive-files' does not exist
Solution: Service auto-creates bucket on startup
Check: MinIO console at http://localhost:9001
```

## What's Implemented

✅ Complete file upload with validation  
✅ File download with streaming  
✅ File deletion  
✅ MinIO integration (upload, download, delete, exists, URL generation)  
✅ Kafka event publishing (upload, download, delete)  
✅ File validation (size, name, type)  
✅ Filename sanitization  
✅ MD5 checksum calculation  
✅ Automatic bucket creation  
✅ Health check endpoints  
✅ Swagger/OpenAPI documentation  
✅ Comprehensive error handling  
✅ Structured logging  

## Next Steps

Once file-service is running:
1. Implement **metadata-service** to consume events and store metadata
2. Implement **search-service** to index files in Elasticsearch
3. Implement **activity-service** to log user actions
4. Test end-to-end flow through API Gateway

---

**Status**: ✅ **COMPLETE AND READY TO USE**

The File Service is fully implemented with MinIO object storage, Kafka event publishing, and comprehensive file operations. Ready to handle file uploads, downloads, and deletions in the G-Nex ecosystem.
