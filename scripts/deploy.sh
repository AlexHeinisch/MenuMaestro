#!/bin/bash
set -e

echo "===================================="
echo "Deploying MenuMaestro to Kubernetes"
echo "===================================="

# Navigate to project root
cd "$(dirname "$0")/.."

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    echo "Error: kubectl is not installed. Please install it first."
    echo "Visit: https://kubernetes.io/docs/tasks/tools/"
    exit 1
fi

echo ""
echo "Applying Kubernetes manifests..."
kubectl apply -k k8s/

echo ""
echo "Waiting for deployments to be ready..."
echo "(This may take a few minutes on first deployment)"
echo ""

kubectl wait --for=condition=available --timeout=300s deployment/postgres || true
kubectl wait --for=condition=available --timeout=300s deployment/mailpit || true
kubectl wait --for=condition=available --timeout=300s deployment/cloudbeaver || true
kubectl wait --for=condition=available --timeout=600s deployment/menumaestro || true

echo ""
echo "✓ Deployment complete!"
echo ""
echo "Current pod status:"
kubectl get pods
echo ""
echo "Services:"
kubectl get services
echo ""
echo "Access URLs:"
echo "  MenuMaestro:  http://$(minikube ip):30080"
echo "  Mailpit UI:   http://$(minikube ip):30025"
echo "  CloudBeaver:  http://$(minikube ip):30978"
echo ""
echo "Or use 'minikube service <name>' to open in browser"
