# GKE Deployment Guide

## Overview

This guide covers deploying the Sem5 Project Backend to Google Kubernetes Engine (GKE) using Google Cloud Load Balancer (GCLB) instead of nginx ingress. This approach provides better integration with GCP services and automatic SSL certificate management.

## Prerequisites

1. **GKE Cluster**: Running GKE cluster with at least 3 nodes
2. **gcloud CLI**: Installed and authenticated
3. **kubectl**: Configured to connect to your GKE cluster
4. **Domain**: A domain name for your application (e.g., sem5-project.com)
5. **External Databases**: DigitalOcean PostgreSQL and MongoDB cluster configured

## GKE vs Nginx Ingress Benefits

### GKE Ingress with GCLB:
- ✅ **Automatic SSL certificates** via Google-managed certificates
- ✅ **Global load balancing** with Cloud CDN integration
- ✅ **Better performance** with Google's global network
- ✅ **Integrated health checks** and monitoring
- ✅ **No additional nginx controller** needed
- ✅ **Automatic scaling** and high availability

### vs Nginx Ingress:
- ❌ Manual SSL certificate management
- ❌ Additional nginx controller pods
- ❌ Manual configuration for health checks
- ❌ Limited global load balancing

## Pre-Deployment Steps

### 1. Reserve Global Static IP

```bash
# Replace YOUR_PROJECT_ID with your actual GCP project ID
gcloud compute addresses create sem5-project-ip --global --project=YOUR_PROJECT_ID

# Get the IP address
gcloud compute addresses describe sem5-project-ip --global --format="value(address)"
```

### 2. Configure DNS

Point your domain DNS records to the reserved IP:
```
Type: A
Name: api.sem5-project.com
Value: <YOUR_STATIC_IP>

Type: A  
Name: frontend.sem5-project.com
Value: <YOUR_STATIC_IP>

Type: A
Name: admin.sem5-project.com  
Value: <YOUR_STATIC_IP>
```

### 3. Update Configuration Files

#### ConfigMap (configmap.yaml):
```yaml
data:
  # Your actual DigitalOcean PostgreSQL
  PG_DB_URL: "jdbc:postgresql://your-db-host:25060/your_database?sslmode=require"
  PG_DB_USERNAME: "your_username"
  
  # Your actual MongoDB cluster
  MEDIA_DB_URL: "mongodb+srv://username:password@cluster.mongodb.net/media_db"
```

#### Secrets (secrets.yaml):
```bash
# Encode your actual passwords
echo -n "your-actual-password" | base64
```

## Deployment Process

### Option 1: Automated Deployment

```powershell
cd k8s
.\deploy-gke.ps1
```

### Option 2: Manual Step-by-Step

```bash
# 1. Create static IP (run once)
gcloud compute addresses create sem5-project-ip --global

# 2. Apply configurations
kubectl apply -f configmap.yaml
kubectl apply -f secrets.yaml

# 3. Apply GKE-specific resources
kubectl apply -f gke-resources.yaml

# 4. Deploy infrastructure
kubectl apply -f rabbitmq.yaml

# 5. Deploy services
kubectl apply -f user-service.yaml
kubectl apply -f course-service.yaml
kubectl apply -f assessment-service.yaml
kubectl apply -f media-service.yaml
kubectl apply -f notification-service.yaml
kubectl apply -f payment-service.yaml
kubectl apply -f api-gateway.yaml

# 6. Deploy ingress
kubectl apply -f ingress.yaml
```

## Verification and Monitoring

### Check Deployment Status

```bash
# Check pods
kubectl get pods -o wide

# Check services
kubectl get services

# Check ingress status
kubectl get ingress api-ingress

# Check managed certificate status
kubectl get managedcertificate sem5-project-ssl
kubectl describe managedcertificate sem5-project-ssl

# Check backend config
kubectl get backendconfig api-backend-config
```

### Monitor Ingress Provisioning

