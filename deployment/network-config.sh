#!/bin/bash

# Network Configuration Script for Distributed E-commerce System
# This script helps configure network settings and firewall rules

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# Function to configure firewall for frontend machine
configure_frontend_firewall() {
    print_header "Configuring Firewall for Frontend Machine"
    
    if command -v ufw &> /dev/null; then
        print_status "Using UFW (Ubuntu Firewall)"
        sudo ufw allow 3000/tcp comment "Frontend HTTP"
        sudo ufw allow 22/tcp comment "SSH"
        print_status "Opened ports: 3000 (Frontend), 22 (SSH)"
    elif command -v firewall-cmd &> /dev/null; then
        print_status "Using firewalld (CentOS/RHEL/Fedora)"
        sudo firewall-cmd --permanent --add-port=3000/tcp
        sudo firewall-cmd --permanent --add-port=22/tcp
        sudo firewall-cmd --reload
        print_status "Opened ports: 3000 (Frontend), 22 (SSH)"
    else
        print_warning "No supported firewall found. Please manually open port 3000"
    fi
}

# Function to configure firewall for order service machine
configure_order_service_firewall() {
    print_header "Configuring Firewall for Order Service Machine"
    
    if command -v ufw &> /dev/null; then
        print_status "Using UFW (Ubuntu Firewall)"
        sudo ufw allow 8080/tcp comment "Order Service API"
        sudo ufw allow 5432/tcp comment "PostgreSQL"
        sudo ufw allow 22/tcp comment "SSH"
        print_status "Opened ports: 8080 (Order Service), 5432 (PostgreSQL), 22 (SSH)"
    elif command -v firewall-cmd &> /dev/null; then
        print_status "Using firewalld (CentOS/RHEL/Fedora)"
        sudo firewall-cmd --permanent --add-port=8080/tcp
        sudo firewall-cmd --permanent --add-port=5432/tcp
        sudo firewall-cmd --permanent --add-port=22/tcp
        sudo firewall-cmd --reload
        print_status "Opened ports: 8080 (Order Service), 5432 (PostgreSQL), 22 (SSH)"
    else
        print_warning "No supported firewall found. Please manually open ports 8080 and 5432"
    fi
}

# Function to configure firewall for inventory service machine
configure_inventory_service_firewall() {
    print_header "Configuring Firewall for Inventory Service Machine"
    
    if command -v ufw &> /dev/null; then
        print_status "Using UFW (Ubuntu Firewall)"
        sudo ufw allow 8081/tcp comment "Inventory Service API"
        sudo ufw allow 5432/tcp comment "PostgreSQL"
        sudo ufw allow 22/tcp comment "SSH"
        print_status "Opened ports: 8081 (Inventory Service), 5432 (PostgreSQL), 22 (SSH)"
    elif command -v firewall-cmd &> /dev/null; then
        print_status "Using firewalld (CentOS/RHEL/Fedora)"
        sudo firewall-cmd --permanent --add-port=8081/tcp
        sudo firewall-cmd --permanent --add-port=5432/tcp
        sudo firewall-cmd --permanent --add-port=22/tcp
        sudo firewall-cmd --reload
        print_status "Opened ports: 8081 (Inventory Service), 5432 (PostgreSQL), 22 (SSH)"
    else
        print_warning "No supported firewall found. Please manually open ports 8081 and 5432"
    fi
}

# Function to test network connectivity
test_connectivity() {
    local target_ip=$1
    local target_port=$2
    local service_name=$3
    
    print_status "Testing connectivity to $service_name at $target_ip:$target_port"
    
    if command -v nc &> /dev/null; then
        if nc -z -w3 $target_ip $target_port 2>/dev/null; then
            print_status "✓ $service_name is reachable"
            return 0
        else
            print_warning "✗ $service_name is not reachable"
            return 1
        fi
    elif command -v telnet &> /dev/null; then
        if echo "quit" | telnet $target_ip $target_port 2>/dev/null | grep -q "Connected"; then
            print_status "✓ $service_name is reachable"
            return 0
        else
            print_warning "✗ $service_name is not reachable"
            return 1
        fi
    else
        print_warning "nc or telnet not found. Cannot test connectivity."
        return 1
    fi
}

