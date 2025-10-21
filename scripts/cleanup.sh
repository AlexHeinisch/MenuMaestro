#!/bin/bash

echo "===================================="
echo "MenuMaestro Cleanup"
echo "===================================="
echo ""
echo "This will remove all MenuMaestro resources from Kubernetes."
echo "Data in PersistentVolumeClaims will be deleted."
echo ""
read -p "Are you sure? (y/N) " -n 1 -r
echo

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Cleanup cancelled"
    exit 1
fi

cd "$(dirname "$0")/.."

echo ""
echo "Deleting Kubernetes resources..."
kubectl delete -k k8s/

echo ""
echo "✓ Cleanup complete!"
echo ""
echo "To also delete the Minikube cluster, run:"
echo "  minikube stop"
echo "  minikube delete"
