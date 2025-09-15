#!/bin/bash

# Script para executar o sistema distribuído localmente
echo "🚀 Iniciando o Sistema de E-commerce Distribuído"
echo "📊 Executando todos os serviços em uma única máquina..."

# Verificar se o Docker está rodando
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker não está rodando. Por favor, inicie o Docker primeiro."
    exit 1
fi

# Verificar se o Docker Compose está disponível
if ! command -v docker-compose > /dev/null 2>&1; then
    echo "❌ Docker Compose não encontrado. Por favor, instale o Docker Compose."
    exit 1
fi

echo "🔧 Construindo e iniciando os serviços..."

# Para e remove containers existentes
docker-compose -f deployment/docker-compose.local.yml down --volumes --remove-orphans

# Constrói e inicia os serviços
docker-compose -f deployment/docker-compose.local.yml up --build

echo "🎯 Sistema iniciado! Acesse:"
echo "🌐 Frontend: http://localhost:3000"
echo "📦 Order Service: http://localhost:8080"
echo "📋 Inventory Service: http://localhost:8081"