# Function to show current network configuration
show_network_info() {
    print_header "Current Network Configuration"
    
    # Show IP addresses
    print_status "Network Interfaces:"
    if command -v ip &> /dev/null; then
        ip addr show | grep -E "^[0-9]+:|inet " | grep -v "127.0.0.1" | head -10
    elif command -v ifconfig &> /dev/null; then
        ifconfig | grep -E "^[a-z]|inet " | grep -v "127.0.0.1" | head -10
    else
        print_warning "Cannot determine network interfaces"
    fi
    
    echo ""
    
    # Show open ports
    print_status "Open Ports:"
    if command -v ss &> /dev/null; then
        ss -tuln | grep -E ":3000|:8080|:8081|:5432" | head -10
    elif command -v netstat &> /dev/null; then
        netstat -tuln | grep -E ":3000|:8080|:8081|:5432" | head -10
    else
        print_warning "Cannot determine open ports"
    fi
}

# Function to generate network diagram
generate_network_diagram() {
    print_header "Network Architecture Diagram"
    
    cat << 'EOF'
    ┌─────────────────────────────────────────────────────────────────────┐
    │                     Distributed E-commerce System                  │
    └─────────────────────────────────────────────────────────────────────┘
    
    ┌─────────────────┐    HTTP/TCP     ┌─────────────────┐    HTTP/TCP
    │   Machine 1     │◄───────────────►│   Machine 2     │◄──────────────┐
    │                 │                 │                 │               │
    │  Frontend       │                 │  Order Service  │               │
    │  (React.js)     │                 │  (Spring Boot)  │               │
    │  Port: 3000     │                 │  Port: 8080     │               │
    │                 │                 │                 │               │
    │  ┌─────────────┐│                 │  ┌─────────────┐│               │
    │  │   Nginx     ││                 │  │ PostgreSQL  ││               │
    │  │   (Port 80) ││                 │  │ (Port 5432) ││               │
    │  └─────────────┘│                 │  └─────────────┘│               │
    └─────────────────┘                 └─────────────────┘               │
                                                                          │
                                                                          │
    ┌─────────────────┐    HTTP/TCP                                       │
    │   Machine 3     │◄─────────────────────────────────────────────────┘
    │                 │
    │ Inventory Svc   │
    │ (Spring Boot)   │
    │ Port: 8081      │
    │                 │
    │  ┌─────────────┐│
    │  │ PostgreSQL  ││
    │  │ (Port 5432) ││
    │  └─────────────┘│
    └─────────────────┘
    
    Communication Flow:
    1. User → Frontend (Machine 1:3000)
    2. Frontend → Order Service (Machine 2:8080)
    3. Order Service → Inventory Service (Machine 3:8081)
    4. Services ↔ PostgreSQL (Port 5432 on each machine)
    
    Required Network Configuration:
    - All machines must be on the same network
    - Firewalls must allow the specified ports
    - DNS resolution or IP addresses must be configured
    
EOF
}

# Main function
main() {
    local action=$1
    
    case $action in
        "frontend")
            configure_frontend_firewall
            ;;
        "order-service")
            configure_order_service_firewall
            ;;
        "inventory-service")
            configure_inventory_service_firewall
            ;;
        "test")
            if [ -z "$2" ] || [ -z "$3" ] || [ -z "$4" ]; then
                print_error "Usage: $0 test <IP> <PORT> <SERVICE_NAME>"
                exit 1
            fi
            test_connectivity $2 $3 $4
            ;;
        "info")
            show_network_info
            ;;
        "diagram")
            generate_network_diagram
            ;;
        "help"|"-h"|"--help")
            print_header "Network Configuration Script"
            echo ""
            echo "Usage: $0 <action> [options]"
            echo ""
            echo "Actions:"
            echo "  frontend             - Configure firewall for frontend machine"
            echo "  order-service        - Configure firewall for order service machine"
            echo "  inventory-service    - Configure firewall for inventory service machine"
            echo "  test <IP> <PORT> <NAME> - Test connectivity to a service"
            echo "  info                 - Show current network configuration"
            echo "  diagram              - Show network architecture diagram"
            echo "  help                 - Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0 frontend"
            echo "  $0 test 192.168.1.100 8080 \"Order Service\""
            echo "  $0 info"
            echo ""
            ;;
        *)
            print_error "Unknown action: $action"
            print_error "Use '$0 help' for usage information"
            exit 1
            ;;
    esac
}

# Run main function with all arguments
main "$@"
