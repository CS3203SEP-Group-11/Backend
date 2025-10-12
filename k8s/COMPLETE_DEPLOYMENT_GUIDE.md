# 🚀 Complete GKE Deployment Guide - Frontend & Backend

## 📋 **Architecture Overview**

```
Internet → GKE Ingress → {
  api.levelup-learning.com     → Backend API Gateway
  app.levelup-learning.com     → Student Frontend (React)
  admin.levelup-learning.com   → Admin Frontend (React)
}
```

## 🛠️ **Prerequisites**

1. **GKE Cluster** running with:
   - Namespace: `levelup-learning`
   - Backend services deployed
   - External databases (PostgreSQL, MongoDB, RabbitMQ)

2. **Docker Registry**: Google Container Registry (GCR)

3. **Domain Names** (configure DNS):
   - `api.levelup-learning.com`
   - `app.levelup-learning.com`
   - `admin.levelup-learning.com`

---

## 🔧 **Step 1: Build and Push Docker Images**

### **Frontend (Student App)**
```bash
cd Frontend
docker build -t gcr.io/YOUR_PROJECT_ID/levelup-frontend:latest .
docker push gcr.io/YOUR_PROJECT_ID/levelup-frontend:latest
```

### **Frontend-Admin**
```bash
cd Frontend-Admin
docker build -t gcr.io/YOUR_PROJECT_ID/levelup-admin-frontend:latest .
docker push gcr.io/YOUR_PROJECT_ID/levelup-admin-frontend:latest
```

---

## ☸️ **Step 2: Deploy to Kubernetes**

### **Deploy Frontend Services**
```bash
# Deploy student frontend
kubectl apply -f Frontend/k8s/frontend-student.yaml

# Deploy admin frontend
kubectl apply -f Frontend-Admin/k8s/frontend-admin.yaml

# Verify deployments
kubectl get pods -n levelup-learning | grep frontend
```

### **Deploy SSL Certificates**
```bash
kubectl apply -f Backend/k8s/ssl-certificates.yaml
```

### **Deploy Unified Ingress**
```bash
kubectl apply -f Backend/k8s/ingress.yaml
```

---

## 🌐 **Step 3: DNS Configuration**

Configure your domain DNS to point to the GKE ingress IP:

```bash
# Get ingress IP
kubectl get ingress levelup-ingress -n levelup-learning

# Create DNS A records:
# api.levelup-learning.com    → INGRESS_IP
# app.levelup-learning.com    → INGRESS_IP  
# admin.levelup-learning.com  → INGRESS_IP
```

---

## 🔍 **Step 4: Verify Deployment**

### **Check All Services**
```bash
kubectl get all -n levelup-learning
```

### **Check Ingress Status**
```bash
kubectl describe ingress levelup-ingress -n levelup-learning
```

### **Check SSL Certificates**
```bash
kubectl describe managedcertificate levelup-ssl-cert -n levelup-learning
```

### **Test Endpoints**
```bash
# Backend API
curl https://api.levelup-learning.com/actuator/health

# Student Frontend
curl https://app.levelup-learning.com

# Admin Frontend  
curl https://admin.levelup-learning.com
```

---

## 📊 **Resource Summary**

### **Frontend Applications:**
- **Student Frontend**: 2 replicas, 128Mi memory, 100m CPU
- **Admin Frontend**: 1 replica, 128Mi memory, 100m CPU

### **Networking:**
- **Single Ingress**: Handles all traffic routing
- **SSL Termination**: Automatic HTTPS with Google-managed certificates
- **Load Balancing**: Google Cloud Load Balancer

### **Domains:**
- `api.levelup-learning.com` → Backend microservices
- `app.levelup-learning.com` → Student React app
- `admin.levelup-learning.com` → Admin React app

---

## 🚨 **Troubleshooting**

### **Frontend Not Loading**
```bash
# Check pod status
kubectl get pods -n levelup-learning | grep frontend

# Check pod logs
kubectl logs deployment/frontend-student -n levelup-learning
kubectl logs deployment/frontend-admin -n levelup-learning
```

### **SSL Certificate Issues**
```bash
# Check certificate status
kubectl describe managedcertificate levelup-ssl-cert -n levelup-learning

# Note: SSL certificates can take 10-60 minutes to provision
```

### **Ingress Issues**
```bash
# Check ingress events
kubectl describe ingress levelup-ingress -n levelup-learning

# Check backend services
kubectl get services -n levelup-learning
```

---

## 🔄 **CI/CD Pipeline**

### **Automatic Deployment**
- **Trigger**: Push to `main` branch
- **Process**: Build → Test → Push to GCR → Deploy to GKE
- **Monitoring**: GitHub Actions provides deployment status

### **Manual Deployment**
```bash
# Update image in deployment
kubectl set image deployment/frontend-student \
  frontend-student=gcr.io/YOUR_PROJECT_ID/levelup-frontend:NEW_TAG \
  --namespace=levelup-learning

kubectl set image deployment/frontend-admin \
  frontend-admin=gcr.io/YOUR_PROJECT_ID/levelup-admin-frontend:NEW_TAG \
  --namespace=levelup-learning
```

---

## 💰 **Cost Optimization**

### **Resource Limits**
- ✅ Appropriate CPU/memory limits set
- ✅ Frontend apps use minimal resources
- ✅ Single ingress reduces load balancer costs

### **Expected Monthly Costs (GKE)**
- **Compute**: ~$150-200/month (nodes)
- **Load Balancer**: ~$18/month (single ingress)
- **SSL Certificates**: Free (Google-managed)
- **Total**: ~$170-220/month

This deployment is production-ready and cost-optimized! 🎉