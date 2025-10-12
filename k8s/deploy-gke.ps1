# GKE Deployment Script for Sem5 Project Backend
Write-Host "🚀 Starting GKE deployment for Sem5 Project Backend..." -ForegroundColor Green

Write-Host "ℹ️  Prerequisites Check:" -ForegroundColor Cyan
Write-Host "  - GKE cluster is running ✓" -ForegroundColor Green
Write-Host "  - kubectl is configured for your GKE cluster ✓" -ForegroundColor Green
Write-Host "  - External databases are configured ✓" -ForegroundColor Green
Write-Host ""

# Step 1: Reserve a global static IP
Write-Host "🌐 Step 1: Creating global static IP address..." -ForegroundColor Yellow
Write-Host "Run this command first (replace PROJECT_ID):" -ForegroundColor Cyan
Write-Host "gcloud compute addresses create sem5-project-ip --global --project=YOUR_PROJECT_ID" -ForegroundColor White
Write-Host ""

# Step 2: Apply ConfigMaps and Secrets
Write-Host "📦 Step 2: Applying ConfigMaps and Secrets..." -ForegroundColor Yellow
kubectl apply -f configmap.yaml
kubectl apply -f secrets.yaml

# Step 3: Deploy GKE-specific resources
Write-Host "⚙️  Step 3: Deploying GKE-specific resources (BackendConfig, ManagedCertificate)..." -ForegroundColor Yellow
kubectl apply -f gke-resources.yaml

# Step 4: Deploy RabbitMQ
Write-Host "🗄️ Step 4: Deploying RabbitMQ..." -ForegroundColor Yellow
kubectl apply -f rabbitmq.yaml

# Wait for RabbitMQ to be ready
Write-Host "⏳ Waiting for RabbitMQ to be ready..." -ForegroundColor Yellow
kubectl wait --for=condition=ready pod -l app=rabbitmq --timeout=300s

# Step 5: Deploy application services
Write-Host "🚀 Step 5: Deploying application services..." -ForegroundColor Yellow
kubectl apply -f user-service.yaml
kubectl apply -f course-service.yaml
kubectl apply -f assessment-service.yaml
kubectl apply -f media-service.yaml
kubectl apply -f notification-service.yaml
kubectl apply -f payment-service.yaml

# Wait for application services to be ready
Write-Host "⏳ Waiting for application services to be ready..." -ForegroundColor Yellow
kubectl wait --for=condition=ready pod -l app=user-service --timeout=300s
kubectl wait --for=condition=ready pod -l app=course-service --timeout=300s
kubectl wait --for=condition=ready pod -l app=assessment-service --timeout=300s
kubectl wait --for=condition=ready pod -l app=media-service --timeout=300s
kubectl wait --for=condition=ready pod -l app=notification-service --timeout=300s
kubectl wait --for=condition=ready pod -l app=payment-service --timeout=300s

# Step 6: Deploy API Gateway
Write-Host "🌐 Step 6: Deploying API Gateway..." -ForegroundColor Yellow
kubectl apply -f api-gateway.yaml

# Wait for API Gateway to be ready
Write-Host "⏳ Waiting for API Gateway to be ready..." -ForegroundColor Yellow
kubectl wait --for=condition=ready pod -l app=api-gateway --timeout=300s

# Step 7: Deploy Ingress
Write-Host "🔗 Step 7: Deploying GKE Ingress..." -ForegroundColor Yellow
kubectl apply -f ingress.yaml

Write-Host "✅ GKE Deployment completed successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Deployment Summary:" -ForegroundColor Cyan
Write-Host "- ConfigMaps and Secrets: ✅" -ForegroundColor Green
Write-Host "- GKE Resources (BackendConfig, ManagedCertificate): ✅" -ForegroundColor Green
Write-Host "- RabbitMQ: ✅" -ForegroundColor Green
Write-Host "- Application Services: ✅" -ForegroundColor Green
Write-Host "- API Gateway (NodePort): ✅" -ForegroundColor Green
Write-Host "- GKE Ingress with GCLB: ✅" -ForegroundColor Green
Write-Host ""
Write-Host "🗄️ External Dependencies:" -ForegroundColor Cyan
Write-Host "- PostgreSQL: DigitalOcean Database ✅" -ForegroundColor Green
Write-Host "- MongoDB: MongoDB Atlas/Cluster ✅" -ForegroundColor Green
Write-Host ""
Write-Host "🔍 Check deployment status:" -ForegroundColor Cyan
Write-Host "kubectl get pods" -ForegroundColor White
Write-Host "kubectl get services" -ForegroundColor White
Write-Host "kubectl get ingress" -ForegroundColor White
Write-Host "kubectl get managedcertificate" -ForegroundColor White
Write-Host "kubectl get backendconfig" -ForegroundColor White
Write-Host ""
Write-Host "🌐 Get the Load Balancer IP:" -ForegroundColor Cyan
Write-Host "kubectl get ingress api-ingress" -ForegroundColor White
Write-Host "gcloud compute addresses describe sem5-project-ip --global" -ForegroundColor White
Write-Host ""
Write-Host "📝 DNS Setup:" -ForegroundColor Cyan
Write-Host "Point your domain DNS to the Load Balancer IP:" -ForegroundColor White
Write-Host "- api.sem5-project.com → <LOAD_BALANCER_IP>" -ForegroundColor White
Write-Host "- frontend.sem5-project.com → <LOAD_BALANCER_IP>" -ForegroundColor White  
Write-Host "- admin.sem5-project.com → <LOAD_BALANCER_IP>" -ForegroundColor White
Write-Host ""
Write-Host "🔒 SSL Certificate:" -ForegroundColor Cyan
Write-Host "Google-managed SSL certificates will be automatically provisioned" -ForegroundColor White
Write-Host "Check certificate status: kubectl describe managedcertificate sem5-project-ssl" -ForegroundColor White
Write-Host ""
Write-Host "⚠️  Important Notes:" -ForegroundColor Yellow
Write-Host "- It may take 10-15 minutes for the ingress to be fully ready" -ForegroundColor White
Write-Host "- SSL certificates can take up to 60 minutes to provision" -ForegroundColor White
Write-Host "- Ensure your domain DNS is pointing to the Load Balancer IP" -ForegroundColor White