#!/bin/bash

# Quick frontend configuration script
# Usage: ./configure-frontend.sh <order-service-ip> <inventory-service-ip>

if [ $# -ne 2 ]; then
    echo "Usage: $0 <order-service-ip> <inventory-service-ip>"
    echo "Example: $0 192.168.1.100 192.168.1.101"
    exit 1
fi

ORDER_IP=$1
INVENTORY_IP=$2

echo "Configuring frontend for:"
echo "  Order Service: $ORDER_IP:8080"
echo "  Inventory Service: $INVENTORY_IP:8081"

# Update frontend .env
cat > frontend/.env << EOF
# Multi-machine deployment configuration
VITE_ORDER_SERVICE_URL=http://$ORDER_IP:8080
VITE_INVENTORY_SERVICE_URL=http://$INVENTORY_IP:8081

# Auto-generated on $(date)
EOF

echo "Frontend configuration updated!"
echo "You can now start the frontend with:"
echo "cd frontend && docker-compose -f ../deployment/docker-compose.local.yml up frontend -d"