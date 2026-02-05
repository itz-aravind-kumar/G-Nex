# Quick Start Guide

## Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- (Optional) Kubernetes (minikube/k3s)

## Local Development Setup

### 1. Start Infrastructure Services
```powershell
docker-compose up -d postgres redis elasticsearch kafka zookeeper minio
```

Wait for all services to be healthy (~2 minutes).

### 2. Build All Projects
```powershell
mvn clean install -DskipTests
```

This builds:
- common-lib (shared library)
- api-gateway
- file-service
- metadata-service
- search-service
- activity-service

### 3. Run Services

**Option A: Using Maven (Recommended for Development)**

Open 5 separate PowerShell terminals:

**Terminal 1 - API Gateway**
```powershell
cd api-gateway
mvn spring-boot:run
```

**Terminal 2 - File Service**
```powershell
cd file-service
mvn spring-boot:run
```

**Terminal 3 - Metadata Service**
```powershell
cd metadata-service
mvn spring-boot:run
```

**Terminal 4 - Search Service**
```powershell
cd search-service
mvn spring-boot:run
```

**Terminal 5 - Activity Service**
```powershell
cd activity-service
mvn spring-boot:run
```

**Option B: Using Docker Compose (Full Stack)**

First, build Docker images:
```powershell
# Build each service
cd api-gateway; mvn clean package; docker build -t gnexdrive/api-gateway:latest .; cd ..
cd file-service; mvn clean package; docker build -t gnexdrive/file-service:latest .; cd ..
cd metadata-service; mvn clean package; docker build -t gnexdrive/metadata-service:latest .; cd ..
cd search-service; mvn clean package; docker build -t gnexdrive/search-service:latest .; cd ..
cd activity-service; mvn clean package; docker build -t gnexdrive/activity-service:latest .; cd ..
```

Then run everything:
```powershell
docker-compose up
```

### 4. Verify Services

Check if all services are running:

```powershell
# API Gateway
curl http://localhost:8080/actuator/health

# File Service
curl http://localhost:8081/actuator/health

# Metadata Service
curl http://localhost:8082/actuator/health

# Search Service
curl http://localhost:8083/actuator/health

# Activity Service
curl http://localhost:8084/actuator/health
```

Or visit Swagger UIs:
- http://localhost:8081/swagger-ui.html (File Service)
- http://localhost:8082/swagger-ui.html (Metadata Service)
- http://localhost:8083/swagger-ui.html (Search Service)
- http://localhost:8084/swagger-ui.html (Activity Service)

## Testing the APIs

### Upload a File
```powershell
curl -X POST http://localhost:8080/api/v1/files/upload `
  -H "X-User-Id: user123" `
  -F "file=@C:\path\to\your\file.txt"
```

### Search Files
```powershell
curl "http://localhost:8080/api/v1/search?query=document&userId=user123"
```

### Get User's Files
```powershell
curl http://localhost:8080/api/v1/metadata/user/user123
```

### Download a File
```powershell
curl -o downloaded-file.txt http://localhost:8080/api/v1/files/{fileId}/download `
  -H "X-User-Id: user123"
```

### Get User Activities
```powershell
curl http://localhost:8080/api/v1/activities/user/user123
```

## Kubernetes Deployment

### 1. Start Minikube
```powershell
minikube start --memory=8192 --cpus=4
```

### 2. Build & Load Images
```powershell
# Build images
docker-compose build

# Load into minikube
minikube image load gnexdrive/api-gateway:latest
minikube image load gnexdrive/file-service:latest
minikube image load gnexdrive/metadata-service:latest
minikube image load gnexdrive/search-service:latest
minikube image load gnexdrive/activity-service:latest
```

### 3. Deploy to Kubernetes
```powershell
# Create namespace
kubectl apply -f k8s/namespace.yaml

# Deploy infrastructure
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/redis.yaml
kubectl apply -f k8s/elasticsearch.yaml
kubectl apply -f k8s/kafka.yaml
kubectl apply -f k8s/minio.yaml

# Wait for infrastructure to be ready (~2 minutes)
kubectl get pods -n gdrive -w

