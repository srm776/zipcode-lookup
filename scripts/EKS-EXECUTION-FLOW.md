# EKS Deployment - Step-by-Step Execution Flow

This document provides the exact sequence of commands to deploy your Spring Boot application to AWS EKS and clean it up afterward.

---

## Prerequisites Check

Before starting, verify you have the required tools:

```bash
# Check AWS CLI and credentials
aws sts get-caller-identity

# Check eksctl
eksctl version

# Check kubectl
kubectl version --client

# Check Docker
docker --version
```

---

## Step 1: Create EKS Cluster

### Execute Script
```bash
# Make script executable
chmod +x scripts/create-eks-cluster.sh

# Create EKS cluster (takes 10-15 minutes)
./scripts/create-eks-cluster.sh
```

### What This Does
- Creates EKS control plane
- Sets up VPC, subnets, security groups
- Creates managed node group with 2 t3.medium instances
- Configures IAM roles and policies

### Monitor Progress (Optional)
```bash
# In another terminal window
watch -n 30 "eksctl get cluster --name zipcode-lookup-cluster --region us-east-1"

# Or manual check
eksctl get cluster --name zipcode-lookup-cluster --region us-east-1
```

### Expected Output
```
NAME                    REGION          EKSCTL CREATED
zipcode-lookup-cluster  us-east-1       True
```

---

## Step 2: Build and Push to ECR

### Execute Script
```bash
# Make script executable
chmod +x scripts/build-and-push-ecr.sh

# Build Docker image and push to ECR
./scripts/build-and-push-ecr.sh
```

### What This Does
- Creates ECR repository
- Authenticates Docker with ECR
- Builds Docker image locally
- Tags image for ECR
- Pushes image to AWS ECR

### Expected Output
```
AWS Account ID: 783764574351
Region: us-east-1
Repository: zipcode-lookup
Creating ECR repository...
Logging into ECR...
Building Docker image...
Pushing image to ECR...
Image pushed successfully!
ECR URI: 783764574351.dkr.ecr.us-east-1.amazonaws.com/zipcode-lookup:latest
```

---

## Step 3: Set Environment Variables

### Required Exports
```bash
# Set environment variables for deployment
export AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
export AWS_REGION="us-east-1"

# Verify variables are set
echo "AWS Account ID: $AWS_ACCOUNT_ID"
echo "AWS Region: $AWS_REGION"
```

### Why These Are Needed
- `AWS_ACCOUNT_ID`: Used in ECR image URI in deployment manifest
- `AWS_REGION`: Used for ECR image URI and resource location

---

## Step 4: Deploy Application to EKS

### Execute Script
```bash
# Make script executable
chmod +x scripts/deploy-to-eks.sh

# Deploy application to EKS
./scripts/deploy-to-eks.sh
```

### What This Does
- Updates kubectl context to point to EKS cluster
- Substitutes environment variables in deployment manifest
- Applies Kubernetes manifests (namespace, deployment, service)
- Waits for deployment to be ready
- Shows LoadBalancer URL

### Expected Output
```
Deploying to EKS cluster: zipcode-lookup-cluster
Updating kubeconfig...
Verifying connection to EKS...
NAME                                         STATUS   ROLES    AGE   VERSION
ip-192-168-x-x.us-east-1.compute.internal   Ready    <none>   5m    v1.27.x

Preparing deployment manifest...
Applying Kubernetes manifests...
namespace/zipcode-app created
deployment.apps/zipcode-lookup created
service/zipcode-lookup-service created

Waiting for deployment to be ready...
deployment.apps/zipcode-lookup condition met

Getting service URL...
NAME                     TYPE           CLUSTER-IP       EXTERNAL-IP                     PORT(S)        AGE
zipcode-lookup-service   LoadBalancer   10.100.146.235   a46b31e8cbd194a9e8bfb85c3691db9a-1b17cff6fa9ce299.elb.us-east-1.amazonaws.com   80:31291/TCP   2m

Deployment complete!
```

---

## Step 5: Verify Application

### Check Application Status
```bash
# Check pods
kubectl get pods -n zipcode-app

# Check service and LoadBalancer
kubectl get service zipcode-lookup-service -n zipcode-app

# Watch for LoadBalancer to get external IP
kubectl get service zipcode-lookup-service -n zipcode-app -w
```

### Test Application
```bash
# Get LoadBalancer URL
LB_URL=$(kubectl get service zipcode-lookup-service -n zipcode-app -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
echo "Application URL: http://$LB_URL"

# Test with curl
curl http://$LB_URL

# Or open in browser
# http://a46b31e8cbd194a9e8bfb85c3691db9a-1b17cff6fa9ce299.elb.us-east-1.amazonaws.com
```

---

## Step 6: Monitor and Troubleshoot

