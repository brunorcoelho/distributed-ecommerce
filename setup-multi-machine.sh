#!/bin/bash

# Multi-machine deployment script for distributed e-commerce system
# This script helps configure the system to run on different machines

echo "=== Distributed E-commerce Multi-Machine Setup ==="
echo

# Function to get the current machine's IP address
get_machine_ip() {
    # Try to get IP from common network interfaces
    IP=$(ip route get 8.8.8.8 2>/dev/null | grep -oP 'src \K[^ ]+' | head -1)
    
    if [[ -z "$IP" ]]; then
        # Fallback method
        IP=$(hostname -I 2>/dev/null | awk '{print $1}')
    fi
    
    if [[ -z "$IP" ]]; then
        # Another fallback
        IP=$(ip addr show | grep -oP '192\.168\.\d+\.\d+' | head -1)
    fi
    
    echo "$IP"
}

# Get current machine IP
CURRENT_IP=$(get_machine_ip)
echo "Detected current machine IP: $CURRENT_IP"
echo

# Service selection menu
echo "What service will run on this machine?"
echo "1) Frontend (React + Nginx)"
echo "2) Order Service (Java Spring Boot)"
echo "3) Inventory Service (Java Spring Boot)"
echo "4) Configure environment only"
read -p "Select option (1-4): " SERVICE_CHOICE

case $SERVICE_CHOICE in
    1)
        echo "Setting up Frontend..."
        echo "You will need the IP addresses of the machines running:"
        echo "- Order Service (port 8080)"
        echo "- Inventory Service (port 8081)"
        echo
        
        read -p "Enter Order Service machine IP: " ORDER_IP
        read -p "Enter Inventory Service machine IP: " INVENTORY_IP
        
        # Update frontend .env file
        cat > frontend/.env << EOF
# Multi-machine deployment configuration
VITE_ORDER_SERVICE_URL=http://$ORDER_IP:8080
VITE_INVENTORY_SERVICE_URL=http://$INVENTORY_IP:8081

# Generated on $(date)
# Frontend IP: $CURRENT_IP
EOF
        
        echo "Frontend .env updated!"
        echo "To start the frontend: cd frontend && docker-compose -f ../deployment/docker-compose.local.yml up frontend -d"
        ;;
        
    2)
        echo "Setting up Order Service..."
        echo "Order Service will run on IP: $CURRENT_IP:8080"
        echo "Make sure this IP is accessible from the frontend machine."
        echo
        
        read -p "Enter Inventory Service machine IP (for inter-service communication): " INVENTORY_IP
        
        # Create environment file for order service
        mkdir -p deployment/order-service-env
        cat > deployment/order-service-env/application.properties << EOF
# Order Service Multi-machine Configuration
cors.allowed-origins=*

# Inventory service URL for inter-service communication
inventory.service.url=http://$INVENTORY_IP:8081

# Database configuration (keep existing settings)
spring.datasource.url=\${DATABASE_URL:jdbc:postgresql://localhost:5432/orders}
spring.datasource.username=\${DATABASE_USERNAME:orders}
spring.datasource.password=\${DATABASE_PASSWORD:password}
EOF
        
        echo "Order Service configuration updated!"
        echo "To start: docker-compose -f deployment/docker-compose.local.yml up order-service -d"
        ;;
        
    3)
        echo "Setting up Inventory Service..."
        echo "Inventory Service will run on IP: $CURRENT_IP:8081"
        echo "Make sure this IP is accessible from frontend and order service machines."
        
        # Create environment file for inventory service
        mkdir -p deployment/inventory-service-env
        cat > deployment/inventory-service-env/application.properties << EOF
# Inventory Service Multi-machine Configuration
cors.allowed-origins=*

# Database configuration (keep existing settings)
spring.datasource.url=\${DATABASE_URL:jdbc:postgresql://localhost:5432/inventory}
spring.datasource.username=\${DATABASE_USERNAME:inventory}
spring.datasource.password=\${DATABASE_PASSWORD:password}
EOF
        
        echo "Inventory Service configuration updated!"
        echo "To start: docker-compose -f deployment/docker-compose.local.yml up inventory-service -d"
        ;;
        
    4)
        echo "Environment configuration mode..."
        echo "Current machine IP: $CURRENT_IP"
        echo
        echo "Manual configuration steps:"
        echo "1. Update frontend/.env with correct service IPs"
        echo "2. Ensure CORS is configured to allow cross-machine requests"
        echo "3. Use docker-compose to start the appropriate service on each machine"
        ;;
        
    *)
        echo "Invalid option selected."
        exit 1
        ;;
esac

echo
echo "=== Network Requirements ==="
echo "Ensure the following ports are accessible between machines:"
echo "- Frontend: port 3000 (for browser access)"
echo "- Order Service: port 8080"
echo "- Inventory Service: port 8081"
echo "- PostgreSQL: port 5432 (if using shared database)"
echo
echo "=== Quick Network Test ==="
echo "To test connectivity from other machines:"
echo "curl http://$CURRENT_IP:PORT/api/health"
echo
echo "Setup complete!"