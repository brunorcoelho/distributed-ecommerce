# Multi-Machine Deployment Guide

This guide explains how to deploy the distributed e-commerce system across three different machines on the same network.

## Overview

The system consists of:
- **Frontend**: React application served by Nginx (port 3000)
- **Order Service**: Java Spring Boot microservice (port 8080)
- **Inventory Service**: Java Spring Boot microservice (port 8081)

## Prerequisites

1. Three machines connected to the same network
2. Docker and Docker Compose installed on all machines
3. Network ports 3000, 8080, and 8081 accessible between machines
4. Git access to clone the repository on each machine

## Quick Setup

### Option 1: Automated Setup Script

On each machine, run the interactive setup script:

```bash
./setup-multi-machine.sh
```

The script will:
- Detect the current machine's IP address
- Ask which service to run on this machine
- Configure the appropriate environment variables
- Provide startup commands

### Option 2: Manual Configuration

#### Step 1: Determine Machine IPs

First, find the IP address of each machine:
```bash
# Get current machine IP
ip route get 8.8.8.8 | grep -oP 'src \K[^ ]+'
# or
hostname -I | awk '{print $1}'
```

Let's assume:
- Machine A (Frontend): 192.168.1.100
- Machine B (Order Service): 192.168.1.101  
- Machine C (Inventory Service): 192.168.1.102

#### Step 2: Configure Frontend (Machine A)

```bash
# Update frontend configuration
./configure-frontend.sh 192.168.1.101 192.168.1.102

# Or manually edit frontend/.env:
cat > frontend/.env << EOF
VITE_ORDER_SERVICE_URL=http://192.168.1.101:8080
VITE_INVENTORY_SERVICE_URL=http://192.168.1.102:8081
EOF

# Start frontend
cd frontend
docker-compose -f ../deployment/docker-compose.local.yml up frontend -d
```

#### Step 3: Start Order Service (Machine B)

```bash
# Start order service with database
docker-compose -f deployment/docker-compose.local.yml up order-service order-db -d

# Check if it's running
curl http://localhost:8080/api/orders/health
```

#### Step 4: Start Inventory Service (Machine C)

```bash
# Start inventory service with database  
docker-compose -f deployment/docker-compose.local.yml up inventory-service inventory-db -d

# Check if it's running
curl http://localhost:8081/api/inventory/health
```

## Network Testing

### Test Service Connectivity

From the frontend machine (192.168.1.100), test backend services:

```bash
# Test order service
curl http://192.168.1.101:8080/api/orders/health

# Test inventory service  
curl http://192.168.1.102:8081/api/inventory/health
```

### Test From Browser

Open your browser and go to: `http://192.168.1.100:3000`

The frontend should show both services as online (green wifi icons).

## Troubleshooting

### CORS Issues

If you see CORS errors in browser console:

1. **Check CORS Configuration**: Both backend services are configured to accept requests from any origin (`allowedOriginPatterns("*")`)

2. **Verify Service URLs**: Ensure frontend `.env` has correct IP addresses

3. **Check Network Connectivity**: Test if machines can reach each other:
   ```bash
   ping 192.168.1.101  # from frontend machine
   telnet 192.168.1.101 8080  # test port connectivity
   ```

### Service Not Responding

1. **Check if service is running**:
   ```bash
   docker ps
   docker logs <container-name>
   ```

2. **Check port binding**:
   ```bash
   netstat -tlnp | grep 8080
   ```

3. **Firewall issues**: Ensure ports 3000, 8080, 8081 are not blocked

### Frontend Shows "No Connection"

1. Check browser developer console for specific error messages
2. Verify the `.env` file has correct IP addresses  
3. Test backend health endpoints directly with curl
4. Check that both backend services are running

## Port Configuration

| Service | Default Port | Purpose |
|---------|-------------|---------|
| Frontend | 3000 | Web interface |
| Order Service | 8080 | Order management API |
| Inventory Service | 8081 | Inventory management API |
| PostgreSQL (Order) | 5432 | Order database |
| PostgreSQL (Inventory) | 5433 | Inventory database |

## Environment Variables

### Frontend (.env)
```bash
VITE_ORDER_SERVICE_URL=http://<order-service-ip>:8080
VITE_INVENTORY_SERVICE_URL=http://<inventory-service-ip>:8081
```

### Backend Services
Both services automatically accept CORS requests from any origin. No additional configuration needed.

## Security Notes

**⚠️ Important**: This configuration uses `allowedOriginPatterns("*")` which accepts requests from any domain. This is suitable for development and local networks but should be restricted in production environments.

For production, update CORS configuration to only allow specific domains:

```java
.allowedOriginPatterns("http://your-frontend-domain.com")
```

## Monitoring

### Check All Services Status

Create a simple monitoring script:

```bash
#!/bin/bash
echo "=== Service Health Check ==="
echo "Frontend: http://192.168.1.100:3000"
echo "Order Service: $(curl -s http://192.168.1.101:8080/api/orders/health || echo 'DOWN')"  
echo "Inventory Service: $(curl -s http://192.168.1.102:8081/api/inventory/health || echo 'DOWN')"
```

### View Logs

```bash
# On each machine, check service logs
docker logs <service-container-name> -f
```

## Advanced Configuration

### Custom Ports

If you need to use different ports, update:

1. `docker-compose.local.yml` port mappings
2. Frontend `.env` file URLs
3. Any firewall rules

### Database Configuration

Each service runs its own PostgreSQL instance. For a shared database setup, modify the `docker-compose.local.yml` file and update database connection strings.

## Support

If you encounter issues:

1. Check this troubleshooting guide
2. Verify network connectivity between machines
3. Check Docker logs for specific error messages
4. Ensure all prerequisites are met