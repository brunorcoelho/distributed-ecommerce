# Guia de Deploy - Sistema de E-commerce Distribuído

Este guia detalha como implantar o sistema de e-commerce distribuído em três máquinas separadas na mesma rede.

## Visão Geral da Arquitetura

O sistema é composto por três aplicações independentes:

- **Máquina 1**: Frontend (React.js) - Interface do usuário
- **Máquina 2**: Order Service (Spring Boot) + PostgreSQL - Gerenciamento de pedidos
- **Máquina 3**: Inventory Service (Spring Boot) + PostgreSQL - Controle de estoque

## Pré-requisitos

### Requisitos de Hardware

**Mínimo recomendado por máquina:**
- 2 CPU cores
- 4GB RAM
- 20GB armazenamento
- Conexão de rede

**Para produção:**
- 4+ CPU cores
- 8GB+ RAM
- 50GB+ armazenamento SSD
- Conexão de rede estável

### Requisitos de Software

**Todas as máquinas:**
- Ubuntu 20.04+ / CentOS 8+ / Debian 11+
- Docker 20.10+
- Docker Compose 2.0+
- Acesso SSH

**Opcionais (para desenvolvimento):**
- Java 17+ (para build local)
- Node.js 18+ (para build local)
- Maven 3.8+

## Configuração de Rede

### Exemplo de IPs

Para este guia, usaremos os seguintes IPs (substitua pelos IPs reais):

- **Máquina 1 (Frontend)**: 192.168.1.101
- **Máquina 2 (Order Service)**: 192.168.1.100
- **Máquina 3 (Inventory Service)**: 192.168.1.102

### Portas Utilizadas

| Serviço | Máquina | Porta | Descrição |
|---------|---------|-------|-----------|
| Frontend | 1 | 3000 | Interface web |
| Order Service | 2 | 8080 | API REST |
| PostgreSQL (Orders) | 2 | 5432 | Banco de dados |
| Inventory Service | 3 | 8081 | API REST |
| PostgreSQL (Inventory) | 3 | 5432 | Banco de dados |

## Instalação do Docker

### Ubuntu/Debian

```bash
# Atualizar sistema
sudo apt update && sudo apt upgrade -y

# Instalar Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Adicionar usuário ao grupo docker
sudo usermod -aG docker $USER

# Instalar Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Reiniciar sessão ou executar
newgrp docker
```

### CentOS/RHEL

```bash
# Instalar Docker
sudo yum install -y yum-utils
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo yum install -y docker-ce docker-ce-cli containerd.io

# Iniciar Docker
sudo systemctl start docker
sudo systemctl enable docker

# Adicionar usuário ao grupo docker
sudo usermod -aG docker $USER

# Instalar Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

## Deploy por Máquina

### Máquina 1: Frontend

**Preparação:**

```bash
# Clonar o projeto
git clone <repository-url>
cd distributed-ecommerce

# Configurar firewall
./deployment/network-config.sh frontend

# Ou manualmente:
sudo ufw allow 3000/tcp
sudo ufw allow 22/tcp
```

**Deploy usando script:**

```bash
./deployment/deploy.sh frontend 192.168.1.100
```

**Deploy usando Docker Compose:**

```bash
# Editar IPs no arquivo
nano deployment/docker-compose.frontend.yml

# Atualizar REACT_APP_ORDER_SERVICE_URL com IP real do Order Service
# Exemplo: http://192.168.1.100:8080

# Deploy
cd deployment
docker-compose -f docker-compose.frontend.yml up -d
```

**Verificação:**

```bash
# Verificar containers
docker ps

# Verificar logs
docker logs ecommerce-frontend

# Testar acesso
curl http://localhost:3000
```

### Máquina 2: Order Service

**Preparação:**

```bash
# Clonar o projeto
git clone <repository-url>
cd distributed-ecommerce

# Configurar firewall
./deployment/network-config.sh order-service

# Ou manualmente:
sudo ufw allow 8080/tcp
sudo ufw allow 5432/tcp
sudo ufw allow 22/tcp
```

**Deploy usando Docker Compose:**

```bash
# Editar IPs no arquivo
nano deployment/docker-compose.order-service.yml

# Atualizar:
# - INVENTORY_SERVICE_URL com IP do Inventory Service (192.168.1.102:8081)
# - CORS_ALLOWED_ORIGINS com IP do Frontend (192.168.1.101:3000)

# Deploy
cd deployment
docker-compose -f docker-compose.order-service.yml up -d
```

**Verificação:**

```bash
# Verificar containers
docker ps

# Verificar logs
docker logs order-service
docker logs postgres-orders

# Testar APIs
curl http://localhost:8080/api/orders/health
curl http://localhost:8080/api/orders/statistics
```

### Máquina 3: Inventory Service

**Preparação:**

```bash
# Clonar o projeto
git clone <repository-url>
cd distributed-ecommerce

# Configurar firewall
./deployment/network-config.sh inventory-service
```

**Deploy usando Docker Compose:**

```bash
cd deployment
docker-compose -f docker-compose.inventory-service.yml up -d
```

**Verificação:**

```bash
# Verificar containers
docker ps

# Verificar logs
docker logs inventory-service
docker logs postgres-inventory

# Testar APIs
curl http://localhost:8081/api/inventory/health
curl http://localhost:8081/api/inventory/products
curl http://localhost:8081/api/inventory/statistics
```

## Testes de Conectividade

### Teste das Conexões

```bash
# Da máquina Frontend (1), testar Order Service (2)
./deployment/network-config.sh test 192.168.1.100 8080 "Order Service"

