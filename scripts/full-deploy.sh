#!/bin/bash
set -e

echo "========================================"
echo "Full MenuMaestro Deployment to Minikube"
echo "========================================"

# Navigate to script directory
cd "$(dirname "$0")"

# Step 1: Setup Minikube
echo ""
echo "Step 1/3: Setting up Minikube..."
./setup-minikube.sh

# Step 2: Build image
echo ""
echo "Step 2/3: Building Docker image..."
eval $(minikube docker-env)
./build-image.sh

# Step 3: Deploy
echo ""
echo "Step 3/3: Deploying to Kubernetes..."
./deploy.sh

echo ""
echo "========================================"
echo "✓ Full deployment complete!"
echo "========================================"
