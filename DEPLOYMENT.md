# Backend CI/CD and Docker Configuration

This repository contains the complete CI/CD setup for all backend microservices with Docker containerization and automated deployment to GKE.

## Services Overview

### Production Services (with Docker & CI/CD)
- **user-service** (Port 8081)
- **course-service** (Port 8082)
- **payment-service** (Port 8083)
- **assessment-service** (Port 8084)
- **notification-service** (Port 8085)
- **api-gateway** (Port 8080)
- **media-service** (Port 8086)

### Development Services (excluded from production)
- **eureka-server** - Service discovery (not needed in K8s)
- **config-server** - Configuration management (replaced by ConfigMaps)

## Docker Configuration

### Multi-stage Dockerfiles
Each service uses a standardized multi-stage Docker build:
```dockerfile
# Build stage
FROM maven:3.9.6-openjdk-21 as build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:21-jre-slim
RUN addgroup --system appgroup && adduser --system appuser --ingroup appgroup
COPY --from=build /app/target/*.jar app.jar
USER appuser
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Docker Hub Images
All services are published to Docker Hub under the `levelup/` organization:
- `levelup/user-service:latest`
- `levelup/course-service:latest`
- `levelup/payment-service:latest`
- `levelup/assessment-service:latest`
- `levelup/notification-service:latest`
- `levelup/api-gateway:latest`
- `levelup/media-service:latest`

## GitHub Actions CI/CD

### Workflow Features
Each service has an individual GitHub Actions workflow that:

1. **Triggers** on pushes to `main`/`develop` branches affecting the service
2. **Tests** the application using Maven
3. **Builds** the application JAR
4. **Containerizes** using Docker multi-stage build
5. **Publishes** to Docker Hub with SHA and `latest` tags
6. **Deploys** to GKE cluster with rolling updates

### Required GitHub Secrets
Configure these secrets in your GitHub repository:

```yaml
DOCKERHUB_USERNAME: # Your Docker Hub username
DOCKERHUB_TOKEN: # Docker Hub access token
GCP_SA_KEY: # Google Cloud service account key (JSON)
GCP_PROJECT_ID: # Your GCP project ID
```

### Service-specific Workflows
- `.github/workflows/user-service.yml`
- `.github/workflows/course-service.yml`
- `.github/workflows/payment-service.yml`
- `.github/workflows/assessment-service.yml`
- `.github/workflows/notification-service.yml`
- `.github/workflows/api-gateway.yml`
- `.github/workflows/media-service.yml`

## Spring Boot Configuration

### Environment-specific Profiles
All services use the proper Spring Boot configuration loading order:
- `application.yml` - Common configuration
- `application-dev.yml` - Development overrides
- `application-prod.yml` - Production overrides

### Production Configuration
Services communicate directly via HTTP without Eureka service discovery:
```yaml
# Example: user-service calling course-service
course-service:
  url: http://course-service:8082

# Database configuration
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:userdb}
    username: ${DB_USERNAME:user}
    password: ${DB_PASSWORD:password}
```

## Deployment Architecture

### GKE Cluster Setup
- **Cluster**: `levelup-cluster` in `us-central1-a`
- **Namespace**: `levelup-learning`
- **Ingress**: Single NGINX ingress routing to all services
- **SSL**: Managed certificates for all domains

### Domain Routing
- `api.levelup-learning.com` → api-gateway (8080)
- `app.levelup-learning.com` → student frontend
- `admin.levelup-learning.com` → admin frontend

## Local Development

### Building Services Locally
```bash
# Build individual service
cd user-service
mvn clean package

# Build Docker image
docker build -t levelup/user-service:local .

# Run locally
docker run -p 8081:8081 levelup/user-service:local
```

### Running All Services
```bash
# Using Docker Compose (create docker-compose.yml)
docker-compose up -d

# Or individual containers
docker run -d -p 8080:8080 levelup/api-gateway:latest
docker run -d -p 8081:8081 levelup/user-service:latest
docker run -d -p 8082:8082 levelup/course-service:latest
# ... etc
```

## Monitoring and Health Checks

### Health Endpoints
All services expose Spring Actuator health endpoints:
- `http://service:port/actuator/health`
- Used by Docker health checks and Kubernetes readiness probes

### Logging
Services use structured logging with correlation IDs for distributed tracing.

## Security Considerations

### Docker Security
- Non-root user execution
- Minimal base images (OpenJDK slim)
- No secrets in images
- Multi-stage builds to reduce attack surface

### Kubernetes Security
- Network policies for service isolation
- Resource limits and requests
- Security contexts for pods
- Secret management via Kubernetes secrets

## Troubleshooting

### Common Issues
1. **Build failures**: Check Maven dependencies and Java version
2. **Docker push failures**: Verify Docker Hub credentials
3. **Deployment failures**: Check GKE cluster access and image availability
4. **Health check failures**: Verify actuator endpoints are enabled

### Debugging
```bash
# Check workflow status
gh workflow list

# View logs
kubectl logs deployment/user-service -n levelup-learning

# Check pod status
kubectl get pods -n levelup-learning
```