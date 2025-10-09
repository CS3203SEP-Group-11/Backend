# Kubernetes Deployment for Sem5 Project Backend

This directory contains Kubernetes manifests and deployment scripts for the Sem5 Project Backend microservices.

## Architecture Overview

The backend consists of the following microservices:
- **user-service** (Port 8081) - User management and authentication
- **course-service** (Port 8082) - Course management and enrollment
- **assessment-service** (Port 8083) - Assessment and quiz management
- **payment-service** (Port 8085) - Payment processing with Stripe
- **media-service** (Port 8086) - File upload and media management
- **notification-service** (Port 8087) - Email notifications
- **api-gateway** (Port 8080) - API Gateway and routing

## Infrastructure Services

- **PostgreSQL** - External database hosted on DigitalOcean
- **MongoDB** - External database hosted on MongoDB Atlas/Cluster
- **RabbitMQ** - Message broker deployed in Kubernetes cluster

## Configuration Profiles

The application supports two profiles:
- **local** - For local development with Eureka service discovery
- **k8s** - For Kubernetes deployment without Eureka (uses K8s DNS)

## Prerequisites

1. **Kubernetes Cluster** - Minikube, Docker Desktop, or cloud cluster
2. **kubectl** - Kubernetes command-line tool
3. **Docker Images** - All services need to be built and available
4. **NGINX Ingress Controller** (for ingress)

## Environment Variables

Before deployment, update the `secrets.yaml` file with your actual credentials:

### Required Secrets (base64 encoded):
- `GOOGLE_CLIENT_ID` & `GOOGLE_CLIENT_SECRET` - Google OAuth
- `AWS_ACCESS_KEY` & `AWS_SECRET_KEY` - AWS S3 credentials
- `STRIPE_SECRET_KEY` & `STRIPE_WEBHOOK_SECRET` - Stripe payment
- `SENDGRID_API_KEY` - SendGrid email service
- `CERTIFIER_API_KEY` - Certificate API
- Database and RabbitMQ passwords

### Encoding Secrets:
```bash
# Linux/Mac
echo -n "your-secret-value" | base64

# Windows PowerShell
[Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("your-secret-value"))
```

## Building Docker Images

Before deployment, build Docker images for all services:

```bash
# Example for each service
cd user-service
docker build -t user-service:latest .

cd ../course-service
docker build -t course-service:latest .

# Repeat for all services...
```

## Deployment Steps

### 1. Update Configuration
- Edit `secrets.yaml` with your actual base64-encoded credentials
- Update `configmap.yaml` if needed

### 2. Deploy to Kubernetes

#### Option A: GKE Deployment with Google Cloud Load Balancer (Recommended)
```powershell
cd k8s
.\deploy-gke.ps1
```

#### Option B: Using Minimal PowerShell Script (Generic Kubernetes)
```powershell
cd k8s
.\deploy-minimal.ps1
```

#### Option C: Using Updated PowerShell Script
```powershell
cd k8s
.\deploy.ps1
```

#### Option B: Using Bash Script (Linux/Mac)
```bash
cd k8s
chmod +x deploy.sh
./deploy.sh
```

#### Option C: Manual Deployment (External Databases)
```bash
# Apply configs
kubectl apply -f configmap.yaml
kubectl apply -f secrets.yaml

# Deploy only RabbitMQ (databases are external)
kubectl apply -f rabbitmq.yaml

# Deploy services
kubectl apply -f user-service.yaml
kubectl apply -f course-service.yaml
kubectl apply -f assessment-service.yaml
kubectl apply -f media-service.yaml
kubectl apply -f notification-service.yaml
kubectl apply -f payment-service.yaml
kubectl apply -f api-gateway.yaml

# Deploy ingress
kubectl apply -f ingress.yaml
```

### 3. Configure Ingress

#### For GKE (Recommended):
GKE Ingress with Google Cloud Load Balancer provides automatic SSL certificates and better performance:

1. Reserve a global static IP:
```bash
gcloud compute addresses create sem5-project-ip --global --project=YOUR_PROJECT_ID
```

2. Configure DNS to point to the static IP
3. Deploy using `deploy-gke.ps1`

See `GKE_DEPLOYMENT_GUIDE.md` for detailed instructions.

#### For Generic Kubernetes:
If using nginx ingress, install NGINX Ingress Controller:
```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.1/deploy/static/provider/cloud/deploy.yaml
```

Get ingress IP and update your hosts file:
```bash
kubectl get ingress api-ingress
```

Add to `/etc/hosts` (Linux/Mac) or `C:\Windows\System32\drivers\etc\hosts` (Windows):
```
<INGRESS_IP> api.sem5-project.local frontend.sem5-project.local admin.sem5-project.local
```

## Verification

Check deployment status:
```bash
# Check pods
kubectl get pods

# Check services
kubectl get services

# Check ingress
kubectl get ingress

# Check logs
kubectl logs -l app=user-service
```

## Access URLs

- **API Gateway**: http://api.sem5-project.local (or LoadBalancer IP:8080)
- **RabbitMQ Management**: kubectl port-forward svc/rabbitmq-service 15672:15672

## Local Development vs Kubernetes

### Local Development
- Use `SPRING_PROFILES_ACTIVE=local`
- Eureka service discovery enabled
- Services connect via localhost
- Environment variables from system/IDE

### Kubernetes Deployment
- Use `SPRING_PROFILES_ACTIVE=k8s`
- Eureka disabled, uses K8s DNS
- Services connect via service names
- Environment variables from ConfigMaps/Secrets

## Troubleshooting

### Common Issues:
1. **Pod CrashLoopBackOff**: Check logs with `kubectl logs <pod-name>`
2. **ImagePullBackOff**: Ensure Docker images are built and available
3. **Service not accessible**: Check service and ingress configuration
4. **Database connection issues**: Verify ConfigMap and Secret values

### Useful Commands:
```bash
# Scale deployment
kubectl scale deployment user-service --replicas=3

# Update environment variables
kubectl patch deployment user-service -p '{"spec":{"template":{"spec":{"containers":[{"name":"user-service","env":[{"name":"NEW_VAR","value":"new-value"}]}]}}}}'

# Restart deployment
kubectl rollout restart deployment user-service

# Port forward for debugging
kubectl port-forward svc/user-service 8081:8081
```

## File Structure

```
k8s/
├── configmap.yaml          # Configuration values
├── secrets.yaml            # Sensitive data (base64 encoded)
├── postgres.yaml           # PostgreSQL database
├── mongodb.yaml            # MongoDB database
├── rabbitmq.yaml           # RabbitMQ message broker
├── user-service.yaml       # User service deployment
├── course-service.yaml     # Course service deployment
├── assessment-service.yaml # Assessment service deployment
├── media-service.yaml      # Media service deployment
├── notification-service.yaml # Notification service deployment
├── payment-service.yaml    # Payment service deployment
├── api-gateway.yaml        # API Gateway deployment
├── ingress.yaml            # Ingress configuration
├── deploy.sh               # Bash deployment script
├── deploy.ps1              # PowerShell deployment script
└── README.md               # This file
```

## Security Notes

- All sensitive data is stored in Kubernetes Secrets
- Database passwords should be changed from defaults
- API keys should be properly secured
- Consider using external secret management systems for production

## Production Considerations

- Use managed databases (RDS, Cloud SQL) instead of in-cluster databases
- Implement proper monitoring and logging
- Set up autoscaling policies
- Use network policies for security
- Implement backup strategies
- Consider using service mesh (Istio) for advanced features