```bash
# Watch ingress creation (can take 10-15 minutes)
kubectl get ingress api-ingress -w

# Check Google Cloud Load Balancer
gcloud compute url-maps list
gcloud compute backend-services list
gcloud compute target-https-proxies list
```

### SSL Certificate Status

```bash
# Check certificate provisioning status
kubectl describe managedcertificate sem5-project-ssl

# Certificate states:
# - Provisioning: Certificate is being created
# - Active: Certificate is ready and serving traffic
# - FailedNotVisible: Domain validation failed
```

## Accessing Your Application

### URLs (after DNS propagation):
- **API Gateway**: https://api.sem5-project.com
- **Frontend**: https://frontend.sem5-project.com
- **Admin Panel**: https://admin.sem5-project.com

### Health Checks:
- **API Health**: https://api.sem5-project.com/actuator/health
- **Service Discovery**: All services accessible via API Gateway routes

## Troubleshooting

### Common Issues:

#### 1. Ingress not getting external IP
```bash
# Check if static IP is reserved
gcloud compute addresses list --global

# Verify ingress annotation
kubectl get ingress api-ingress -o yaml | grep static-ip
```

#### 2. SSL Certificate stuck in "Provisioning"
```bash
# Check domain DNS resolution
nslookup api.sem5-project.com
dig api.sem5-project.com

# Verify domain ownership
kubectl describe managedcertificate sem5-project-ssl
```

#### 3. Backend services unhealthy
```bash
# Check pod health
kubectl get pods
kubectl logs -l app=api-gateway

# Check backend config
kubectl describe backendconfig api-backend-config
```

#### 4. CORS issues
```bash
# Verify backend config is applied
kubectl get service api-gateway -o yaml | grep backend-config

# Check CORS configuration
kubectl describe backendconfig api-backend-config
```

### Useful Commands:

```bash
# Scale deployments
kubectl scale deployment api-gateway --replicas=3

# Rolling restart
kubectl rollout restart deployment api-gateway

# Check resource usage
kubectl top pods
kubectl top nodes

# View logs
kubectl logs -f deployment/api-gateway
kubectl logs -l app=user-service --tail=100

# Port forward for debugging
kubectl port-forward service/api-gateway 8080:8080
```

## Production Considerations

### Security:
- ✅ **SSL/TLS**: Automatic Google-managed certificates
- ✅ **Network policies**: Restrict pod-to-pod communication
- ✅ **Secrets management**: Use Google Secret Manager for sensitive data
- ✅ **RBAC**: Implement proper role-based access control

### Monitoring:
- ✅ **Google Cloud Monitoring**: Automatic metrics collection
- ✅ **Cloud Logging**: Centralized log aggregation
- ✅ **Uptime checks**: Monitor application availability
- ✅ **Alerting**: Set up alerts for critical metrics

### Scaling:
- ✅ **Horizontal Pod Autoscaling**: Automatic pod scaling based on CPU/memory
- ✅ **Cluster Autoscaling**: Automatic node scaling
- ✅ **Vertical Pod Autoscaling**: Automatic resource adjustment

### Backup:
- ✅ **External databases**: Managed by DigitalOcean and MongoDB Atlas
- ✅ **Configuration backup**: Store manifests in Git
- ✅ **Disaster recovery**: Multi-region deployment capability

## Cost Optimization

1. **Use preemptible nodes** for non-critical workloads
2. **Enable cluster autoscaling** to scale down during low usage
3. **Use appropriate resource requests/limits**
4. **Enable Cloud CDN** for static content caching
5. **Monitor costs** with Google Cloud Billing alerts

## Next Steps

1. **Set up CI/CD pipeline** for automated deployments
2. **Implement monitoring and alerting**
3. **Configure backup strategies**
4. **Set up staging environment**
5. **Implement security best practices**

This GKE setup provides a production-ready, scalable, and secure deployment for your microservices architecture!