# External Database Configuration

## Overview

This setup assumes you're using external managed databases instead of deploying them in the Kubernetes cluster:
- **PostgreSQL**: Hosted on DigitalOcean
- **MongoDB**: Hosted on MongoDB Atlas or other cloud cluster

## Configuration Steps

### 1. Update ConfigMap with Your Database URLs

Edit `k8s/configmap.yaml` and update these values:

```yaml
data:
  # Your DigitalOcean PostgreSQL connection
  PG_DB_URL: "jdbc:postgresql://your-digitalocean-host:25060/your_database?sslmode=require"
  PG_DB_USERNAME: "your_db_user"
  
  # Your MongoDB Atlas connection string
  MEDIA_DB_URL: "mongodb+srv://username:password@your-cluster.mongodb.net/media_db?retryWrites=true&w=majority"
```

### 2. Update Secrets with Your Database Passwords

Edit `k8s/secrets.yaml` and update these base64-encoded values:

```yaml
data:
  # Base64 encoded PostgreSQL password
  PG_DB_PASSWORD: <your-base64-encoded-password>
```

To encode your password:
```powershell
# Windows PowerShell
[Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("your-actual-password"))
```

### 3. Database Connection Examples

#### DigitalOcean PostgreSQL:
```
Host: your-db-host.db.ondigitalocean.com
Port: 25060
Database: your_database_name
Username: your_username
Password: your_password
SSL Mode: require
```

Connection URL format:
```
jdbc:postgresql://your-db-host.db.ondigitalocean.com:25060/your_database_name?sslmode=require
```

#### MongoDB Atlas:
```
mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/media_db?retryWrites=true&w=majority
```

### 4. Firewall Configuration

Make sure your Kubernetes cluster can access your external databases:

#### DigitalOcean Database:
- Add your Kubernetes cluster's IP range to the database's trusted sources
- Or add `0.0.0.0/0` for testing (not recommended for production)

#### MongoDB Atlas:
- Add your Kubernetes cluster's IP range to the Network Access whitelist
- Or add `0.0.0.0/0` for testing (not recommended for production)

### 5. Deployment with External Databases

Use the minimal deployment script that excludes database deployments:

```powershell
cd k8s
.\deploy-minimal.ps1
```

This will deploy:
- ✅ RabbitMQ (for messaging)
- ✅ All application services
- ✅ API Gateway
- ✅ Ingress
- ❌ PostgreSQL (using external)
- ❌ MongoDB (using external)

### 6. Verification

After deployment, check that services can connect to external databases:

```bash
# Check service logs for database connectivity
kubectl logs -l app=user-service
kubectl logs -l app=course-service
kubectl logs -l app=media-service

# Look for successful database connections in the logs
```

### 7. Benefits of External Databases

- **Managed Backups**: Automatic backups handled by the cloud provider
- **High Availability**: Built-in redundancy and failover
- **Scalability**: Easy to scale database resources independently
- **Security**: Professional database security and monitoring
- **Maintenance**: Automated updates and maintenance
- **Cost Efficiency**: Pay only for what you use

### 8. Local Development

For local development, you can still use local databases or connect to the same external databases by setting the environment variables:

```bash
export PG_DB_URL="jdbc:postgresql://your-digitalocean-host:25060/your_database?sslmode=require"
export PG_DB_USERNAME="your_username"
export PG_DB_PASSWORD="your_password"
export MEDIA_DB_URL="mongodb+srv://username:password@your-cluster.mongodb.net/media_db"
```

This gives you the flexibility to use the same database for both local development and production deployment.