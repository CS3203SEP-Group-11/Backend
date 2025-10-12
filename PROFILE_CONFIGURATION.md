# Profile-Based Configuration Implementation

## Overview

I've successfully implemented a comprehensive profile-based configuration system for your microservices architecture that supports both local development and Kubernetes deployment with the requested "application.yml, application-local.yml, application-k8s.yml" approach.

## Configuration Structure

### Base Configuration (application.yml)
- Contains common application settings shared across all environments
- Includes application name, server port, JPA settings
- No environment-specific configurations

### Local Development Profile (application-local.yml)
- Uses environment variables (your provided list)
- Eureka service discovery enabled
- Services communicate via localhost
- RabbitMQ with environment variable credentials

### Kubernetes Profile (application-k8s.yml)
- Eureka disabled (uses Kubernetes DNS for service discovery)
- Services communicate via Kubernetes service names
- Configuration pulled from ConfigMaps and Secrets
- Hardcoded service URLs for inter-service communication

## Environment Variables Mapping

### Local Development Environment Variables:
```bash
# Database
PG_DB_URL=jdbc:postgresql://localhost:5432/sem5_project
PG_DB_USERNAME=your_db_user
PG_DB_PASSWORD=your_db_password

# MongoDB
MEDIA_DB_URL=mongodb://localhost:27017/media_db

# RabbitMQ
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest

# Google OAuth
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# AWS S3
AWS_ACCESS_KEY=your_aws_access_key
AWS_SECRET_KEY=your_aws_secret_key
AWS_REGION=us-east-1
AWS_S3_BUCKET_NAME=your_bucket_name

# Stripe
STRIPE_SECRET_KEY=your_stripe_secret_key
STRIPE_WEBHOOK_SECRET=your_webhook_secret

# SendGrid
SENDGRID_API_KEY=your_sendgrid_api_key

# Certifier
CERTIFIER_ISSUE_URL=your_certifier_issue_url
CERTIFIER_VALIDATE_URL=your_certifier_validate_url
CERTIFIER_API_KEY=your_certifier_api_key
CERTIFIER_GROUP_ID=your_group_id
CERTIFIER_VERSION=v1
```

## Service Configuration Changes

### All Services Updated:
1. **user-service** - Google OAuth, JWT, database, RabbitMQ
2. **course-service** - Database, RabbitMQ, Certifier API
3. **payment-service** - Database, RabbitMQ, Stripe, service URLs
4. **notification-service** - Database, RabbitMQ, SendGrid
5. **media-service** - MongoDB, AWS S3
6. **assessment-service** - Database only
7. **api-gateway** - JWT secret, service routing
8. **eureka-server** - Local development only
9. **config-server** - Not used in this architecture

## Kubernetes Deployment

### ConfigMaps and Secrets:
- **ConfigMap**: Non-sensitive configuration values
- **Secrets**: Sensitive credentials (base64 encoded)
- **Environment Variables**: Injected into containers from ConfigMaps/Secrets

### Service Discovery:
- **Local**: Eureka Server for service registration and discovery
- **Kubernetes**: Native DNS-based service discovery (no Eureka needed)

## Usage Instructions

### Local Development:
```bash
# Set environment variables in your IDE or shell
export SPRING_PROFILES_ACTIVE=local
export PG_DB_URL=jdbc:postgresql://localhost:5432/sem5_project
# ... other environment variables

# Run Eureka Server first
cd eureka-server
./mvnw spring-boot:run

# Run other services
cd user-service
./mvnw spring-boot:run -Dspring.profiles.active=local
```

### Kubernetes Deployment:
```bash
# Build Docker images
docker build -t user-service:latest ./user-service
# ... build all services

# Deploy to Kubernetes
cd k8s
kubectl apply -f configmap.yaml
kubectl apply -f secrets.yaml
./deploy.ps1  # or ./deploy.sh
```

## Benefits of This Approach

1. **Environment Separation**: Clear separation between local and K8s configs
2. **Security**: Sensitive data in Kubernetes Secrets
3. **Flexibility**: Easy to add new environments (staging, prod)
4. **Maintainability**: Common settings in base application.yml
5. **Service Discovery**: Appropriate discovery mechanism for each environment

## Service Communication

### Local Development:
- Services register with Eureka
- Discovery via Eureka client
- Communication via localhost URLs

### Kubernetes:
- No Eureka dependency
- Discovery via Kubernetes DNS
- Communication via service names (e.g., `http://user-service:8081`)

## Files Created/Modified

### Configuration Files:
- All services now have `application.yml`, `application-local.yml`, `application-k8s.yml`

### Kubernetes Manifests:
- ConfigMaps and Secrets for configuration
- Deployments for all services
- Services for networking
- PersistentVolumeClaims for data storage
- Ingress for external access

### Deployment Scripts:
- `deploy.ps1` (PowerShell)
- `deploy.sh` (Bash)
- Comprehensive README with instructions

This implementation gives you the flexibility to run locally with environment variables and Eureka, while deploying to Kubernetes with proper ConfigMaps, Secrets, and native service discovery.