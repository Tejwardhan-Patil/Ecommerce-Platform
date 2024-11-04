#!/bin/bash

# Fail on any error
set -e

# Define variables
DOCKER_REGISTRY="registry.website.com"
DOCKER_COMPOSE_FILE="docker-compose.yml"
K8S_DIR="k8s"
DEPLOYMENT_FILE="deployment.yaml"
SERVICE_FILE="service.yaml"
NAMESPACE="ecommerce"

# Build Docker images for all services
echo "Building Docker images..."
docker-compose -f $DOCKER_COMPOSE_FILE build

# Tag and push Docker images to the registry
echo "Tagging and pushing Docker images to $DOCKER_REGISTRY..."
docker-compose -f $DOCKER_COMPOSE_FILE push

# Apply Kubernetes deployment and services
echo "Applying Kubernetes configurations..."

# Ensure the Kubernetes namespace exists
kubectl get namespace $NAMESPACE || kubectl create namespace $NAMESPACE

# Deploy services to Kubernetes
kubectl apply -f $K8S_DIR/$DEPLOYMENT_FILE --namespace=$NAMESPACE
kubectl apply -f $K8S_DIR/$SERVICE_FILE --namespace=$NAMESPACE

# Verify the deployment
echo "Verifying deployment..."
kubectl rollout status deployment/ecommerce-api --namespace=$NAMESPACE

# Output the status of the services
echo "Current status of services:"
kubectl get pods --namespace=$NAMESPACE
kubectl get services --namespace=$NAMESPACE

echo "Deployment completed successfully!"