# CORS Configuration Fix - Frontend API Calls

## 🔍 **Problem Identified**

Your frontend calls:
```
VITE_API_BASE_URL=https://api.levelups.app/api
```

But CORS origins were incorrectly including the API domain itself.

## ❌ **Before (Incorrect)**

### **CORS Origins included API domain:**
```yaml
CORS_ALLOWED_ORIGINS: "https://api.levelups.app,https://app.levelups.app,https://admin.levelups.app"
```

### **This was wrong because:**
- **API domain** (`api.levelups.app`) should NOT be in CORS origins
- **CORS origins** should only include **frontend domains** that call the API
- **API calling itself** doesn't need CORS validation

## ✅ **After (Correct)**

### **CORS Origins now only include frontend domains:**
```yaml
CORS_ALLOWED_ORIGINS: "https://app.levelups.app,https://admin.levelups.app"
```

---

## 🎯 **How CORS Works**

### **Traffic Flow:**
```
Frontend Domain    →    API Domain    →    Response
(Origin Header)         (CORS Check)       (Access-Control-Allow-Origin)

app.levelups.app   →   api.levelups.app   →   Access-Control-Allow-Origin: https://app.levelups.app
admin.levelups.app →   api.levelups.app   →   Access-Control-Allow-Origin: https://admin.levelups.app
```

### **CORS Headers in Browser:**
```http
# Request from https://app.levelups.app
GET https://api.levelups.app/api/courses
Origin: https://app.levelups.app

# Response from API Gateway
HTTP/1.1 200 OK
Access-Control-Allow-Origin: https://app.levelups.app
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH
Access-Control-Allow-Headers: *
Access-Control-Allow-Credentials: true
```

---

## 📝 **Updated Configuration**

### **1. application-k8s.yml (Production)**
```yaml
# CORS Configuration for Production - Frontend domains that call the API
cors:
  allowed-origins: "${CORS_ALLOWED_ORIGINS:https://app.levelups.app,https://admin.levelups.app}"
```

### **2. configmap.yaml (Kubernetes)**
```yaml
# CORS Configuration for API Gateway - Frontend domains that call the API
CORS_ALLOWED_ORIGINS: "https://app.levelups.app,https://admin.levelups.app"
```

### **3. SecurityConfig.java (Spring Boot)**
```java
// Default includes common local development ports - NOT the API domain itself
@Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
private String allowedOriginsString;
```

### **4. application-local.yml (Local Development)**
```yaml
# CORS Configuration for Local Development
cors:
  allowed-origins: "http://localhost:3000,http://localhost:5173,http://localhost:5174"
```

---

## 🚀 **Deployment Commands**

### **1. Update ConfigMap:**
```bash
kubectl apply -f k8s/configmap.yaml
```

### **2. Update API Gateway:**
```bash
kubectl apply -f k8s/api-gateway.yaml
```

### **3. Restart API Gateway (to pick up new config):**
```bash
kubectl rollout restart deployment/api-gateway -n levelup-learning
```

### **4. Verify deployment:**
```bash
# Check pod status
kubectl get pods -n levelup-learning

# Check logs
kubectl logs -f deployment/api-gateway -n levelup-learning

# Check config
kubectl get configmap app-config -n levelup-learning -o yaml
```

---

## 🧪 **Testing CORS**

### **Test 1: Preflight Request (OPTIONS)**
```bash
curl -X OPTIONS https://api.levelups.app/api/auth/login \
  -H "Origin: https://app.levelups.app" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type,Authorization" \
  -v
```

**Expected Response:**
```http
Access-Control-Allow-Origin: https://app.levelups.app
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH
Access-Control-Allow-Headers: *
Access-Control-Allow-Credentials: true
```

### **Test 2: Actual Request**
```bash
curl -X GET https://api.levelups.app/api/courses \
  -H "Origin: https://app.levelups.app" \
  -v
```

**Expected Response:**
```http
Access-Control-Allow-Origin: https://app.levelups.app
Content-Type: application/json
[...course data...]
```

### **Test 3: Blocked Request (Wrong Origin)**
```bash
curl -X GET https://api.levelups.app/api/courses \
  -H "Origin: https://malicious-site.com" \
  -v
```

**Expected Response:**
```http
# No Access-Control-Allow-Origin header = CORS blocked
```

---

## 📊 **Valid vs Invalid Origins**

### **✅ Valid Origins (Will Work)**
- `https://app.levelups.app` (Student frontend)
- `https://admin.levelups.app` (Admin frontend)
- `http://localhost:5173` (Local development - Vite)
- `http://localhost:3000` (Local development - React)

### **❌ Invalid Origins (Will Be Blocked)**
- `https://api.levelups.app` (API domain itself - not needed)
- `https://malicious-site.com` (Unauthorized domain)
- `http://app.levelups.app` (Wrong protocol - HTTP vs HTTPS)
- `https://test.levelups.app` (Not in allowed list)

---

## 🔧 **Frontend Configuration**

### **Your Frontend Environment Variables:**
```env
# Production
VITE_API_BASE_URL=https://api.levelups.app/api

# Local Development
VITE_API_BASE_URL=http://localhost:8080/api
```

### **Frontend Domains:**
```
Production Frontend:  https://app.levelups.app       → API: https://api.levelups.app/api
Admin Frontend:       https://admin.levelups.app     → API: https://api.levelups.app/api
Local Frontend:       http://localhost:5173          → API: http://localhost:8080/api
```

---

## ✅ **Result**

After these changes:

1. **✅ Frontend apps can call API** - CORS allows your frontend domains
2. **✅ Security maintained** - Only authorized domains can access API
3. **✅ No CORS errors** - Proper origin validation
4. **✅ Development works** - Local development included
5. **✅ Production ready** - Production domains configured

Your CORS configuration now correctly allows your frontend applications to call the API while maintaining security! 🎉

---

## 🎯 **Key Takeaway**

**CORS Origins = Frontend domains that need to call your API**
- ✅ Include: Where your frontend is hosted
- ❌ Don't include: Where your API is hosted

The API domain (`api.levelups.app`) should NEVER be in CORS origins!