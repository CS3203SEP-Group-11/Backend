# Health Check Endpoints Configuration

## 🏥 **Available Health Check Endpoints**

After the configuration update, your API Gateway now provides multiple health check endpoints:

### **1. Standard Health Endpoints:**
```
GET https://api.levelups.app/health
GET https://api.levelups.app/actuator/health  
GET https://api.levelups.app/
```

### **2. API-Consistent Health Endpoint:**
```
GET https://api.levelups.app/api/health
```

---

## 🎯 **When to Use Each Endpoint**

### **`/health` - Kubernetes & Infrastructure**
```yaml
# Kubernetes health checks
readinessProbe:
  httpGet:
    path: /health    # ✅ Standard health check
    port: 8080
livenessProbe:
  httpGet:
    path: /health    # ✅ Standard health check  
    port: 8080
```

**Usage:**
- ✅ Kubernetes readiness/liveness probes
- ✅ Load balancer health checks
- ✅ Infrastructure monitoring
- ✅ Docker health checks

### **`/api/health` - Frontend & API Consistency**
```javascript
// Frontend health check
const response = await fetch(`${VITE_API_BASE_URL}/health`);
// This calls: https://api.levelups.app/api/health
```

**Usage:**
- ✅ Frontend application health checks
- ✅ API monitoring from client side
- ✅ Consistent with frontend base URL
- ✅ User-facing health status

### **`/` - Root Service Info**
```bash
curl https://api.levelups.app/
```

**Response:**
```json
{
  "service": "LevelUp Learning API Gateway",
  "status": "Running", 
  "version": "1.0.0"
}
```

**Usage:**
- ✅ Service identification
- ✅ Version information
- ✅ Quick service verification

---

## 📊 **Response Examples**

### **Standard Health Check (`/health`, `/api/health`)**
```json
{
  "status": "UP",
  "service": "api-gateway",
  "timestamp": "1697234567890"
}
```

### **Root Endpoint (`/`)**
```json
{
  "service": "LevelUp Learning API Gateway",
  "status": "Running",
  "version": "1.0.0"
}
```

---

## 🧪 **Testing Health Endpoints**

### **Test 1: Standard Health Check**
```bash
curl -X GET https://api.levelups.app/health
```

### **Test 2: API Health Check (New)**
```bash
curl -X GET https://api.levelups.app/api/health
```

### **Test 3: Root Information**
```bash
curl -X GET https://api.levelups.app/
```

### **Test 4: From Frontend Context**
```javascript
// In your frontend app
const baseUrl = 'https://api.levelups.app/api';
const healthResponse = await fetch(`${baseUrl}/health`);
// Calls: https://api.levelups.app/api/health
```

---

## 🔧 **Kubernetes Configuration**

Your Kubernetes health checks should continue using the standard endpoint:

```yaml
# In api-gateway.yaml
readinessProbe:
  httpGet:
    path: /health          # ✅ Keep using standard path
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10

livenessProbe:
  httpGet:
    path: /health          # ✅ Keep using standard path
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 30
```

**Why not use `/api/health` for Kubernetes?**
- Kubernetes health checks should be simple and direct
- `/health` is the standard pattern for infrastructure
- `/api/health` is for application-level consistency

---

## 📈 **Monitoring Setup**

### **Infrastructure Monitoring:**
```yaml
# Use direct health endpoints
- /health
- /actuator/health
```

### **Application Monitoring:**
```yaml
# Use API-consistent endpoints  
- /api/health
```

### **Load Balancer Health Checks:**
```yaml
# Use simple, direct endpoints
- /health
- /
```

---

## ✅ **Summary**

Now you have flexible health check options:

1. **`/health`** - Standard for Kubernetes & infrastructure
2. **`/api/health`** - Consistent with your frontend API base URL
3. **`/`** - Service information and version
4. **`/actuator/health`** - Spring Boot actuator (if enabled)

### **Recommendation:**
- **Kubernetes**: Use `/health`  
- **Frontend**: Use `/api/health`
- **Monitoring**: Use both as needed

This gives you the best of both worlds - standard infrastructure patterns AND frontend API consistency! 🎯