# Kubernetes Deployment Script for Sem5 Project Backend (PowerShell)
Write-Host "🚀 Starting Kubernetes deployment for Sem5 Project Backend..." -ForegroundColor Green

# Apply ConfigMaps and Secrets first
Write-Host "📦 Applying ConfigMaps and Secrets..." -ForegroundColor Yellow
kubectl apply -f configmap.yaml
kubectl apply -f secrets.yaml

# Deploy infrastructure services (only RabbitMQ - databases are external)
Write-Host "🗄️ Deploying infrastructure services..." -ForegroundColor Yellow
kubectl apply -f rabbitmq.yaml

# Wait for infrastructure to be ready
Write-Host "⏳ Waiting for infrastructure services to be ready..." -ForegroundColor Yellow
kubectl wait --for=condition=ready pod -l app=rabbitmq --timeout=300s

# Deploy application services
Write-Host "🚀 Deploying application services..." -ForegroundColor Yellow
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

# Deploy API Gateway
Write-Host "🌐 Deploying API Gateway..." -ForegroundColor Yellow
kubectl apply -f api-gateway.yaml

# Wait for API Gateway to be ready
Write-Host "⏳ Waiting for API Gateway to be ready..." -ForegroundColor Yellow
kubectl wait --for=condition=ready pod -l app=api-gateway --timeout=300s

# Deploy Ingress
Write-Host "🔗 Deploying Ingress..." -ForegroundColor Yellow
kubectl apply -f ingress.yaml

Write-Host "✅ Deployment completed successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Deployment Summary:" -ForegroundColor Cyan
Write-Host "- ConfigMaps and Secrets: ✅" -ForegroundColor Green
Write-Host "- Infrastructure (RabbitMQ only - databases are external): ✅" -ForegroundColor Green
Write-Host "- Application Services: ✅" -ForegroundColor Green
Write-Host "- API Gateway: ✅" -ForegroundColor Green
Write-Host "- Ingress: ✅" -ForegroundColor Green
Write-Host ""
Write-Host "🔍 Check deployment status:" -ForegroundColor Cyan
Write-Host "kubectl get pods" -ForegroundColor White
Write-Host "kubectl get services" -ForegroundColor White
Write-Host "kubectl get ingress" -ForegroundColor White
Write-Host ""
Write-Host "🌐 Access URLs (after setting up hosts file):" -ForegroundColor Cyan
Write-Host "- API: http://api.sem5-project.local" -ForegroundColor White
Write-Host "- Frontend: http://frontend.sem5-project.local" -ForegroundColor White
Write-Host "- Admin: http://admin.sem5-project.local" -ForegroundColor White
Write-Host ""
Write-Host "📝 Add to C:\Windows\System32\drivers\etc\hosts (replace <INGRESS_IP> with actual IP):" -ForegroundColor Cyan
Write-Host "<INGRESS_IP> api.sem5-project.local frontend.sem5-project.local admin.sem5-project.local" -ForegroundColor White