#!/bin/bash

# Variables
CLUSTER_NAME="zipcode-lookup-cluster"
REGION="us-east-1"

echo "Cleaning up EKS resources..."

# Delete Kubernetes resources first
echo "Deleting Kubernetes resources..."
kubectl delete -f k8s/aws/ --ignore-not-found=true
kubectl delete -f k8s/namespace.yaml --ignore-not-found=true

# Wait a bit for LoadBalancer to be deleted
echo "Waiting for LoadBalancer to be deleted..."
sleep 30

# Delete EKS cluster
echo "Deleting EKS cluster: $CLUSTER_NAME"
eksctl delete cluster --name $CLUSTER_NAME --region $REGION

echo "Cleanup complete!"
echo "Note: ECR repository is not deleted. Delete manually if needed:"
echo "aws ecr delete-repository --repository-name zipcode-lookup --region $REGION --force"