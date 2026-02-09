# 🚀 G-Nex System - Ready to Test!

## ✅ What's Complete

### Infrastructure (Running)
- ✅ **PostgreSQL** - Port 5432 (Database)
- ✅ **Redis** - Port 6379 (Cache/Rate Limiting)
- ✅ **Kafka** - Port 9092 (Event Bus)
- ✅ **Zookeeper** - Port 2181 (Kafka dependency)
- ✅ **MinIO** - Ports 9000/9001 (Object Storage)

### Microservices (Ready to Start)
- ✅ **API Gateway** - Port 8080 (JWT, Rate Limiting, Routing)
- ✅ **File Service** - Port 8081 (Upload/Download, MinIO, Kafka)
- ✅ **Metadata Service** - Port 8082 (PostgreSQL, Kafka Consumer)

### Common Library
- ✅ Built and installed in local Maven repository

## 📋 Quick Start (3 Steps)

### Step 1: Open 3 PowerShell Terminals

You need 3 separate terminals to run the services.

### Step 2: Start Services

**Terminal 1 - API Gateway:**
```powershell
cd "c:\Users\ARAVIND KUMAR\Desktop\G-Nex\api-gateway"
mvn spring-boot:run
```
Wait for: `Started ApiGatewayApplication in X seconds`

**Terminal 2 - File Service:**
```powershell
cd "c:\Users\ARAVIND KUMAR\Desktop\G-Nex\file-service"
mvn spring-boot:run
```
Wait for: `Started FileServiceApplication in X seconds`

**Terminal 3 - Metadata Service:**
```powershell
cd "c:\Users\ARAVIND KUMAR\Desktop\G-Nex\metadata-service"
mvn spring-boot:run
```
Wait for: `Started MetadataServiceApplication in X seconds`

### Step 3: Run Test Script

Once all 3 services show "Started", open a new terminal:

```powershell
cd "c:\Users\ARAVIND KUMAR\Desktop\G-Nex"
.\run-test.ps1
```

This will automatically:
1. ✅ Check all services are healthy
2. ✅ Generate JWT token
3. ✅ Upload a test file
4. ✅ Wait for Kafka event processing
5. ✅ Query metadata from database
6. ✅ Download the file back
7. ✅ Show storage statistics
8. ✅ Display summary

## 🧪 Manual Testing

If you prefer manual testing:

### 1. Health Check All Services

```powershell
# API Gateway
curl http://localhost:8080/health

# File Service
curl http://localhost:8081/health

# Metadata Service
curl http://localhost:8082/health
```

### 2. Generate JWT Token

```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/auth/generate-token" `
    -Method Post `
    -ContentType "application/json" `
    -Body '{"userId":"user123","username":"testuser","email":"test@example.com"}'

$token = $response.data.token
Write-Host "Token: $token"
```

### 3. Upload File

```powershell
# Create test file
"Hello World!" | Out-File test.txt

# Upload through API Gateway
curl -X POST http://localhost:8080/file-service/api/v1/files/upload `
    -H "Authorization: Bearer $token" `
    -F "file=@test.txt"
```

### 4. Wait and Check Metadata

```powershell
# Wait 3 seconds for Kafka processing
Start-Sleep -Seconds 3

# Query metadata (replace YOUR_FILE_ID with actual ID from upload response)
curl http://localhost:8080/metadata-service/api/v1/metadata/YOUR_FILE_ID `
    -H "Authorization: Bearer $token"
```

### 5. Download File

```powershell
curl http://localhost:8080/file-service/api/v1/files/YOUR_FILE_ID/download `
    -H "Authorization: Bearer $token" `
    -o downloaded.txt
```

## 🔍 Monitoring & Debugging

### Check Kafka Topics

```powershell
docker exec gdrive-kafka kafka-topics --bootstrap-server localhost:9092 --list
```

Expected topics:
- `file.uploaded`
- `file.deleted`
- `file.downloaded`
- `metadata.updated`

### Check PostgreSQL Database

```powershell
docker exec -it gdrive-postgres psql -U gdrive_user -d gdrive_metadata

# Inside psql:
\dt                                    # List tables
SELECT * FROM file_metadata;           # View all metadata
\q                                     # Exit
```

### Check MinIO Storage

Open browser: http://localhost:9001
- Username: `minioadmin`
- Password: `minioadmin`
- Bucket: `gdrive-files`

### Check Kafka Consumer Group

```powershell
docker exec gdrive-kafka kafka-consumer-groups `
    --bootstrap-server localhost:9092 `
    --group metadata-service `
    --describe
```

### View Service Logs

Check the terminal where each service is running for real-time logs.

## 🌐 Swagger UI

Once services are running:

- **File Service API**: http://localhost:8081/swagger-ui.html
- **Metadata Service API**: http://localhost:8082/swagger-ui.html

## 🎯 Complete Workflow Test

```
1. Client generates JWT token through API Gateway
   ↓
2. Client uploads file through API Gateway → File Service
   ↓
3. File Service stores file in MinIO
   ↓
4. File Service publishes "file.uploaded" event to Kafka
   ↓
5. Metadata Service consumes event from Kafka
   ↓
