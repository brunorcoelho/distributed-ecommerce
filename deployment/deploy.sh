#!/bin/bash

# Distributed E-commerce System Deployment Script
# This script helps deploy each service on separate machines

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${BLUE}=== $1 ===${NC}"
}

# Function to check if Docker is installed
check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    
    if ! docker info &> /dev/null; then
        print_error "Docker daemon is not running. Please start Docker."
        exit 1
    fi
    
    print_status "Docker is installed and running"
}

# Function to check if PostgreSQL is available
check_postgres() {
    local host=$1
    local port=$2
    local user=$3
    local db=$4
    
    if command -v psql &> /dev/null; then
        if PGPASSWORD=$5 psql -h $host -p $port -U $user -d $db -c '\q' 2>/dev/null; then
            print_status "PostgreSQL connection successful"
            return 0
        else
            print_warning "Cannot connect to PostgreSQL at $host:$port"
            return 1
        fi
    else
        print_warning "psql command not found. Cannot verify PostgreSQL connection."
        return 1
    fi
}

# Function to setup PostgreSQL database
setup_database() {
    local service_name=$1
    local db_name=$2
    local db_user=$3
    local db_password=$4
    
    print_header "Setting up PostgreSQL for $service_name"
    
    # Create database and user (assumes PostgreSQL admin access)
    cat << EOF > /tmp/setup_${service_name}_db.sql
CREATE DATABASE ${db_name};
CREATE USER ${db_user} WITH ENCRYPTED PASSWORD '${db_password}';
GRANT ALL PRIVILEGES ON DATABASE ${db_name} TO ${db_user};
ALTER DATABASE ${db_name} OWNER TO ${db_user};
EOF
    
    print_status "Database setup script created at /tmp/setup_${service_name}_db.sql"
    print_status "Run this script as PostgreSQL admin: psql -U postgres -f /tmp/setup_${service_name}_db.sql"
}

# Function to deploy frontend
deploy_frontend() {
    local order_service_ip=$1
    
    print_header "Deploying Frontend Service"
    
    cd ../frontend
    
    # Create environment file
    cat << EOF > .env
REACT_APP_ORDER_SERVICE_URL=http://${order_service_ip}:8080
EOF
    
    print_status "Created .env file with ORDER_SERVICE_URL=http://${order_service_ip}:8080"
    
    # Build Docker image
    print_status "Building frontend Docker image..."
    docker build -t ecommerce-frontend .
    
    # Stop existing container if running
    docker stop ecommerce-frontend 2>/dev/null || true
    docker rm ecommerce-frontend 2>/dev/null || true
    
    # Run container
    print_status "Starting frontend container..."
    docker run -d \
        --name ecommerce-frontend \
        --restart unless-stopped \
        -p 3000:80 \
        -e REACT_APP_ORDER_SERVICE_URL=http://${order_service_ip}:8080 \
        ecommerce-frontend
    
    print_status "Frontend deployed successfully on port 3000"
    print_status "Access the application at: http://localhost:3000"
}

# Function to deploy order service
deploy_order_service() {
    local db_host=$1
    local inventory_service_ip=$2
    local frontend_ip=$3
    
    print_header "Deploying Order Service"
    
    cd ../order-service
    
    # Build Docker image
    print_status "Building order service Docker image..."
    docker build -t order-service .
    
    # Stop existing container if running
    docker stop order-service 2>/dev/null || true
    docker rm order-service 2>/dev/null || true
    
    # Run container
    print_status "Starting order service container..."
    docker run -d \
        --name order-service \
        --restart unless-stopped \
        -p 8080:8080 \
        -e SPRING_DATASOURCE_URL=jdbc:postgresql://${db_host}:5432/order_service_db \
        -e SPRING_DATASOURCE_USERNAME=order_user \
        -e SPRING_DATASOURCE_PASSWORD=order_password \
        -e INVENTORY_SERVICE_URL=http://${inventory_service_ip}:8081 \
        -e CORS_ALLOWED_ORIGINS=http://${frontend_ip}:3000 \
        order-service
    
    print_status "Order service deployed successfully on port 8080"
    print_status "Health check: curl http://localhost:8080/api/orders/health"
}

# Function to deploy inventory service
deploy_inventory_service() {
    local db_host=$1
    
    print_header "Deploying Inventory Service"
    
    cd ../inventory-service
    
    # Build Docker image
    print_status "Building inventory service Docker image..."
    docker build -t inventory-service .
    
    # Stop existing container if running
    docker stop inventory-service 2>/dev/null || true
    docker rm inventory-service 2>/dev/null || true
    
    # Run container
    print_status "Starting inventory service container..."
    docker run -d \
        --name inventory-service \
        --restart unless-stopped \
        -p 8081:8081 \
        -e SPRING_DATASOURCE_URL=jdbc:postgresql://${db_host}:5432/inventory_service_db \
        -e SPRING_DATASOURCE_USERNAME=inventory_user \
        -e SPRING_DATASOURCE_PASSWORD=inventory_password \
        inventory-service
    
    print_status "Inventory service deployed successfully on port 8081"
    print_status "Health check: curl http://localhost:8081/api/inventory/health"
}

# Main deployment function
main() {
    local service_type=$1
    
    case $service_type in
        "frontend")
            if [ -z "$2" ]; then
                print_error "Usage: $0 frontend <ORDER_SERVICE_IP>"
                exit 1
            fi
            check_docker
            deploy_frontend $2
            ;;
        "order-service")
            if [ -z "$2" ] || [ -z "$3" ] || [ -z "$4" ]; then
                print_error "Usage: $0 order-service <DB_HOST> <INVENTORY_SERVICE_IP> <FRONTEND_IP>"
                exit 1
            fi
            check_docker
            deploy_order_service $2 $3 $4
            ;;
        "inventory-service")
            if [ -z "$2" ]; then
                print_error "Usage: $0 inventory-service <DB_HOST>"
                exit 1
            fi
            check_docker
            deploy_inventory_service $2
            ;;
        "setup-db")
            if [ -z "$2" ]; then
                print_error "Usage: $0 setup-db <SERVICE_NAME>"
                print_error "SERVICE_NAME: order-service or inventory-service"
                exit 1
            fi
            if [ "$2" = "order-service" ]; then
                setup_database "order-service" "order_service_db" "order_user" "order_password"
            elif [ "$2" = "inventory-service" ]; then
                setup_database "inventory-service" "inventory_service_db" "inventory_user" "inventory_password"
            else
                print_error "Invalid service name. Use 'order-service' or 'inventory-service'"
                exit 1
            fi
            ;;
        "help"|"-h"|"--help")
            print_header "Distributed E-commerce Deployment Script"
            echo ""
            echo "Usage: $0 <service-type> [options]"
            echo ""
            echo "Service types:"
            echo "  frontend <ORDER_SERVICE_IP>                    - Deploy frontend service"
            echo "  order-service <DB_HOST> <INVENTORY_IP> <FRONTEND_IP> - Deploy order service"
            echo "  inventory-service <DB_HOST>                    - Deploy inventory service"
            echo "  setup-db <SERVICE_NAME>                       - Generate database setup script"
            echo "  help                                          - Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0 frontend 192.168.1.100"
            echo "  $0 order-service 192.168.1.100 192.168.1.102 192.168.1.101"
            echo "  $0 inventory-service 192.168.1.100"
            echo "  $0 setup-db order-service"
            echo ""
            ;;
        *)
            print_error "Unknown service type: $service_type"
            print_error "Use '$0 help' for usage information"
            exit 1
            ;;
    esac
}

# Run main function with all arguments
main "$@"
