#!/bin/bash

echo "Building Docker image..."
docker build -t zipcode-lookup:latest .

echo "Applying Kubernetes manifests..."
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml

echo "Waiting for deployment to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/zipcode-lookup -n zipcode-app

echo "Getting service URL..."
minikube service zipcode-lookup-service -n zipcode-app --url

echo "Deployment complete!"
echo "Access your application at the URL shown above"