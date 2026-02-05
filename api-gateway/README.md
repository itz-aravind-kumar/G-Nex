# API Gateway Service

## Overview
The API Gateway serves as the single entry point for all client requests in the G-Nex file storage system. It provides authentication, authorization, rate limiting, and intelligent routing to downstream microservices.

## Features
- ✅ **JWT Authentication** - Stateless token-based authentication
- ✅ **Rate Limiting** - Redis-based rate limiting (100 req/min per user, 200 req/min per IP)
- ✅ **Circuit Breaker** - Resilience4j for fault tolerance
- ✅ **Request Routing** - Intelligent routing to microservices
- ✅ **CORS Support** - Cross-origin resource sharing
- ✅ **Retry Logic** - Automatic retry on failures
- ✅ **Fallback Responses** - Graceful degradation when services unavailable

## Architecture Components

### 1. JWT Authentication Filter (`JwtAuthenticationFilter`)
- Validates JWT tokens from `Authorization: Bearer <token>` header
- Extracts user information and adds to request headers (`X-User-Id`, `X-Username`, `X-User-Email`)
- Skips authentication for public paths (`/health`, `/actuator`, `/api/v1/auth`)
- Returns 401 Unauthorized for invalid/missing tokens

### 2. Rate Limiting (`RateLimitFilter`)
- **User-based**: Limits requests per authenticated user (uses `X-User-Id`)
- **IP-based**: Fallback limiting by client IP address
- **Path-based**: Per-endpoint rate limiting
- Uses Redis for distributed rate limiting

### 3. Circuit Breaker (`GatewayConfig`)
- Monitors downstream service health
- Opens circuit after 50% failure rate (min 5 calls)
- Half-open state after 30 seconds
- Provides fallback responses via `FallbackController`

### 4. Security Configuration (`SecurityConfig`)
- Disables CSRF (stateless JWT auth)
- Configures public/protected endpoints
- CORS configuration for cross-origin requests
- Disables form login and HTTP basic auth

## Configuration

### JWT Settings (`application.yml`)
```yaml
jwt:
  secret: mySecretKeyForJWTTokenGenerationAndValidation12345678
  expiration: 3600000  # 1 hour
```

### Circuit Breaker
```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        failureRateThreshold: 50
        slidingWindowSize: 10
        waitDurationInOpenState: 30s
```

### Redis
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

## Routes

| Path | Target Service | Port |
|------|---------------|------|
| `/api/v1/files/**` | file-service | 8081 |
| `/api/v1/metadata/**` | metadata-service | 8082 |
| `/api/v1/search/**` | search-service | 8083 |
| `/api/v1/activities/**` | activity-service | 8084 |

## API Endpoints

### Health Check
```bash
GET http://localhost:8080/health
```

### Generate Test Token (Development Only)
```bash
POST http://localhost:8080/api/v1/auth/generate-token?userId=user123&username=john&email=john@example.com

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "user123",
  "username": "john",
  "email": "john@example.com",
  "type": "Bearer"
}
```

### Validate Token
```bash
POST http://localhost:8080/api/v1/auth/validate-token
Content-Type: application/json

{
  "token": "your-jwt-token"
}
```

### Using the Gateway with Services
```bash
# Upload file (requires JWT token)
POST http://localhost:8080/api/v1/files/upload
Authorization: Bearer <your-jwt-token>
Content-Type: multipart/form-data

# Search files
GET http://localhost:8080/api/v1/search?query=document
Authorization: Bearer <your-jwt-token>
```

## Running the Service

### Prerequisites
- Java 17+
- Redis running on localhost:6379
- Maven 3.8+

### Start Redis
```powershell
docker run -d -p 6379:6379 redis:7-alpine
```

### Run Locally
```powershell
cd api-gateway
mvn spring-boot:run
```

### Build Docker Image
```powershell
mvn clean package
docker build -t gnexdrive/api-gateway:latest .
```

### Run with Docker
```powershell
docker run -p 8080:8080 \
  -e SPRING_DATA_REDIS_HOST=redis \
  gnexdrive/api-gateway:latest
```

## Testing

### 1. Health Check
```powershell
curl http://localhost:8080/health
```

### 2. Generate Token
```powershell
curl -X POST "http://localhost:8080/api/v1/auth/generate-token?userId=user123&username=testuser&email=test@example.com"
```

### 3. Test Protected Endpoint
```powershell
# Get token from step 2
$token = "your-generated-token"

# Call protected endpoint
curl -H "Authorization: Bearer $token" http://localhost:8080/api/v1/metadata/user/user123
```

## Security Notes

⚠️ **Important for Production:**
1. **Remove AuthController** - Token generation endpoint is for testing only
2. **Change JWT Secret** - Use environment variable with strong secret
3. **HTTPS Only** - Enable TLS/SSL in production
4. **CORS Origins** - Restrict allowed origins (not `*`)
5. **Rate Limits** - Adjust based on production traffic

## Monitoring

### Actuator Endpoints
- `/actuator/health` - Service health
- `/actuator/metrics` - Application metrics
- `/actuator/circuitbreakers` - Circuit breaker status
- `/actuator/prometheus` - Prometheus metrics

### Circuit Breaker Dashboard
```powershell
curl http://localhost:8080/actuator/circuitbreakers
```

## Troubleshooting

### Token Validation Fails
- Check JWT secret matches between services
- Verify token hasn't expired (1 hour default)
- Ensure `Authorization: Bearer <token>` format

### Circuit Breaker Opens
- Check downstream service logs
- Verify service health: `curl http://localhost:8081/actuator/health`
- Circuit auto-recovers after 30 seconds

### Rate Limiting Issues
- Check Redis connectivity
- Verify user ID in token
- Review rate limit configuration

### CORS Errors
- Update `allowedOrigins` in SecurityConfig
- Check CORS headers in response
- Verify OPTIONS preflight requests

## File Structure
```
api-gateway/
├── src/main/java/com/gnexdrive/gateway/
│   ├── ApiGatewayApplication.java       # Main application
│   ├── config/
│   │   ├── SecurityConfig.java          # Security & CORS
│   │   ├── GatewayConfig.java           # Circuit breaker
│   │   └── RedisConfig.java             # Redis setup
│   ├── filter/
│   │   ├── JwtAuthenticationFilter.java # JWT validation
│   │   └── RateLimitFilter.java         # Rate limiting
│   ├── util/
│   │   └── JwtUtil.java                 # JWT utilities
│   └── controller/
│       ├── AuthController.java          # Test auth endpoints
│       ├── HealthController.java        # Health checks
│       └── FallbackController.java      # Circuit breaker fallbacks
└── src/main/resources/
    ├── application.yml                  # Main config
    └── application-docker.yml           # Docker config
```

## Next Steps
- Implement user authentication service
- Add OAuth2 support
- Implement API key authentication
- Add request/response logging
- Set up distributed tracing
