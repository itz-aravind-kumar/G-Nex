# Metadata Service - Quick Start Guide

Get the Metadata Service running in 5 minutes!

## Prerequisites

- Java 17+
- Maven 3.9+
- Docker Desktop
- Common-lib built (`mvn install -pl common-lib`)

## Step 1: Start PostgreSQL (1 minute)

```powershell
docker run -d `
  --name postgres-metadata `
  -e POSTGRES_DB=gdrive_metadata `
  -e POSTGRES_USER=gdrive_user `
  -e POSTGRES_PASSWORD=gdrive_pass `
  -p 5432:5432 `
  postgres:16
```

Verify:
```powershell
docker ps | Select-String postgres
```

## Step 2: Start Kafka (1 minute)

```powershell
# Start Zookeeper
docker run -d `
  --name zookeeper `
  -p 2181:2181 `
  -e ZOOKEEPER_CLIENT_PORT=2181 `
  confluentinc/cp-zookeeper:latest

# Start Kafka
docker run -d `
  --name kafka `
  -p 9092:9092 `
  -e KAFKA_ZOOKEEPER_CONNECT=localhost:2181 `
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 `
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 `
  confluentinc/cp-kafka:latest
```

## Step 3: Run Metadata Service (2 minutes)

```powershell
cd metadata-service
mvn spring-boot:run
```

Wait for: `Started MetadataServiceApplication in X seconds`

## Step 4: Test the Service (1 minute)

### Health Check

```powershell
curl http://localhost:8082/health
```

Expected:
```json
{
  "success": true,
  "data": {
    "status": "UP",
    "service": "metadata-service",
    "database": "UP"
  }
}
```

### Simulate File Upload Event

Publish test event to Kafka:

```powershell
# Windows PowerShell - create test event file
@"
{
  "eventId": "test-1",
  "eventType": "FILE_UPLOADED",
  "fileId": "test-file-123",
  "userId": "user123",
  "timestamp": "2026-02-09T10:00:00",
  "payload": {
    "fileName": "test-document.pdf",
    "fileType": "pdf",
    "fileSize": 1048576,
    "contentType": "application/pdf",
    "storagePath": "user123/test-file-123.pdf",
    "checksum": "abc123def456",
    "ownerEmail": "test@example.com"
  }
}
"@ | docker exec -i kafka kafka-console-producer --broker-list localhost:9092 --topic file.uploaded
```

### Query the Metadata

```powershell
curl http://localhost:8082/api/v1/metadata/test-file-123 `
  -H "X-User-Id: user123"
```

Expected:
```json
{
  "success": true,
  "message": "File metadata retrieved successfully",
  "data": {
    "fileId": "test-file-123",
    "fileName": "test-document.pdf",
    "fileSize": 1048576,
    "ownerId": "user123",
    "status": "UPLOADED"
  }
}
```

## Common Operations

### Get User Files (Paginated)

```powershell
curl "http://localhost:8082/api/v1/metadata/user/user123?page=0&size=10"
```

### Search Files

```powershell
curl "http://localhost:8082/api/v1/metadata/user/user123/search?query=test"
```

### Get Storage Stats

```powershell
curl http://localhost:8082/api/v1/metadata/user/user123/stats
```

### Update File Metadata

```powershell
curl -X PUT http://localhost:8082/api/v1/metadata/test-file-123 `
  -H "Content-Type: application/json" `
  -H "X-User-Id: user123" `
  -d '{\"fileName\": \"renamed-document.pdf\", \"status\": \"AVAILABLE\"}'
```

### Delete File Metadata

```powershell
curl -X DELETE http://localhost:8082/api/v1/metadata/test-file-123 `
  -H "X-User-Id: user123"
```

## Swagger UI

Open API documentation:

```powershell
Start-Process "http://localhost:8082/swagger-ui.html"
```

## Database Access

Connect to PostgreSQL:

```powershell
docker exec -it postgres-metadata psql -U gdrive_user -d gdrive_metadata
```

Useful queries:

```sql
-- View all metadata
SELECT * FROM file_metadata;

-- Count files by user
SELECT owner_id, COUNT(*) FROM file_metadata GROUP BY owner_id;

-- Total storage by user
SELECT owner_id, SUM(file_size) FROM file_metadata GROUP BY owner_id;

-- Files by status
SELECT status, COUNT(*) FROM file_metadata GROUP BY status;
```

## Kafka Topics

List topics:

```powershell
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list
```

Expected topics:
- `file.uploaded`
- `file.deleted`
- `metadata.updated`

Monitor consumer group:

```powershell
docker exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --group metadata-service --describe
```

## Testing Full Workflow

### 1. Upload File via File Service

```powershell
curl -X POST http://localhost:8081/api/v1/files/upload `
  -H "X-User-Id: user123" `
  -F "file=@test.txt"
```

### 2. Event Auto-Consumed by Metadata Service

Check logs: `Received file uploaded event`

### 3. Query Metadata

```powershell
curl http://localhost:8082/api/v1/metadata/{fileId} `
  -H "X-User-Id: user123"
```

### 4. Check Stats

```powershell
curl http://localhost:8082/api/v1/metadata/user/user123/stats
```

## Troubleshooting

### ❌ Database Connection Error

```powershell
# Check PostgreSQL is running
docker ps | Select-String postgres

# View logs
docker logs postgres-metadata

# Restart container
docker restart postgres-metadata
```

### ❌ Kafka Connection Error

```powershell
# Check Kafka is running
docker ps | Select-String kafka

# Check Zookeeper
docker ps | Select-String zookeeper

# Restart Kafka
docker restart kafka
```

### ❌ Events Not Consumed

```powershell
# Check consumer lag
docker exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --group metadata-service --describe

# Check service logs
Get-Content metadata-service.log -Wait -Tail 50
```

### ❌ Metadata Not Found

```sql
-- Check if record exists
docker exec -it postgres-metadata psql -U gdrive_user -d gdrive_metadata -c "SELECT * FROM file_metadata WHERE file_id = 'your-file-id';"
```

## Pro Tips

💡 **Tip 1:** Use Swagger UI for interactive testing  
💡 **Tip 2:** Monitor Kafka consumer lag for event processing delays  
💡 **Tip 3:** Check database indexes for performance optimization  
💡 **Tip 4:** Use pagination for large file lists  
💡 **Tip 5:** Enable SQL logging for debugging: `spring.jpa.show-sql=true`

## Environment Variables

Override defaults:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/gdrive_metadata"
$env:SPRING_DATASOURCE_USERNAME="gdrive_user"
$env:SPRING_DATASOURCE_PASSWORD="gdrive_pass"
$env:SPRING_KAFKA_BOOTSTRAP_SERVERS="localhost:9092"

mvn spring-boot:run
```

## Docker Compose (Alternative)

Use project root `docker-compose.yml`:

```powershell
cd ..
docker-compose up -d postgres kafka
cd metadata-service
mvn spring-boot:run
```

## Clean Up

```powershell
# Stop service: Ctrl+C

# Remove containers
docker rm -f postgres-metadata kafka zookeeper

# Clean build
mvn clean
```

## Next Steps

1. ✅ Start File Service (port 8081)
2. ✅ Start API Gateway (port 8080)
3. ✅ Upload file and watch metadata auto-save
4. ✅ Query metadata via API Gateway

## Support

- 📖 Full documentation: `README.md`
- 🔍 Architecture: `../ARCHITECTURE.md`
- 🐛 Issues: Check logs and troubleshooting section

---

**Service:** Metadata Service  
**Port:** 8082  
**Status:** ✅ Ready  
**Dependencies:** PostgreSQL, Kafka
