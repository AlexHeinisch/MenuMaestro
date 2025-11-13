#!/bin/bash
set -e

echo "====================================="
echo "Building MenuMaestro Docker Image"
echo "====================================="

# Navigate to project root
cd "$(dirname "$0")/.."

# Check if we're using Minikube's Docker
if [ -z "$MINIKUBE_ACTIVE_DOCKERD" ]; then
    echo "Warning: Minikube Docker environment not detected!"
    echo "Please run: eval \$(minikube docker-env)"
    echo ""
    read -p "Continue anyway? (y/N) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

echo ""
echo "Building Docker image..."
docker build -t menumaestro:latest .

echo ""
echo "✓ Docker image built successfully!"
echo ""
docker images | grep menumaestro
