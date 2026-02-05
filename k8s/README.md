# Kubernetes Deployment Files

This directory contains Kubernetes manifests for deploying the Mini Google Drive system.

## Prerequisites
- Kubernetes cluster (minikube, k3s, or cloud provider)
- kubectl configured
- Docker images built and pushed to registry

## Deployment Steps

1. **Create namespace**
```bash
kubectl apply -f namespace.yaml
```

2. **Deploy infrastructure services**
```bash
kubectl apply -f postgres.yaml
kubectl apply -f redis.yaml
kubectl apply -f elasticsearch.yaml
kubectl apply -f kafka.yaml
kubectl apply -f minio.yaml
```

3. **Deploy application services**
```bash
kubectl apply -f api-gateway.yaml
kubectl apply -f file-service.yaml
kubectl apply -f metadata-service.yaml
kubectl apply -f search-service.yaml
kubectl apply -f activity-service.yaml
```

4. **Check deployment status**
```bash
kubectl get pods -n gdrive
kubectl get services -n gdrive
```

## Services

- **API Gateway**: http://localhost:8080
- **File Service**: Internal (8081)
- **Metadata Service**: Internal (8082)
- **Search Service**: Internal (8083)
- **Activity Service**: Internal (8084)

## Scaling

Scale individual services:
```bash
kubectl scale deployment file-service --replicas=3 -n gdrive
```

## Monitoring

View logs:
```bash
kubectl logs -f deployment/file-service -n gdrive
```

## Cleanup

```bash
kubectl delete namespace gdrive
```
