# Docker Image Naming for Your Project

## Correct Format
For DockerHub, the image name should be: `username/repository:tag`

### Your Configuration:
- **DockerHub Username**: `heshanheshan`
- **Repository Name**: `api-gateway`
- **Tag**: `latest` or specific version

### Correct Image Names:
```yaml
# ✅ Correct format
image: heshanheshan/api-gateway:latest
image: heshanheshan/course-service:latest
image: heshanheshan/user-service:latest
image: heshanheshan/payment-service:latest
```

### Incorrect Formats:
```yaml
# ❌ Missing username
image: api-gateway:latest

# ❌ Wrong username
image: levelup/api-gateway:latest

# ❌ Too many path segments
image: heshanheshan/levelup/api-gateway:latest
```

## Building and Pushing Commands

### Local Development:
```bash
# Build
docker build -t heshanheshan/api-gateway:latest .

# Push
docker push heshanheshan/api-gateway:latest
```

### GitHub Actions Workflow:
```yaml
# Build
docker build -t ${{ secrets.DOCKERHUB_USERNAME }}/api-gateway:${{ github.sha }} .

# Push
docker push ${{ secrets.DOCKERHUB_USERNAME }}/api-gateway:${{ github.sha }}
```

## DockerHub Repository Setup

1. **Go to DockerHub**: https://hub.docker.com
2. **Create Repository**: 
   - Click "Create Repository"
   - Name: `api-gateway` (not `levelup/api-gateway`)
   - Visibility: Public
3. **Repeat for each service**:
   - `heshanheshan/api-gateway`
   - `heshanheshan/user-service`
   - `heshanheshan/course-service`
   - `heshanheshan/payment-service`
   - `heshanheshan/notification-service`
   - `heshanheshan/media-service`
   - `heshanheshan/assessment-service`

## Update All Kubernetes Manifests

You'll need to update all K8s deployment files to use the correct image names:

```yaml
# In each service YAML file
containers:
- name: service-name
  image: heshanheshan/service-name:latest
```

## GitHub Secrets Required

Make sure these are set in GitHub repository secrets:
- `DOCKERHUB_USERNAME`: `heshanheshan`
- `DOCKERHUB_TOKEN`: Your DockerHub access token
- Other secrets as needed for GCP deployment