6. Metadata Service saves metadata to PostgreSQL
   ↓
7. Client queries metadata through API Gateway → Metadata Service
   ↓
8. Client downloads file through API Gateway → File Service
   ↓
9. All operations authenticated with JWT
```

## 📊 What Gets Tested

### API Gateway
- ✅ JWT token generation
- ✅ JWT authentication (Bearer token)
- ✅ Request routing to file-service
- ✅ Request routing to metadata-service
- ✅ Rate limiting (Redis-backed)
- ✅ Circuit breaker (fallback responses)
- ✅ Health checks

### File Service
- ✅ File upload with validation (size, name)
- ✅ Storage in MinIO (object storage)
- ✅ Kafka event publishing (file.uploaded)
- ✅ File download with streaming
- ✅ MD5 checksum calculation
- ✅ Filename sanitization
- ✅ Health checks

### Metadata Service
- ✅ Kafka event consumption (file.uploaded)
- ✅ Metadata extraction from events
- ✅ PostgreSQL persistence
- ✅ Metadata query by file ID
- ✅ User files listing (paginated)
- ✅ Storage statistics calculation
- ✅ Health checks with database validation

### Integration
- ✅ End-to-end file upload → metadata save
- ✅ Event-driven architecture (Kafka)
- ✅ Distributed system communication
- ✅ Database persistence
- ✅ Object storage integration

## ⚠️ Troubleshooting

### Service won't start

**Check if port is already in use:**
```powershell
Get-NetTCPConnection -LocalPort 8080,8081,8082 -ErrorAction SilentlyContinue
```

**Solution:** Stop the conflicting process or change the port in `application.yml`

### Kafka events not consumed

**Check if Kafka is running:**
```powershell
docker ps | Select-String kafka
```

**Check consumer lag:**
```powershell
docker exec gdrive-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --group metadata-service --describe
```

### Database connection error

**Check if PostgreSQL is running:**
```powershell
docker ps | Select-String postgres
```

**Test connection:**
```powershell
docker exec gdrive-postgres pg_isready -U gdrive_user
```

### MinIO connection error

**Check if MinIO is running:**
```powershell
docker ps | Select-String minio
```

**Check MinIO health:**
```powershell
curl http://localhost:9000/minio/health/live
```

### JWT token expired

Tokens expire in 1 hour. Generate a new one:
```powershell
curl -X POST http://localhost:8080/auth/generate-token `
    -H "Content-Type: application/json" `
    -d '{"userId":"user123","username":"testuser","email":"test@example.com"}'
```

## 🎉 Success Indicators

You'll know everything is working when:

1. ✅ All 3 services start without errors
2. ✅ All health checks return `"status": "UP"`
3. ✅ File upload returns 201 with file ID
4. ✅ Metadata query (after 3 sec) returns file info
5. ✅ Downloaded file matches uploaded file
6. ✅ Storage stats show correct file count

## 📁 Project Files

### Documentation
- `TEST-RUN.md` - Detailed testing guide
- `README.md` - Project overview
- `ARCHITECTURE.md` - System architecture
- `QUICKSTART.md` - Quick start guides per service

### Scripts
- `run-test.ps1` - Automated test script (use this!)
- `start-services.ps1` - Helper for common-lib build
- `docker-compose.yml` - Infrastructure setup

### Services
- `api-gateway/` - Port 8080
- `file-service/` - Port 8081
- `metadata-service/` - Port 8082
- `common-lib/` - Shared library

## 🚀 Next Steps After Testing

Once basic testing is complete:

1. **Implement Search Service** - Elasticsearch integration
2. **Implement Activity Service** - User activity tracking
3. **Performance Testing** - Load test with JMeter
4. **Security Hardening** - Production-ready security
5. **Monitoring Setup** - Prometheus + Grafana
6. **Frontend Development** - React UI

## 💡 Pro Tips

1. **Keep terminals organized** - Label each terminal window
2. **Watch logs** - Errors appear in service terminals
3. **Check Kafka lag** - If metadata not appearing
4. **Use Swagger UI** - Interactive API testing
5. **Test incrementally** - One operation at a time

## 📞 Support

If something doesn't work:

1. Check service logs in respective terminals
2. Verify infrastructure: `docker-compose ps`
3. Check network connectivity: `curl http://localhost:PORT/health`
4. Review documentation: `TEST-RUN.md`, `README.md`
5. Check TODO.md for known limitations

---

## ⚡ Quick Command Reference

```powershell
# Infrastructure
docker-compose up -d postgres redis kafka zookeeper minio
docker-compose ps
docker-compose down

# Build
mvn clean install -pl common-lib -DskipTests

# Run Services (3 terminals)
cd api-gateway && mvn spring-boot:run
cd file-service && mvn spring-boot:run
cd metadata-service && mvn spring-boot:run

# Test
.\run-test.ps1

# Health Checks
curl http://localhost:8080/health
curl http://localhost:8081/health
curl http://localhost:8082/health
```

---

**Status**: 🟢 Ready to Test  
**Services**: 3/5 Complete (API Gateway, File, Metadata)  
**Infrastructure**: ✅ Running  
**Documentation**: ✅ Complete
