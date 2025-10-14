#!/bin/bash

# Kubernetes Deployment Script for Sem5 Project Backend
echo "🚀 Starting Kubernetes deployment for Sem5 Project Backend..."

# Apply ConfigMaps and Secrets first
echo "📦 Applying ConfigMaps and Secrets..."
kubectl apply -f configmap.yaml
kubectl apply -f secrets.yaml

# Deploy infrastructure services (only RabbitMQ - databases are external)
echo "🗄️ Deploying infrastructure services..."
kubectl apply -f rabbitmq.yaml

# Wait for infrastructure to be ready
echo "⏳ Waiting for infrastructure services to be ready..."
kubectl wait --for=condition=ready pod -l app=rabbitmq --timeout=300s

# Deploy application services
echo "🚀 Deploying application services..."
kubectl apply -f user-service.yaml
kubectl apply -f course-service.yaml
kubectl apply -f assessment-service.yaml
kubectl apply -f media-service.yaml
kubectl apply -f notification-service.yaml
kubectl apply -f payment-service.yaml

# Wait for application services to be ready
echo "⏳ Waiting for application services to be ready..."
kubectl wait --for=condition=ready pod -l app=user-service --timeout=300s
kubectl wait --for=condition=ready pod -l app=course-service --timeout=300s
kubectl wait --for=condition=ready pod -l app=assessment-service --timeout=300s
kubectl wait --for=condition=ready pod -l app=media-service --timeout=300s
kubectl wait --for=condition=ready pod -l app=notification-service --timeout=300s
kubectl wait --for=condition=ready pod -l app=payment-service --timeout=300s

# Deploy API Gateway
echo "🌐 Deploying API Gateway..."
kubectl apply -f api-gateway.yaml

# Wait for API Gateway to be ready
echo "⏳ Waiting for API Gateway to be ready..."
kubectl wait --for=condition=ready pod -l app=api-gateway --timeout=300s

# Deploy Ingress
echo "🔗 Deploying Ingress..."
kubectl apply -f ingress.yaml

echo "✅ Deployment completed successfully!"
echo ""
echo "📋 Deployment Summary:"
echo "- ConfigMaps and Secrets: ✅"
echo "- Infrastructure (RabbitMQ only - databases are external): ✅"
echo "- Application Services: ✅"
echo "- API Gateway: ✅"
echo "- Ingress: ✅"
echo ""
echo "🔍 Check deployment status:"
echo "kubectl get pods"
echo "kubectl get services"
echo "kubectl get ingress"
echo ""
echo "🌐 Access URLs (after setting up /etc/hosts):"
echo "- API: http://api.sem5-project.local"
echo "- Frontend: http://frontend.sem5-project.local"
echo "- Admin: http://admin.sem5-project.local"
echo ""
echo "📝 Add to /etc/hosts (replace <INGRESS_IP> with actual IP):"
echo "<INGRESS_IP> api.sem5-project.local frontend.sem5-project.local admin.sem5-project.local"