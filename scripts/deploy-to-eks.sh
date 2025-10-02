#!/bin/bash

# Variables
CLUSTER_NAME="zipcode-lookup-cluster"
REGION="us-east-1"
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

echo "Deploying to EKS cluster: $CLUSTER_NAME"

# Update kubeconfig to point to EKS cluster
echo "Updating kubeconfig..."
aws eks update-kubeconfig --region $REGION --name $CLUSTER_NAME

# Verify connection to EKS
echo "Verifying connection to EKS..."
kubectl get nodes

# Substitute environment variables in deployment manifest
echo "Preparing deployment manifest..."
envsubst < k8s/aws/deployment.yaml > k8s/aws/deployment-final.yaml

# Apply manifests
echo "Applying Kubernetes manifests..."
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/aws/deployment-final.yaml
kubectl apply -f k8s/aws/service.yaml

# Wait for deployment
echo "Waiting for deployment to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/zipcode-lookup -n zipcode-app

# Get service URL
echo "Getting service URL..."
kubectl get service zipcode-lookup-service -n zipcode-app

echo "Deployment complete!"
echo "It may take a few minutes for the LoadBalancer to be ready."
echo "Check status with: kubectl get service zipcode-lookup-service -n zipcode-app"

# Clean up temporary file
rm -f k8s/aws/deployment-final.yaml