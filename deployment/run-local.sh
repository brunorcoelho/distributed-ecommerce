#!/bin/bash

# Script para rodar todo o sistema na mesma máquina (desenvolvimento/teste local)

set -e

echo "🚀 Sistema de E-commerce Distribuído - Deploy Local"
echo "=================================================="
echo

# Verificar se Docker está instalado
if ! command -v docker &> /dev/null; then
    echo "❌ Docker não encontrado. Instale o Docker primeiro."
    exit 1
fi

# Verificar se Docker Compose está disponível
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "❌ Docker Compose não encontrado. Instale o Docker Compose primeiro."
    exit 1
fi

# Função para usar docker-compose ou docker compose
docker_compose_cmd() {
    if command -v docker-compose &> /dev/null; then
        docker-compose "$@"
    else
        docker compose "$@"
    fi
}

echo "📋 Verificando pré-requisitos..."
echo "✅ Docker instalado e funcionando"

# Navegar para o diretório do projeto
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Parar containers existentes se houver
echo "🛑 Parando containers existentes..."
docker_compose_cmd -f docker-compose.local.yml down --remove-orphans 2>/dev/null || true

# Limpar volumes se solicitado
if [[ "$1" == "--clean" ]]; then
    echo "🧹 Limpando volumes..."
    docker_compose_cmd -f docker-compose.local.yml down -v 2>/dev/null || true
    docker volume rm deployment_postgres_orders_data deployment_postgres_inventory_data 2>/dev/null || true
fi

echo "🔧 Construindo e iniciando serviços..."
echo

# Build e start dos serviços
docker_compose_cmd -f docker-compose.local.yml up --build -d

echo "⏳ Aguardando serviços ficarem prontos..."
echo

# Função para aguardar serviço ficar disponível
wait_for_service() {
    local url=$1
    local name=$2
    local max_attempts=30
    local attempt=1
    
    echo -n "   Aguardando $name"
    while [ $attempt -le $max_attempts ]; do
        if curl -s -o /dev/null -w "%{http_code}" "$url" | grep -q "200"; then
            echo " ✅"
            return 0
        fi
        echo -n "."
        sleep 2
        ((attempt++))
    done
    echo " ❌ (timeout)"
    return 1
}

# Aguardar serviços ficarem prontos
wait_for_service "http://localhost:8081/api/inventory/health" "Inventory Service"
wait_for_service "http://localhost:8080/api/orders/health" "Order Service"
wait_for_service "http://localhost:3000" "Frontend"

echo
echo "🎉 Sistema implantado com sucesso!"
echo "================================="
echo
echo "📱 Acessos:"
echo "   Frontend:          http://localhost:3000"
echo "   Order Service:     http://localhost:8080"
echo "   Inventory Service: http://localhost:8081"
echo
echo "🔍 Health Checks:"
echo "   Order Service:     http://localhost:8080/api/orders/health"
echo "   Inventory Service: http://localhost:8081/api/inventory/health"
echo
echo "📊 APIs de Teste:"
echo "   Produtos:          http://localhost:8081/api/inventory/products"
echo "   Pedidos:           http://localhost:8080/api/orders"
echo "   Estatísticas:      http://localhost:8080/api/orders/statistics"
echo
echo "🖥️  Comandos úteis:"
echo "   Ver logs:          docker-compose -f deployment/docker-compose.local.yml logs -f"
echo "   Parar sistema:     docker-compose -f deployment/docker-compose.local.yml down"
echo "   Reiniciar:         bash deployment/run-local.sh"
echo "   Limpar tudo:       bash deployment/run-local.sh --clean"
echo
echo "🧪 Teste rápido:"
echo '   curl -X POST http://localhost:8080/api/orders \'
echo '     -H "Content-Type: application/json" \'
echo '     -d '"'"'{"customerName":"Teste","customerEmail":"teste@email.com","customerAddress":"Rua Teste, 123","items":[{"productId":1,"productName":"Smartphone Galaxy","quantity":1,"price":899.99}],"totalAmount":899.99}'"'"''
echo

# Mostrar status dos containers
echo "📊 Status dos Containers:"
docker_compose_cmd -f docker-compose.local.yml ps