### Useful Commands
```bash
# Check all resources
kubectl get all -n zipcode-app

# Check pod logs
kubectl logs -f deployment/zipcode-lookup -n zipcode-app

# Describe service for troubleshooting
kubectl describe service zipcode-lookup-service -n zipcode-app

# Check node status
kubectl get nodes

# Check cluster info
kubectl cluster-info
```

---

## Step 7: Cleanup (Stop Costs)

### Execute Cleanup Script
```bash
# Make script executable
chmod +x scripts/cleanup-eks.sh

# Clean up all AWS resources
./scripts/cleanup-eks.sh
```

### What This Does
- Deletes Kubernetes resources (LoadBalancer, pods, services)
- Waits for LoadBalancer to be deleted (important for cost)
- Deletes entire EKS cluster and node groups
- Removes VPC and associated resources

### Expected Output
```
Cleaning up EKS resources...
Deleting Kubernetes resources...
service "zipcode-lookup-service" deleted
deployment.apps "zipcode-lookup" deleted
namespace "zipcode-app" deleted

Waiting for LoadBalancer to be deleted...
Deleting EKS cluster: zipcode-lookup-cluster
2024-10-01 22:25:00 [ℹ]  deleting EKS cluster "zipcode-lookup-cluster"
...
2024-10-01 22:35:00 [✔]  deleted EKS cluster "zipcode-lookup-cluster"

Cleanup complete!
```

---

## Step 8: Monitor Deletion Progress

### Watch Cluster Deletion
```bash
# In another terminal window - watch deletion progress
while true; do
  echo "$(date): Checking cluster status..."
  eksctl get cluster --name zipcode-lookup-cluster --region us-east-1
  echo "---"
  sleep 30
done
```

### When Deletion is Complete
You'll see this error (which means success):
```
Error: unable to describe control plane "zipcode-lookup-cluster": 
ResourceNotFoundException: No cluster found for name: zipcode-lookup-cluster
```

**Stop the monitoring loop with Ctrl+C**

---

## Step 9: ECR Repository Decision

### Option A: Keep ECR Repository (Recommended)
```bash
# Do nothing - repository remains with your image
# Cost: ~$0.10/month for image storage
# Benefit: Quick redeployment possible
```

### Option B: Delete ECR Repository
```bash
# Delete repository and all images (saves ~$0.10/month)
aws ecr delete-repository --repository-name zipcode-lookup --region us-east-1 --force
```

### Recommendation
**Keep the ECR repository** because:
- Minimal cost (~$0.10/month)
- Enables quick redeployment
- Contains your tested, working image
- Easy to delete later if needed

---

## Step 10: Switch kubectl Context Back

### Return to Minikube
```bash
# Switch kubectl back to minikube
kubectl config use-context minikube

# Verify context
kubectl config current-context
```

---

## Complete Command Sequence Summary

```bash
# 1. Create EKS cluster
./scripts/create-eks-cluster.sh

# 2. Build and push to ECR
./scripts/build-and-push-ecr.sh

# 3. Set environment variables
export AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
export AWS_REGION="us-east-1"

# 4. Deploy to EKS
./scripts/deploy-to-eks.sh

# 5. Test application (get URL from previous output)
curl http://<LoadBalancer-URL>

# 6. Clean up when done
./scripts/cleanup-eks.sh

# 7. Monitor deletion (in another window)
while true; do
  echo "$(date): Checking cluster status..."
  eksctl get cluster --name zipcode-lookup-cluster --region us-east-1
  echo "---"
  sleep 30
done

# 8. Switch back to minikube
kubectl config use-context minikube
```

---

## Cost Impact

### During Deployment
- **EKS Control Plane**: $0.10/hour
- **EC2 Instances**: 2 × $0.0416/hour
- **Network Load Balancer**: $0.0225/hour
- **Total**: ~$0.19/hour

### After Cleanup
- **ECR Repository**: ~$0.10/month (if kept)
- **Everything else**: $0

### Time to Complete
- **Cluster Creation**: 10-15 minutes
- **Image Build/Push**: 2-3 minutes
- **Application Deployment**: 2-3 minutes
- **Cluster Deletion**: 10-15 minutes
- **Total**: ~30-40 minutes

---

## Troubleshooting Common Issues

### Cluster Creation Fails
```bash
# Check IAM permissions
aws iam get-user
aws sts get-caller-identity

# Check eksctl logs
eksctl utils describe-stacks --region us-east-1 --cluster zipcode-lookup-cluster
```

### Image Push Fails
```bash
# Re-authenticate with ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com

# Check repository exists
aws ecr describe-repositories --region us-east-1
```

### Deployment Fails
```bash
# Check kubectl context
kubectl config current-context

# Check if image exists in ECR
aws ecr list-images --repository-name zipcode-lookup --region us-east-1

# Check pod events
kubectl describe pod -n zipcode-app
```

### LoadBalancer Not Getting External IP
```bash
# Check service events
kubectl describe service zipcode-lookup-service -n zipcode-app

# Check AWS Load Balancer Controller (if needed)
kubectl get pods -n kube-system | grep aws-load-balancer
```