#!/bin/bash

echo "===================================="
echo "Opening MenuMaestro Services"
echo "===================================="

# Check if minikube is running
if ! minikube status &> /dev/null; then
    echo "Error: Minikube is not running"
    echo "Run './scripts/setup-minikube.sh' to start it"
    exit 1
fi

SERVICE=${1:-all}

open_service() {
    local service_name=$1
    echo "Opening $service_name..."
    minikube service $service_name
}

case $SERVICE in
    menumaestro)
        open_service menumaestro
        ;;
    mailpit)
        open_service mailpit
        ;;
    cloudbeaver)
        open_service cloudbeaver
        ;;
    all)
        echo "Opening all services in browser..."
        echo ""
        open_service menumaestro &
        open_service mailpit &
        open_service cloudbeaver &
        wait
        ;;
    *)
        echo "Usage: $0 [service]"
        echo ""
        echo "Available services:"
        echo "  menumaestro   - MenuMaestro application"
        echo "  mailpit       - Mailpit mail server UI"
        echo "  cloudbeaver   - CloudBeaver database UI"
        echo "  all           - All services (default)"
        exit 1
        ;;
esac
