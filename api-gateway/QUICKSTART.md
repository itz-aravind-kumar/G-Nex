# API Gateway - Quick Start Guide

## 🚀 Getting Started in 5 Minutes

### Step 1: Start Redis
```powershell
docker run -d --name gdrive-redis -p 6379:6379 redis:7-alpine
```

### Step 2: Build the Project
```powershell
# From the root directory
cd "c:\Users\ARAVIND KUMAR\Desktop\G-Nex"
mvn clean install -DskipTests
```

### Step 3: Run API Gateway
```powershell
cd api-gateway
mvn spring-boot:run
```

The gateway will start on **http://localhost:8080**

### Step 4: Test the Gateway

#### Generate a Test Token
```powershell
curl -X POST "http://localhost:8080/api/v1/auth/generate-token?userId=user123&username=john&email=john@example.com"
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyMTIzIiwidXNlcklkIjoidXNlcjEyMyIsInVzZXJuYW1lIjoiam9obiIsImVtYWlsIjoiam9obkBleGFtcGxlLmNvbSIsImlhdCI6MTY0MjU4NTIwMCwiZXhwIjoxNjQyNTg4ODAwfQ.xxxxx",
  "userId": "user123",
  "username": "john",
  "email": "john@example.com",
  "type": "Bearer"
}
```

#### Check Health
```powershell
curl http://localhost:8080/health
```

#### Test Authentication
```powershell
# Save the token
$token = "paste-your-token-here"

# Try accessing a protected endpoint (will fail if downstream service not running)
curl -H "Authorization: Bearer $token" http://localhost:8080/api/v1/metadata/user/user123
```

## 🔧 Configuration

### JWT Secret (Change for Production!)
Edit `application.yml`:
```yaml
jwt:
  secret: YOUR-SUPER-SECRET-KEY-HERE-MINIMUM-256-BITS
```

### Rate Limiting
Current limits:
- **Per User**: 100 requests/minute
- **Per IP**: 200 requests/minute

## 📝 Key Features Working

✅ **JWT Authentication** - Validates Bearer tokens  
✅ **Rate Limiting** - Redis-based throttling  
✅ **Circuit Breaker** - Fault tolerance for downstream services  
✅ **Request Routing** - Routes to file/metadata/search/activity services  
✅ **CORS Support** - Cross-origin requests enabled  
✅ **Health Monitoring** - Actuator endpoints available  

## 🧪 Testing Endpoints

### Public Endpoints (No Auth Required)
- `GET /health`
- `GET /actuator/health`
- `POST /api/v1/auth/generate-token`
- `POST /api/v1/auth/validate-token`

### Protected Endpoints (JWT Required)
- `POST /api/v1/files/upload`
- `GET /api/v1/files/{fileId}/download`
- `GET /api/v1/metadata/user/{userId}`
- `GET /api/v1/search?query=...`
- `GET /api/v1/activities/user/{userId}`

## 🐛 Troubleshooting

### Redis Connection Error
```
Error: Unable to connect to Redis
Solution: Ensure Redis is running on localhost:6379
```

### JWT Validation Fails
```
Error: 401 Unauthorized
Solution: Check token format - should be "Bearer <token>"
```

### Port Already in Use
```
Error: Port 8080 already in use
Solution: Change port in application.yml or kill process using port 8080
```

## 📊 Monitoring

### Circuit Breaker Status
```powershell
curl http://localhost:8080/actuator/circuitbreakers
```

### Metrics
```powershell
curl http://localhost:8080/actuator/metrics
```

### Prometheus Endpoint
```powershell
curl http://localhost:8080/actuator/prometheus
```

## 🔐 Security Checklist for Production

- [ ] Change JWT secret to strong random value
- [ ] Remove/secure AuthController (token generation endpoint)
- [ ] Enable HTTPS/TLS
- [ ] Restrict CORS origins (not `*`)
- [ ] Adjust rate limits based on traffic
- [ ] Enable request logging
- [ ] Set up monitoring alerts
- [ ] Use environment variables for secrets

## 🎯 What's Implemented

The API Gateway now has:
- ✅ Complete JWT authentication with validation
- ✅ Rate limiting with Redis
- ✅ Circuit breaker with Resilience4j
- ✅ Request routing to all microservices
- ✅ CORS configuration
- ✅ Security configuration
- ✅ Fallback responses for circuit breaker
- ✅ Health check endpoints
- ✅ Test token generation (dev only)
- ✅ Comprehensive logging
- ✅ Actuator monitoring

## 📚 Next Steps

1. Start other microservices (file-service, metadata-service, etc.)
2. Test end-to-end flow with file upload
3. Monitor circuit breaker behavior
4. Test rate limiting with concurrent requests
5. Review logs for authentication flow

## 💡 Pro Tips

**Generate Token Programmatically:**
```powershell
# PowerShell
$response = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/v1/auth/generate-token?userId=user123&username=john&email=john@example.com"
$token = $response.token
Write-Host "Token: $token"
```

**Test with Postman:**
1. POST to generate-token endpoint
2. Copy token from response
3. Add to other requests as: `Authorization: Bearer <token>`

**Check Logs:**
```powershell
# See authentication flow
tail -f api-gateway/logs/spring.log
```

---

**Ready to build the complete system?** Start the other microservices following their respective guides!
