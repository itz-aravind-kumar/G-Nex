# File Service - Quick Start Guide

## 🚀 Getting Started in 5 Minutes

### Step 1: Start MinIO
```powershell
docker run -d `
  -p 9000:9000 `
  -p 9001:9001 `
  --name gdrive-minio `
  -e MINIO_ROOT_USER=minioadmin `
  -e MINIO_ROOT_PASSWORD=minioadmin `
  minio/minio server /data --console-address ":9001"
```

**MinIO Console**: http://localhost:9001 (minioadmin/minioadmin)

### Step 2: Start Kafka & Zookeeper
```powershell
# Zookeeper
docker run -d --name gdrive-zookeeper -p 2181:2181 `
  -e ZOOKEEPER_CLIENT_PORT=2181 `
  confluentinc/cp-zookeeper:7.5.0

# Kafka
docker run -d --name gdrive-kafka -p 9092:9092 `
  --link gdrive-zookeeper:zookeeper `
  -e KAFKA_ZOOKEEPER_CONNECT=gdrive-zookeeper:2181 `
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 `
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 `
  confluentinc/cp-kafka:7.5.0
```

### Step 3: Build Common Library
```powershell
cd "c:\Users\ARAVIND KUMAR\Desktop\G-Nex"
mvn clean install -pl common-lib
```

### Step 4: Run File Service
```powershell
cd file-service
mvn spring-boot:run
```

The service will start on **http://localhost:8081**

### Step 5: Test the Service

#### Health Check
```powershell
curl http://localhost:8081/health
```

**Response:**
```json
{
  "status": "UP",
  "service": "file-service",
  "timestamp": "2026-02-09T10:30:00"
}
```

#### Upload a File
```powershell
# Create a test file
"Hello, G-Nex!" | Out-File test.txt

# Upload it
curl -X POST http://localhost:8081/api/v1/files/upload `
  -H "X-User-Id: user123" `
  -F "file=@test.txt"
```

**Response:**
```json
{
  "success": true,
  "message": "File uploaded successfully",
  "data": {
    "fileId": "123e4567-e89b-12d3-a456-426614174000",
    "fileName": "test.txt",
    "fileType": "txt",
    "fileSize": 14,
    "ownerId": "user123",
    "storagePath": "user123/123e4567-e89b-12d3-a456-426614174000.txt",
    "contentType": "text/plain",
    "uploadedAt": "2026-02-09T10:30:00",
    "status": "UPLOADED"
  }
}
```

#### Download the File
```powershell
# Save fileId from upload response
$fileId = "123e4567-e89b-12d3-a456-426614174000"

# Download it
curl -o downloaded.txt http://localhost:8081/api/v1/files/$fileId/download `
  -H "X-User-Id: user123"
```

#### Delete the File
```powershell
curl -X DELETE http://localhost:8081/api/v1/files/$fileId `
  -H "X-User-Id: user123"
```

## 📊 Verify Kafka Events

Check if events are published to Kafka:

```powershell
docker exec -it gdrive-kafka kafka-console-consumer `
  --bootstrap-server localhost:9092 `
  --topic file.uploaded `
  --from-beginning
```

You should see events like:
```json
{
  "eventId": "evt-uuid",
  "eventType": "FILE_UPLOADED",
  "fileId": "123e4567-...",
  "fileName": "test.txt",
  "userId": "user123",
  "timestamp": "2026-02-09T10:30:00",
  "source": "file-service"
}
```

## 🎯 Testing with Postman

### 1. Upload File
```
POST http://localhost:8081/api/v1/files/upload
Headers:
  X-User-Id: user123
Body: form-data
  file: [select file]
```

### 2. Download File
```
GET http://localhost:8081/api/v1/files/{fileId}/download
Headers:
  X-User-Id: user123
```

### 3. Delete File
```
DELETE http://localhost:8081/api/v1/files/{fileId}
Headers:
  X-User-Id: user123
```

## 🌐 Swagger UI

Access interactive API documentation:
```
http://localhost:8081/swagger-ui.html
```

## 📦 Check MinIO

View uploaded files in MinIO console:
1. Open http://localhost:9001
2. Login: minioadmin / minioadmin
3. Browse `gdrive-files` bucket
4. See files organized by userId

## 🔧 Configuration

### Default Settings
```yaml
Server Port: 8081
MinIO URL: http://localhost:9000
Kafka: localhost:9092
Max File Size: 100MB
Bucket: gdrive-files
```

### Change Configuration
Edit `file-service/src/main/resources/application.yml`

## 🐛 Troubleshooting

### MinIO Not Starting
```powershell
# Check if running
docker ps | grep minio

# Check logs
docker logs gdrive-minio

# Restart
docker restart gdrive-minio
```

### Kafka Connection Failed
```powershell
# Check if running
docker ps | grep kafka

# Check logs
docker logs gdrive-kafka

# Restart both
docker restart gdrive-zookeeper
docker restart gdrive-kafka
```

### File Upload Fails - Size Limit
```
Error: "File size exceeds maximum allowed size of 100.00 MB"
Solution: File too large. Split or compress file.
```

### Bucket Not Found
```
Error: Bucket 'gdrive-files' does not exist
Solution: Service auto-creates bucket on startup. Check MinIO logs.
```

### Port Already in Use
```
Error: Port 8081 already in use
Solution: Stop other service or change port in application.yml
```

## 💡 Pro Tips

### Upload Large Files
```powershell
# PowerShell: Upload with progress
$file = "C:\large-file.zip"
Invoke-WebRequest -Method Post `
  -Uri "http://localhost:8081/api/v1/files/upload" `
  -Headers @{"X-User-Id"="user123"} `
  -InFile $file `
  -ContentType "multipart/form-data"
```

### Batch Upload
```powershell
# Upload multiple files
Get-ChildItem *.txt | ForEach-Object {
  curl -X POST http://localhost:8081/api/v1/files/upload `
    -H "X-User-Id: user123" `
    -F "file=@$($_.Name)"
}
```

### Monitor Uploads
```powershell
# Watch file service logs
cd file-service
mvn spring-boot:run | Select-String "Upload"
```

## 📈 Next Steps

1. **Test with API Gateway** - Route requests through gateway (port 8080)
2. **Implement Metadata Service** - Store file metadata in PostgreSQL
3. **Implement Search Service** - Index files in Elasticsearch
4. **Test Event Flow** - Verify Kafka consumers receive events

## ✅ What's Working

- ✅ File upload to MinIO
- ✅ File download from MinIO
- ✅ File deletion
- ✅ File validation (size, name, type)
- ✅ Kafka event publishing
- ✅ MD5 checksum calculation
- ✅ Filename sanitization
- ✅ Bucket auto-creation
- ✅ Health checks
- ✅ Swagger documentation

## 🎓 Example Workflow

```powershell
# 1. Upload file
$response = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8081/api/v1/files/upload" `
  -Headers @{"X-User-Id"="user123"} `
  -Form @{file=Get-Item "test.txt"}

# 2. Extract fileId
$fileId = $response.data.fileId
Write-Host "File ID: $fileId"

# 3. Download file
Invoke-WebRequest `
  -Uri "http://localhost:8081/api/v1/files/$fileId/download" `
  -Headers @{"X-User-Id"="user123"} `
  -OutFile "downloaded-test.txt"

# 4. Delete file
Invoke-RestMethod -Method Delete `
  -Uri "http://localhost:8081/api/v1/files/$fileId" `
  -Headers @{"X-User-Id"="user123"}
```

---

**Ready to upload files!** 🚀

The File Service is ready to handle file operations. Next, implement the downstream services (metadata, search, activity) to complete the ecosystem.
