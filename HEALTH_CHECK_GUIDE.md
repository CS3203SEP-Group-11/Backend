# Health Check Configuration Guide

## Issue
Your Spring Boot applications don't have `/actuator/health` endpoint, causing health check failures in Kubernetes.

## Two Solutions

### 🔧 Solution 1: Add Spring Boot Actuator (Recommended)

**Benefits:**
- ✅ Proper health monitoring
- ✅ Application insights and metrics
- ✅ Production-ready monitoring
- ✅ Integration with GKE monitoring

**Steps:**
1. **Add Actuator dependency to pom.xml:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

2. **Configure actuator in application.yml:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: when-authorized
```

3. **Test the endpoint:**
```bash
curl http://localhost:8080/actuator/health
# Should return: {"status":"UP"}
```

### 🔧 Solution 2: Remove Health Checks (Alternative)

**If you don't want to add Actuator:**

1. **Remove readinessProbe and livenessProbe from Kubernetes deployments**
2. **Remove health check from BackendConfig**
3. **Kubernetes will use default TCP checks**

**Drawbacks:**
- ❌ No application-level health monitoring
- ❌ Limited insight into application status
- ❌ Pods may appear healthy when application is failing

## What I've Done

### ✅ Added Actuator to API Gateway:
- Added dependency to `api-gateway/pom.xml`
- Added configuration to `application.yml`
- Health endpoint will be available at `/actuator/health`

### ✅ Commented Out Health Checks (as backup):
- Commented out readiness/liveness probes in `api-gateway.yaml`
- Commented out health check in `gke-resources.yaml`
- You can uncomment after adding actuator to all services

### ✅ Fixed Service Type:
- Changed from ClusterIP to NodePort (required for GKE Ingress)

## Next Steps

### For All Services:
You should add Spring Boot Actuator to all your microservices:

1. **Add to each service's pom.xml:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

2. **Add to each service's application.yml:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: when-authorized
```

3. **Services to update:**
   - user-service
   - course-service
   - payment-service
   - notification-service
   - media-service
   - assessment-service

### Alternative Health Check Endpoints

If you have custom health endpoints, you can modify the health check path:

```yaml
readinessProbe:
  httpGet:
    path: /health  # or /api/health or any custom endpoint
    port: 8080
```

## Testing

### With Actuator:
```bash
# Test locally
curl http://localhost:8080/actuator/health

# Test in Kubernetes
kubectl port-forward svc/api-gateway 8080:8080
curl http://localhost:8080/actuator/health
```

### Without Actuator:
- Kubernetes will use TCP socket checks
- Pods will be considered ready if they accept connections on port 8080

## Recommendation

**Use Solution 1 (Add Actuator)** because:
- Better monitoring and observability
- Production-ready approach
- Integration with cloud monitoring tools
- Proper application health validation