# Deploy application services
kubectl apply -f k8s/api-gateway.yaml
kubectl apply -f k8s/file-service.yaml
kubectl apply -f k8s/metadata-service.yaml
kubectl apply -f k8s/search-service.yaml
kubectl apply -f k8s/activity-service.yaml
```

### 4. Access Services
```powershell
# Get API Gateway URL
minikube service api-gateway -n gdrive --url
```

### 5. Monitor
```powershell
# View all pods
kubectl get pods -n gdrive

# View logs
kubectl logs -f deployment/file-service -n gdrive

# Describe pod
kubectl describe pod <pod-name> -n gdrive
```

## Troubleshooting

### Services Won't Start
1. Check if ports are available:
   ```powershell
   netstat -ano | findstr "8080 8081 8082 8083 8084"
   ```

2. Check Docker services:
   ```powershell
   docker-compose ps
   ```

3. View service logs:
   ```powershell
   docker-compose logs -f <service-name>
   ```

### Database Connection Issues
```powershell
# Check PostgreSQL
docker exec -it gdrive-postgres psql -U gdrive_user -d gdrive_metadata

# Verify tables
\dt
```

### Kafka Issues
```powershell
# Check Kafka topics
docker exec -it gdrive-kafka kafka-topics --list --bootstrap-server localhost:9092
```

### Elasticsearch Issues
```powershell
# Check Elasticsearch
curl http://localhost:9200/_cat/health

# List indices
curl http://localhost:9200/_cat/indices
```

### Redis Issues
```powershell
# Check Redis
docker exec -it gdrive-redis redis-cli ping
```

### MinIO Issues
```powershell
# Access MinIO console
# Open browser: http://localhost:9001
# Login: minioadmin / minioadmin
```

## Stopping Services

### Docker Compose
```powershell
# Stop all services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

### Kubernetes
```powershell
# Delete everything
kubectl delete namespace gdrive

# Or delete individually
kubectl delete -f k8s/
```

## Development Tips

### Hot Reload
Use Spring Boot DevTools for hot reload during development. Add to your IDE configuration.

### Debug Mode
Run services with debug enabled:
```powershell
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

### View Kafka Messages
```powershell
docker exec -it gdrive-kafka kafka-console-consumer `
  --bootstrap-server localhost:9092 `
  --topic file.uploaded `
  --from-beginning
```

### Check Elasticsearch Documents
```powershell
curl http://localhost:9200/files/_search?pretty
```

### Monitor Redis Cache
```powershell
docker exec -it gdrive-redis redis-cli monitor
```

## Building for Production

### 1. Build Production JARs
```powershell
mvn clean package -Pprod -DskipTests
```

### 2. Build Docker Images
```powershell
docker-compose build
```

### 3. Push to Registry
```powershell
docker tag gnexdrive/api-gateway:latest your-registry/api-gateway:latest
docker push your-registry/api-gateway:latest
# Repeat for all services
```

### 4. Update Kubernetes Manifests
Update image references in `k8s/*.yaml` files to point to your registry.

## Next Steps

1. **Implement Business Logic**: Fill in all `// TODO` comments with actual implementations
2. **Add Authentication**: Implement JWT token generation and validation
3. **Write Tests**: Add unit tests and integration tests
4. **Configure Logging**: Set up structured logging
5. **Add Monitoring**: Integrate Prometheus and Grafana
6. **Security Hardening**: Add HTTPS, secrets management
7. **Performance Testing**: Load test with JMeter or Gatling

## Useful Commands

```powershell
# View all running containers
docker ps

# View container logs
docker logs -f <container-name>

# Execute command in container
docker exec -it <container-name> <command>

# Check disk usage
docker system df

# Clean up unused resources
docker system prune -a

# Maven skip tests
mvn clean install -DskipTests

# Maven offline mode
mvn clean install -o

# Build specific module
mvn clean install -pl api-gateway -am

# Run tests only
mvn test

# Check for updates
mvn versions:display-dependency-updates
```

## Support & Resources

- **Architecture**: See `ARCHITECTURE.md`
- **API Documentation**: Swagger UI at `http://localhost:808X/swagger-ui.html`
- **Spring Boot**: https://spring.io/projects/spring-boot
- **Spring Cloud**: https://spring.io/projects/spring-cloud
- **Kafka**: https://kafka.apache.org/documentation/
- **Elasticsearch**: https://www.elastic.co/guide/
- **Kubernetes**: https://kubernetes.io/docs/

Happy coding! 🚀
