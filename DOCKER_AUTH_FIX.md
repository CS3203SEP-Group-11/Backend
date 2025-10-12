# Docker Hub Authentication Fix

## Issues Identified

### 1. **Authentication Problem**
The error "denied: requested access to the resource is denied" indicates Docker Hub authentication failure.

### 2. **Image Name Format**
Using proper DockerHub username format: `username/repository:tag`

## Solutions Applied

### ✅ **Fixed Dockerfile (Simplified Single-Stage)**
```dockerfile
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw clean package -DskipTests
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/target/*.jar"]
```

### ✅ **Fixed Workflow Authentication**
Changed from:
```yaml
docker build -t levelup/api-gateway:$GITHUB_SHA
```

To:
```yaml
docker build -t ${{ secrets.DOCKERHUB_USERNAME }}/api-gateway:${{ github.sha }}
```

## Required GitHub Secrets

Make sure these secrets are set in your GitHub repository:

### 1. **DOCKERHUB_USERNAME**
- Go to GitHub repo → Settings → Secrets and variables → Actions
- Add secret: `DOCKERHUB_USERNAME`
- Value: Your DockerHub username (e.g., `yourusername`)

### 2. **DOCKERHUB_TOKEN** 
- Go to DockerHub → Account Settings → Security → New Access Token
- Create token with Read/Write permissions
- Add to GitHub secrets as: `DOCKERHUB_TOKEN`
- Value: The generated token (NOT your password)

### 3. **GCP_PROJECT_ID**
- Your Google Cloud project ID
- Add as GitHub secret: `GCP_PROJECT_ID`

### 4. **GCP_SA_KEY**
- Service account key JSON for GKE deployment
- Add as GitHub secret: `GCP_SA_KEY`

## Verification Steps

### 1. **Check DockerHub Credentials**
```bash
# Test locally
docker login
docker build -t yourusername/api-gateway:test .
docker push yourusername/api-gateway:test
```

### 2. **Verify GitHub Secrets**
- Repository → Settings → Secrets and variables → Actions
- Ensure all 4 secrets are present and valid

### 3. **Check Repository Permissions**
- DockerHub repository `yourusername/api-gateway` must exist
- Repository must be public OR you must have push permissions

## Common Issues & Solutions

### Issue: "repository does not exist"
**Solution:** Create the repository on DockerHub first:
1. Go to DockerHub → Repositories → Create Repository
2. Name: `api-gateway`
3. Visibility: Public

### Issue: "authentication required"
**Solution:** Regenerate DockerHub token:
1. DockerHub → Security → Access Tokens
2. Delete old token
3. Create new token with Read/Write/Delete permissions
4. Update GitHub secret

### Issue: "rate limit exceeded"
**Solution:** Use authenticated pulls:
```yaml
- name: Login to Docker Hub
  uses: docker/login-action@v3
  with:
    username: ${{ secrets.DOCKERHUB_USERNAME }}
    password: ${{ secrets.DOCKERHUB_TOKEN }}
```

## Testing the Fix

### 1. **Commit and Push**
```bash
git add .
git commit -m "Fix Docker Hub authentication and simplify Dockerfile"
git push origin paymentbranch
```

### 2. **Monitor Workflow**
- Go to GitHub → Actions
- Watch the workflow execution
- Check each step for success

### 3. **Verify on DockerHub**
- Check if images are pushed successfully
- Verify both tags: `latest` and commit SHA

## Alternative: Use GitHub Container Registry

If DockerHub issues persist, consider using GitHub Container Registry (ghcr.io):

```yaml
- name: Login to GitHub Container Registry
  uses: docker/login-action@v3
  with:
    registry: ghcr.io
    username: ${{ github.actor }}
    password: ${{ secrets.GITHUB_TOKEN }}

- name: Build and push
  run: |
    docker build -t ghcr.io/${{ github.repository_owner }}/api-gateway:${{ github.sha }} .
    docker push ghcr.io/${{ github.repository_owner }}/api-gateway:${{ github.sha }}
```

This uses GitHub's built-in token and doesn't require additional setup.