# Alternative API Gateway Service Configuration

## Option 1: NodePort (Current - Recommended)
```yaml
apiVersion: v1
kind: Service
metadata:
  name: api-gateway
  namespace: default
  annotations:
    cloud.google.com/backend-config: '{"default": "api-backend-config"}'
spec:
  selector:
    app: api-gateway
  ports:
  - port: 8080
    targetPort: 8080
    protocol: TCP
  type: NodePort
```

**Benefits:**
- ✅ Works with GKE Ingress
- ✅ Cost-effective (no extra load balancer)
- ✅ Single entry point through ingress
- ✅ SSL termination at ingress level

## Option 2: LoadBalancer (Alternative)
```yaml
apiVersion: v1
kind: Service
metadata:
  name: api-gateway
  namespace: default
  annotations:
    cloud.google.com/backend-config: '{"default": "api-backend-config"}'
spec:
  selector:
    app: api-gateway
  ports:
  - port: 8080
    targetPort: 8080
    protocol: TCP
  type: LoadBalancer
```

**Issues:**
- ❌ Creates separate Google Cloud Load Balancer
- ❌ Additional cost for the extra load balancer
- ❌ Two load balancers (ingress + service LB)
- ❌ More complex traffic routing

## Option 3: ClusterIP (Won't Work with GKE Ingress)
```yaml
apiVersion: v1
kind: Service
metadata:
  name: api-gateway
  namespace: default
spec:
  selector:
    app: api-gateway
  ports:
  - port: 8080
    targetPort: 8080
    protocol: TCP
  type: ClusterIP
```

**Result:**
- ❌ GKE Ingress cannot reach the service
- ❌ 502 Bad Gateway errors
- ❌ Service only accessible within cluster

## Recommendation: Keep NodePort

For GKE Ingress deployment, **NodePort is the correct and recommended choice** because:

1. **Cost Efficient**: No extra load balancer costs
2. **Proper Architecture**: Single entry point through ingress
3. **SSL Termination**: Handled at ingress level with managed certificates
4. **Traffic Flow**: Internet → GKE Ingress → NodePort → Pods