# Da máquina Order Service (2), testar Inventory Service (3)
./deployment/network-config.sh test 192.168.1.102 8081 "Inventory Service"

# Teste completo do fluxo
curl -X POST http://192.168.1.100:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Teste",
    "customerEmail": "teste@email.com",
    "customerAddress": "Rua Teste, 123",
    "items": [
      {
        "productId": 1,
        "productName": "Smartphone Galaxy",
        "quantity": 1,
        "price": 899.99
      }
    ],
    "totalAmount": 899.99
  }'
```

### Verificação do Sistema Completo

**1. Acesse o Frontend:**
```
http://192.168.1.101:3000
```

**2. Teste o fluxo completo:**
- Navegue pelos produtos
- Adicione itens ao carrinho
- Preencha dados no checkout
- Finalize um pedido
- Verifique se o status é retornado

**3. Verifique os logs:**
```bash
# Frontend
docker logs ecommerce-frontend

# Order Service
docker logs order-service

# Inventory Service
docker logs inventory-service
```

## Configurações Avançadas

### SSL/HTTPS (Produção)

**Frontend com SSL:**

```bash
# Gerar certificados SSL
sudo apt install certbot

# Obter certificado (substitua pelo domínio real)
sudo certbot certonly --standalone -d seu-dominio.com

# Configurar Nginx com SSL (ver frontend/nginx-ssl.conf)
```

**Services com SSL:**

```bash
# Configurar SSL nos Spring Boot services
# Adicionar no application.properties:
server.ssl.key-store=/path/to/keystore.p12
server.ssl.key-store-password=password
server.ssl.key-store-type=PKCS12
```

### Monitoramento

**Adicionar ao Docker Compose:**

```yaml
  prometheus:
    image: prom/prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml

  grafana:
    image: grafana/grafana
    ports:
      - "3001:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
```

### Backup Automático

**Script de backup PostgreSQL:**

```bash
#!/bin/bash
# backup-db.sh

DATE=$(date +%Y%m%d_%H%M%S)

# Backup Order Service DB
docker exec postgres-orders pg_dump -U order_user order_service_db > "order_db_backup_$DATE.sql"

# Backup Inventory Service DB
docker exec postgres-inventory pg_dump -U inventory_user inventory_service_db > "inventory_db_backup_$DATE.sql"

# Limpar backups antigos (manter últimos 7 dias)
find . -name "*_backup_*.sql" -mtime +7 -delete
```

## Resolução de Problemas

### Problemas Comuns

**1. Frontend não carrega:**
```bash
# Verificar se o Order Service está acessível
curl http://192.168.1.100:8080/api/orders/health

# Verificar CORS no Order Service
docker logs order-service | grep CORS
```

**2. Order Service não conecta ao Inventory:**
```bash
# Verificar conectividade
telnet 192.168.1.102 8081

# Verificar logs
docker logs order-service | grep inventory
```

**3. Erro de banco de dados:**
```bash
# Verificar status do PostgreSQL
docker exec postgres-orders pg_isready -U order_user

# Verificar logs do banco
docker logs postgres-orders
```

**4. Erro de permissão Docker:**
```bash
# Adicionar usuário ao grupo docker
sudo usermod -aG docker $USER
newgrp docker
```

### Comandos Úteis

**Verificar status geral:**
```bash
# Ver todos os containers
docker ps -a

# Ver uso de recursos
docker stats

# Ver redes Docker
docker network ls

# Ver informações do sistema
./deployment/network-config.sh info
```

**Reiniciar serviços:**
```bash
# Reiniciar um serviço específico
docker-compose restart order-service

# Reiniciar tudo
docker-compose down && docker-compose up -d
```

**Logs e debug:**
```bash
# Logs em tempo real
docker logs -f order-service

# Logs de todos os containers
docker-compose logs

# Entrar no container para debug
docker exec -it order-service bash
```

### Configuração de DNS (Opcional)

**Editar /etc/hosts em cada máquina:**

```bash
# Máquina 1 (Frontend)
192.168.1.100 order-service
192.168.1.102 inventory-service

# Máquina 2 (Order Service)
192.168.1.101 frontend
192.168.1.102 inventory-service

# Máquina 3 (Inventory Service)
192.168.1.101 frontend
192.168.1.100 order-service
```

Então usar nomes ao invés de IPs nas configurações:
```
REACT_APP_ORDER_SERVICE_URL=http://order-service:8080
INVENTORY_SERVICE_URL=http://inventory-service:8081
```

## Manutenção

### Atualizações

**Atualizar uma aplicação:**

```bash
# Parar o serviço
docker-compose stop order-service

# Reconstruir imagem
docker-compose build order-service

# Iniciar serviço
docker-compose up -d order-service
```

### Backup e Restore

**Backup completo:**

```bash
# Criar backup dos dados
docker exec postgres-orders pg_dump -U order_user order_service_db > order_backup.sql
docker exec postgres-inventory pg_dump -U inventory_user inventory_service_db > inventory_backup.sql

# Backup dos volumes Docker
docker run --rm -v distributed-ecommerce_postgres_orders_data:/data -v $(pwd):/backup ubuntu tar czf /backup/orders_data.tar.gz /data
```

**Restore:**

```bash
# Restore banco de dados
docker exec -i postgres-orders psql -U order_user order_service_db < order_backup.sql
```

Este guia fornece todas as informações necessárias para implantar o sistema de e-commerce distribuído em três máquinas separadas. Certifique-se de testar todas as conexões e funcionalidades antes de usar em produção.
