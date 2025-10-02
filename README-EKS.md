# Deploy Spring Boot App to AWS EKS

## Prerequisites

1. **AWS CLI configured** with your credentials
2. **eksctl installed** - [Installation Guide](https://eksctl.io/installation/)
3. **kubectl installed** - Should already be available from minikube setup
4. **Docker running** - For building and pushing images

## Step-by-Step Deployment

### 1. Verify Prerequisites

```bash
# Check AWS CLI
aws sts get-caller-identity

# Check eksctl
eksctl version

# Check kubectl
kubectl version --client

# Check Docker
docker --version
```

### 2. Create EKS Cluster

```bash
# Make scripts executable
chmod +x scripts/*.sh

# Create EKS cluster (takes 10-15 minutes)
./scripts/create-eks-cluster.sh
```

### 3. Build and Push Docker Image to ECR

```bash
# Build and push to ECR
./scripts/build-and-push-ecr.sh
```

### 4. Deploy Application to EKS

```bash
# Set environment variables for deployment
export AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
export AWS_REGION="us-east-1"

# Deploy to EKS
./scripts/deploy-to-eks.sh
```

### 5. Access Your Application

```bash
# Get LoadBalancer URL (may take a few minutes to be ready)
kubectl get service zipcode-lookup-service -n zipcode-app

# Watch for EXTERNAL-IP to be assigned
kubectl get service zipcode-lookup-service -n zipcode-app -w
```

Once you see an EXTERNAL-IP, access your app at: `http://<EXTERNAL-IP>`

### 6. Monitor and Troubleshoot

```bash
# Check pods
kubectl get pods -n zipcode-app

# Check logs
kubectl logs -f deployment/zipcode-lookup -n zipcode-app

# Describe service
kubectl describe service zipcode-lookup-service -n zipcode-app
```

### 7. Cleanup (when done)

```bash
# Delete all resources and cluster
./scripts/cleanup-eks.sh
```

## Cost Considerations

- **EKS Cluster**: ~$0.10/hour ($72/month)
- **EC2 Nodes**: 2 x t3.medium ~$0.0416/hour each
- **LoadBalancer**: ~$0.0225/hour
- **ECR Storage**: Minimal for one image

**Estimated monthly cost**: ~$100-120

## Differences from Minikube

- Uses **LoadBalancer** service instead of NodePort
- Images stored in **ECR** instead of local Docker
- **3 replicas** for high availability
- **Production-ready** with AWS managed infrastructure

## Troubleshooting

- If cluster creation fails, check IAM permissions
- If image push fails, verify ECR repository exists
- If pods don't start, check ECR image URI in deployment
- LoadBalancer may take 2-3 minutes to get external IP