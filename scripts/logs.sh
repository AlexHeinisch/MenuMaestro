#!/bin/bash

# Function to show usage
show_usage() {
    echo "Usage: $0 [service]"
    echo ""
    echo "Available services:"
    echo "  menumaestro   - MenuMaestro application logs"
    echo "  postgres      - PostgreSQL database logs"
    echo "  mailpit       - Mailpit mail server logs"
    echo "  cloudbeaver   - CloudBeaver database UI logs"
    echo "  all           - All services (in separate windows)"
    echo ""
    echo "If no service is specified, menumaestro logs will be shown."
    echo ""
    echo "Examples:"
    echo "  $0 menumaestro"
    echo "  $0 postgres"
}

SERVICE=${1:-menumaestro}

case $SERVICE in
    menumaestro)
        echo "Showing MenuMaestro logs (Ctrl+C to exit)..."
        kubectl logs -f deployment/menumaestro
        ;;
    postgres)
        echo "Showing PostgreSQL logs (Ctrl+C to exit)..."
        kubectl logs -f deployment/postgres
        ;;
    mailpit)
        echo "Showing Mailpit logs (Ctrl+C to exit)..."
        kubectl logs -f deployment/mailpit
        ;;
    cloudbeaver)
        echo "Showing CloudBeaver logs (Ctrl+C to exit)..."
        kubectl logs -f deployment/cloudbeaver
        ;;
    all)
        echo "Opening logs for all services..."
        echo "Note: This requires multiple terminal windows."
        echo ""
        echo "Run these commands in separate terminals:"
        echo "  kubectl logs -f deployment/menumaestro"
        echo "  kubectl logs -f deployment/postgres"
        echo "  kubectl logs -f deployment/mailpit"
        echo "  kubectl logs -f deployment/cloudbeaver"
        ;;
    -h|--help)
        show_usage
        ;;
    *)
        echo "Error: Unknown service '$SERVICE'"
        echo ""
        show_usage
        exit 1
        ;;
esac
