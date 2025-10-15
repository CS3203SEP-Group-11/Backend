# CORS Configuration Strategy - Complete Guide

## ✅ **Current Setup: Spring Boot CORS Only (Recommended)**

### **Why This Approach:**

1. **Single Source of Truth**: CORS handled entirely by Spring Boot API Gateway
2. **Profile-Based**: Different origins for local vs production
3. **No Conflicts**: Eliminates GCP BackendConfig vs Spring Boot conflicts
4. **Consistent Behavior**: Same CORS logic across all environments
5. **Easier Debugging**: All CORS logic in one place

## **Configuration Breakdown**

### **1. Spring Boot SecurityConfig.java**
```java
@Value("${cors.allowed-origins}")
private List<String> allowedOrigins;

@Bean
public CorsWebFilter corsWebFilter() {
    CorsConfiguration config = new CorsConfiguration();
    
    // Profile-based origins from application properties
    allowedOrigins.forEach(config::addAllowedOrigin);
    
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);
    
    // Apply to all paths
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    
    return new CorsWebFilter(source);
}
```

### **2. Profile-Based Configuration**

#### **application-local.yml** (Local Development)
```yaml
cors:
  allowed-origins:
    - "http://localhost:3000"    # React dev server
    - "http://localhost:5173"    # Vite dev server  
    - "http://localhost:5174"    # Alternative Vite port
```

#### **application-k8s.yml** (Production)
```yaml
cors:
  allowed-origins:
    - "https://api.levelups.app"     # API domain
    - "https://app.levelups.app"     # Student frontend
    - "https://admin.levelups.app"   # Admin frontend
```

### **3. GKE BackendConfig (No CORS)**
```yaml
apiVersion: cloud.google.com/v1
kind: BackendConfig
metadata:
  name: api-backend-config
spec:
  timeoutSec: 40
  # CORS removed - handled by Spring Boot
  connectionDraining:
    drainingTimeoutSec: 60
  logging:
    enable: true
    sampleRate: 1.0
```

## **Traffic Flow & CORS Handling**

### **Local Development:**
```
Frontend (localhost:5173) → Spring Boot Gateway (localhost:8080) → Backend Services
                          ↑
                    CORS Headers Added Here
```

### **Production (GKE):**
```
Frontend (app.levelups.app) → GCLB → API Gateway → Backend Services
                                    ↑
                              CORS Headers Added Here
```

## **Why NOT Both Spring Boot + GCP CORS:**

### ❌ **Problems with Dual CORS:**

1. **Conflicting Headers**: GCP and Spring Boot both add CORS headers
2. **Override Issues**: One configuration can override the other
3. **Debugging Nightmare**: Hard to know which CORS config is active
4. **Environment Mismatch**: Different behavior between local and production

### ✅ **Benefits of Spring Boot Only:**

1. **Consistent**: Same CORS behavior everywhere
2. **Profile-Aware**: Automatically switches based on environment
3. **Application-Level**: Handles authentication-aware CORS
4. **Single Configuration**: One place to manage all CORS settings

## **Testing Your CORS Setup**

### **1. Local Development Test:**
```bash
curl -X OPTIONS http://localhost:8080/api/auth/login \
  -H "Origin: http://localhost:5173" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type,Authorization" \
  -v
```

**Expected Response:**
```
Access-Control-Allow-Origin: http://localhost:5173
Access-Control-Allow-Methods: GET,POST,PUT,DELETE,OPTIONS,PATCH
Access-Control-Allow-Headers: *
Access-Control-Allow-Credentials: true
```

### **2. Production Test:**
```bash
curl -X OPTIONS https://api.levelups.app/api/auth/login \
  -H "Origin: https://app.levelups.app" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type,Authorization" \
  -v
```

**Expected Response:**
```
Access-Control-Allow-Origin: https://app.levelups.app
Access-Control-Allow-Methods: GET,POST,PUT,DELETE,OPTIONS,PATCH
Access-Control-Allow-Headers: *
Access-Control-Allow-Credentials: true
```

## **Common CORS Issues & Solutions**

### **Issue 1: "CORS policy: No 'Access-Control-Allow-Origin' header"**
**Solution:** Check if your frontend origin is in the allowed-origins list

### **Issue 2: "CORS policy: Credentials flag is 'include'"**
**Solution:** Ensure `allowCredentials: true` in Spring Boot config

### **Issue 3: "CORS policy: Method not allowed"**
**Solution:** Add the HTTP method to `setAllowedMethods`

### **Issue 4: "Multiple CORS headers"**
**Solution:** Use only Spring Boot CORS, remove GCP CORS config

## **Security Considerations**

### ✅ **Production Best Practices:**
```yaml
cors:
  allowed-origins:
    - "https://app.levelups.app"      # Specific domains only
    - "https://admin.levelups.app"    # No wildcards
    # - "*"                          # ❌ Never use in production
```

### ✅ **Headers Configuration:**
```java
config.setAllowedHeaders(List.of(
    "Authorization",
    "Content-Type", 
    "X-Requested-With",
    "Accept"
    // Don't use "*" in production if allowCredentials: true
));
```

## **Summary**

### ✅ **Your Updated Setup:**
1. **Spring Boot handles all CORS** with profile-based origins
2. **GCP BackendConfig has no CORS** (avoids conflicts)
3. **Environment-specific origins** via application-{profile}.yml
4. **Consistent behavior** across local and production

### 🎯 **Result:**
- ✅ Local development: Works with localhost origins
- ✅ Production: Works with HTTPS domains  
- ✅ No conflicts between CORS configurations
- ✅ Easy to debug and maintain
- ✅ Secure and properly configured

This is the **recommended approach** for Spring Boot + GKE deployments! 🚀