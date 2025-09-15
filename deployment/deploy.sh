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

# Function to get current machine's IP address
get_machine_ip() {
    # Try to get IP from common network interfaces
    local IP=$(ip route get 8.8.8.8 2>/dev/null | grep -oP 'src \K[^ ]+' | head -1)
    
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
    local inventory_service_ip=$2
    
    print_header "Deploying Frontend Service"
    
    cd ../frontend
    
    # Create environment file with both service URLs
    cat << EOF > .env
VITE_ORDER_SERVICE_URL=http://${order_service_ip}:8080
VITE_INVENTORY_SERVICE_URL=http://${inventory_service_ip}:8081
EOF
    
    print_status "Created .env file:"
    print_status "  Order Service: http://${order_service_ip}:8080"
    print_status "  Inventory Service: http://${inventory_service_ip}:8081"
    
    # Build Docker image
    print_status "Building frontend Docker image..."
    docker build -t ecommerce-frontend .
    
    # Stop existing container if running
    docker stop ecommerce-frontend 2>/dev/null || true
    docker rm ecommerce-frontend 2>/dev/null || true
    
    # Use docker-compose for more reliable deployment
    print_status "Starting frontend using docker-compose..."
    docker-compose -f ../deployment/docker-compose.local.yml up --build frontend -d
    
    print_status "Frontend deployed successfully on port 3000"
    print_status "Access the application at: http://localhost:3000"
}

# Function to deploy order service
deploy_order_service() {
    print_header "Deploying Order Service"
    
    cd ..
    
    # Use docker-compose for deployment with database
    print_status "Starting order service with database using docker-compose..."
    docker-compose -f ../deployment/docker-compose.local.yml up --build order-service order-db -d
    
    print_status "Order service deployed successfully on port 8080"
    print_status "Health check: curl http://localhost:8080/api/orders/health"
}

# Function to deploy inventory service
deploy_inventory_service() {
    print_header "Deploying Inventory Service"
    
    cd ..
    
    # Use docker-compose for deployment with database
    print_status "Starting inventory service with database using docker-compose..."
    docker-compose -f ../deployment/docker-compose.local.yml up --build inventory-service inventory-db -d
    
    print_status "Inventory service deployed successfully on port 8081"
    print_status "Health check: curl http://localhost:8081/api/inventory/health"
}

# Main deployment function
main() {
    local service_type=$1
    
    case $service_type in
        "frontend")
            if [ -z "$2" ] || [ -z "$3" ]; then
                print_error "Usage: $0 frontend <ORDER_SERVICE_IP> <INVENTORY_SERVICE_IP>"
                exit 1
            fi
            check_docker
            deploy_frontend $2 $3
            ;;
        "order-service")
            check_docker
            deploy_order_service
            ;;
        "inventory-service")
            check_docker
            deploy_inventory_service
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
        "ip"|"get-ip")
            MACHINE_IP=$(get_machine_ip)
            print_status "Current machine IP address: $MACHINE_IP"
            print_status "Use this IP when configuring other machines"
            ;;
        "all-services")
            if [ -z "$2" ] || [ -z "$3" ]; then
                print_error "Usage: $0 all-services <ORDER_SERVICE_IP> <INVENTORY_SERVICE_IP>"
                print_error "This will deploy frontend pointing to the specified backend services"
                exit 1
            fi
            check_docker
            deploy_frontend $2 $3
            deploy_order_service
            deploy_inventory_service
            ;;
        "help"|"-h"|"--help")
            print_header "Distributed E-commerce Deployment Script"
            echo ""
            echo "Usage: $0 <service-type> [options]"
            echo ""
            echo "Service types:"
            echo "  frontend <ORDER_SERVICE_IP> <INVENTORY_SERVICE_IP>  - Deploy frontend service"
            echo "  order-service                                       - Deploy order service with database"
            echo "  inventory-service                                   - Deploy inventory service with database"
            echo "  all-services <ORDER_IP> <INVENTORY_IP>              - Deploy all services (for single-machine test)"
            echo "  ip                                                  - Show current machine IP address"
            echo "  setup-db <SERVICE_NAME>                            - Generate database setup script"
            echo "  help                                               - Show this help message"
            echo ""
            echo "Multi-machine deployment examples:"
            echo "  # On frontend machine (192.168.207.157):"
            echo "  $0 frontend 192.168.207.154 192.168.207.156"
            echo ""
            echo "  # On order service machine (192.168.207.154):"
            echo "  $0 order-service"
            echo ""
            echo "  # On inventory service machine (192.168.207.156):"
            echo "  $0 inventory-service"
            echo ""
            echo "  # Get current machine IP:"
            echo "  $0 ip"
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
