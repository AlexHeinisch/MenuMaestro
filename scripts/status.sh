#!/bin/bash

echo "===================================="
echo "MenuMaestro Deployment Status"
echo "===================================="

# Check if minikube is running
if ! minikube status &> /dev/null; then
    echo "Error: Minikube is not running"
    echo "Run './scripts/setup-minikube.sh' to start it"
    exit 1
fi

echo ""
echo "Minikube Status:"
minikube status

echo ""
echo "Minikube IP: $(minikube ip)"

echo ""
echo "Pods:"
kubectl get pods

echo ""
echo "Services:"
kubectl get services

echo ""
echo "Deployments:"
kubectl get deployments

echo ""
echo "PersistentVolumeClaims:"
kubectl get pvc

echo ""
echo "Access URLs:"
echo "  MenuMaestro:  http://$(minikube ip):30080"
echo "  Mailpit UI:   http://$(minikube ip):30025"
echo "  CloudBeaver:  http://$(minikube ip):30978"
