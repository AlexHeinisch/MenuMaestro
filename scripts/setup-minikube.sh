#!/bin/bash
set -e

echo "===================================="
echo "Setting up Minikube for MenuMaestro"
echo "===================================="

# Check if minikube is installed
if ! command -v minikube &> /dev/null; then
    echo "Error: minikube is not installed. Please install it first."
    echo "Visit: https://minikube.sigs.k8s.io/docs/start/"
    exit 1
fi

# Check if minikube is running
if ! minikube status &> /dev/null; then
    echo "Starting Minikube..."
    minikube start
else
    echo "Minikube is already running"
fi

echo ""
echo "Configuring Docker environment to use Minikube's Docker daemon..."
eval $(minikube docker-env)

echo ""
echo "✓ Minikube setup complete!"
echo ""
echo "Docker environment configured. You can now build images."
echo "Run 'eval \$(minikube docker-env)' in other terminals to use Minikube's Docker."
echo ""
echo "Minikube IP: $(minikube ip)"
