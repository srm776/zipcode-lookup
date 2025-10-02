#!/bin/bash

# Variables
CLUSTER_NAME="zipcode-lookup-cluster"
REGION="us-east-1"
NODE_GROUP_NAME="zipcode-lookup-nodes"

echo "Creating EKS cluster: $CLUSTER_NAME in region: $REGION"

# Create EKS cluster
eksctl create cluster \
  --name $CLUSTER_NAME \
  --region $REGION \
  --nodegroup-name $NODE_GROUP_NAME \
  --node-type t3.medium \
  --nodes 2 \
  --nodes-min 1 \
  --nodes-max 4 \
  --managed

echo "Cluster creation initiated. This will take 10-15 minutes..."
echo "You can check progress with: eksctl get cluster --name $CLUSTER_NAME --region $REGION"