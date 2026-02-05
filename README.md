# Mini Google Drive - Microservices Backend

A production-grade, scalable file storage backend system inspired by Google Drive, designed to demonstrate system design concepts for technical interviews.

## 🏗️ Architecture Overview

```
Client → API Gateway → Microservices → Kafka → Databases/Elasticsearch/Redis → Object Storage
```

### Microservices:
- **API Gateway**: Routing, authentication, rate limiting
- **File Service**: File upload/download, object storage integration
- **Metadata Service**: File metadata management (PostgreSQL)
- **Search Service**: Fast file search (Elasticsearch + Redis cache)
- **Activity Service**: Async activity tracking via Kafka

## 🚀 Technology Stack

- **Framework**: Spring Boot 3.x, Spring Cloud
- **Message Queue**: Apache Kafka
- **Cache**: Redis
- **Search**: Elasticsearch
- **Database**: PostgreSQL
- **Object Storage**: MinIO/S3
- **Containerization**: Docker
- **Orchestration**: Kubernetes
- **Build Tool**: Maven

## 📁 Project Structure

```
mini-google-drive/
├── api-gateway/           # API Gateway Service
├── file-service/          # File Upload/Download Service
├── metadata-service/      # Metadata Management Service
├── search-service/        # Search & Indexing Service
├── activity-service/      # Activity Tracking Service
├── common-lib/           # Shared libraries and utilities
├── docker-compose.yml    # Local development setup
└── k8s/                  # Kubernetes deployment files
```

## 🛠️ Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Kubernetes (minikube/k3s for local)

### Local Development Setup

1. **Start Infrastructure Services**
```bash
docker-compose up -d
```

2. **Build All Services**
```bash
mvn clean install
```

3. **Run Services**
```bash
# Terminal 1 - API Gateway
cd api-gateway && mvn spring-boot:run

# Terminal 2 - File Service
cd file-service && mvn spring-boot:run

# Terminal 3 - Metadata Service
cd metadata-service && mvn spring-boot:run

# Terminal 4 - Search Service
cd search-service && mvn spring-boot:run

# Terminal 5 - Activity Service
cd activity-service && mvn spring-boot:run
```

## 🔌 API Endpoints

### File Operations
- `POST /api/v1/files/upload` - Upload file
- `GET /api/v1/files/{fileId}/download` - Download file
- `DELETE /api/v1/files/{fileId}` - Delete file

### Search
- `GET /api/v1/search?query={query}&type={type}` - Search files

### Metadata
- `GET /api/v1/metadata/{fileId}` - Get file metadata
- `GET /api/v1/metadata/user/{userId}` - Get user's files

### Activity
- `GET /api/v1/activities/user/{userId}` - Get user activities

## 🐳 Docker Deployment

```bash
# Build all images
docker-compose build

# Run all services
docker-compose up
```

## ☸️ Kubernetes Deployment

```bash
# Apply all configurations
kubectl apply -f k8s/

# Check status
kubectl get pods
kubectl get services
```

## 🎯 System Design Concepts Demonstrated

1. **Microservices Architecture**: Independent, scalable services
2. **Event-Driven Design**: Kafka for async processing
3. **Caching Strategy**: Redis for performance
4. **Search Optimization**: Elasticsearch for fast queries
5. **API Gateway Pattern**: Centralized routing and security
6. **Horizontal Scaling**: Kubernetes for auto-scaling
7. **Separation of Concerns**: Clear service boundaries

## 📊 File Upload Flow

1. Client uploads file via API Gateway
2. File Service stores file in object storage (MinIO/S3)
3. File Service publishes event to Kafka
4. Metadata Service saves metadata to PostgreSQL
5. Search Service indexes file in Elasticsearch
6. Activity Service logs the upload activity

## 🔐 Security Features

- JWT-based authentication
- Rate limiting
- CORS configuration
- API key validation

## 📈 Monitoring & Observability

- Spring Boot Actuator endpoints
- Prometheus metrics
- Centralized logging
- Health checks

## 🧪 Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify
```

## 📝 Interview Preparation

This project covers these common system design questions:
- Design Google Drive
- Design File Upload System
- Design Event-Driven Architecture
- Design Distributed System
- Design Scalable Backend

## 👥 Contributing

This is a learning project. Feel free to fork and experiment!

## 📄 License

MIT License
