# Sistema de E-commerce Distribuído

## 📋 Visão Geral

Sistema de e-commerce distribuído baseado em microserviços, desenvolvido para execução em três máquinas separadas na mesma rede. O sistema implementa uma arquitetura robusta para gerenciamento de produtos, pedidos e estoque.

## 🏗️ Arquitetura

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Máquina 1     │    │   Máquina 2     │    │   Máquina 3     │
│                 │    │                 │    │                 │
│  ┌───────────┐  │    │  ┌───────────┐  │    │  ┌───────────┐  │
│  │ Frontend  │  │    │  │  Order    │  │    │  │Inventory  │  │
│  │ React.js  │◄─┼────┼──┤ Service   │◄─┼────┼──┤ Service   │  │
│  │   :3000   │  │    │  │   :8080   │  │    │  │   :8081   │  │
│  └───────────┘  │    │  └───────────┘  │    │  └───────────┘  │
└─────────────────┘    │  ┌───────────┐  │    │  ┌───────────┐  │
                       │  │PostgreSQL │  │    │  │PostgreSQL│  │
                       │  │   :5432   │  │    │  │   :5432   │  │
                       │  └───────────┘  │    │  └───────────┘  │
                       └─────────────────┘    └─────────────────┘
```

## 🛠️ Tecnologias

### Frontend
- **React.js 18** - Interface de usuário
- **CSS3** - Estilização responsiva
- **Docker** - Containerização

### Backend
- **Java 17** - Linguagem principal
- **Spring Boot 3.2.0** - Framework principal
- **Spring Data JPA** - Persistência
- **PostgreSQL** - Banco de dados
- **Maven** - Gerenciamento de dependências
- **Docker** - Containerização

### Comunicação
- **REST APIs** - Protocolo HTTP/JSON
- **TCP** - Protocolo de transporte
- **CORS** - Configuração para requisições cross-origin

## 📁 Estrutura do Projeto

```
distributed-ecommerce/
├── frontend/                 # Aplicação React.js
│   ├── src/
│   │   ├── App.js           # Componente principal
│   │   ├── App.css          # Estilos principais
│   │   └── index.js         # Entry point
│   ├── Dockerfile           # Container do frontend
│   └── package.json         # Dependências Node.js
│
├── order-service/           # Serviço de Pedidos (Spring Boot)
│   ├── src/main/java/
│   │   └── com/ecommerce/order/
│   │       ├── OrderServiceApplication.java
│   │       ├── controller/  # REST Controllers
│   │       ├── service/     # Lógica de negócio
│   │       ├── entity/      # Entidades JPA
│   │       ├── repository/  # Repositórios JPA
│   │       ├── dto/         # Data Transfer Objects
│   │       └── config/      # Configurações
│   ├── Dockerfile
│   └── pom.xml
│
├── inventory-service/       # Serviço de Estoque (Spring Boot)
│   ├── src/main/java/
│   │   └── com/ecommerce/inventory/
│   │       ├── InventoryServiceApplication.java
│   │       ├── controller/  # REST Controllers
│   │       ├── service/     # Lógica de negócio
│   │       ├── entity/      # Entidades JPA
│   │       ├── repository/  # Repositórios JPA
│   │       ├── dto/         # Data Transfer Objects
│   │       └── config/      # Configurações
│   ├── Dockerfile
│   └── pom.xml
│
└── deployment/              # Scripts e configurações de deploy
    ├── deploy.sh           # Script principal de deploy
    ├── network-config.sh   # Configuração de rede/firewall
    ├── docker-compose.frontend.yml
    ├── docker-compose.order-service.yml
    ├── docker-compose.inventory-service.yml
    └── DEPLOYMENT_GUIDE.md # Guia detalhado de deploy
```

## 🚀 Início Rápido

### 1. Pré-requisitos

```bash
# Instalar Docker em todas as máquinas
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Adicionar usuário ao grupo docker
sudo usermod -aG docker $USER
newgrp docker
```

### 2. Clonar o Projeto

```bash
git clone <repository-url>
cd distributed-ecommerce
```

### 3. Deploy Rápido

**Máquina 1 (Frontend):**
```bash
./deployment/deploy.sh frontend <IP_ORDER_SERVICE>
# Exemplo: ./deployment/deploy.sh frontend 192.168.1.100
```

**Máquina 2 (Order Service):**
```bash
./deployment/deploy.sh order-service <IP_INVENTORY_SERVICE> <IP_FRONTEND>
# Exemplo: ./deployment/deploy.sh order-service 192.168.1.102 192.168.1.101
```

**Máquina 3 (Inventory Service):**
```bash
./deployment/deploy.sh inventory-service
```

### 4. Verificação

**Acessar o sistema:**
- Frontend: `http://<IP_MAQUINA_1>:3000`
- Order Service: `http://<IP_MAQUINA_2>:8080/api/orders/health`
- Inventory Service: `http://<IP_MAQUINA_3>:8081/api/inventory/health`

## 📖 Funcionalidades

### Frontend (React.js)
- ✅ Catálogo de produtos responsivo
- ✅ Carrinho de compras interativo
- ✅ Processo de checkout
- ✅ Notificações de sucesso/erro
- ✅ Design responsivo mobile-first

### Order Service (Spring Boot)
- ✅ Criação e gerenciamento de pedidos
- ✅ Integração com serviço de estoque
- ✅ Validação de dados de pedidos
- ✅ Sistema de reserva de produtos
- ✅ APIs RESTful com documentação
- ✅ Tratamento de erros robusto

### Inventory Service (Spring Boot)
- ✅ Gerenciamento de produtos e estoque
- ✅ Sistema de reservas com controle de concorrência
- ✅ Operações transacionais
- ✅ Controle de bloqueio pessimista
- ✅ APIs de estatísticas e relatórios
- ✅ Health checks e monitoramento

## 🔧 APIs Disponíveis

### Order Service (Porta 8080)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/orders/health` | Health check do serviço |
| POST | `/api/orders` | Criar novo pedido |
| GET | `/api/orders/{id}` | Buscar pedido por ID |
| GET | `/api/orders` | Listar todos os pedidos |
| GET | `/api/orders/statistics` | Estatísticas de pedidos |

### Inventory Service (Porta 8081)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/inventory/health` | Health check do serviço |
| GET | `/api/inventory/products` | Listar todos os produtos |
| GET | `/api/inventory/products/{id}` | Buscar produto por ID |
| POST | `/api/inventory/reserve` | Reservar produtos |
| POST | `/api/inventory/release` | Liberar reserva |
| GET | `/api/inventory/statistics` | Estatísticas de estoque |

## 🧪 Testes

### Teste Manual do Fluxo

1. **Acessar Frontend**: `http://<IP_FRONTEND>:3000`
2. **Navegar pelos produtos**
3. **Adicionar itens ao carrinho**
4. **Preencher dados do checkout**
5. **Finalizar pedido**
6. **Verificar criação do pedido nos logs**

### Testes de API

```bash
# Teste de criação de pedido
curl -X POST http://<IP_ORDER_SERVICE>:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "João Silva",
    "customerEmail": "joao@email.com", 
    "customerAddress": "Rua das Flores, 123",
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

## 📚 Documentação

Para instruções detalhadas de deploy, consulte o **[Guia de Deploy](deployment/DEPLOYMENT_GUIDE.md)** que inclui:

- Configuração passo a passo por máquina
- Requisitos de hardware e software
- Configuração de rede e firewall
- Resolução de problemas
- Monitoramento e manutenção
- Backup e restore
- Configurações de produção

## 🤝 Desenvolvimento Local

### Rodar Tudo na Mesma Máquina (Teste)

Para testar o sistema completo em uma única máquina:

```bash
# Rodar todo o sistema com Docker Compose
cd deployment
./run-local.sh

# Para limpar e reiniciar tudo
./run-local.sh --clean
```

**Acessos locais:**
- Frontend: `http://localhost:3000`
- Order Service: `http://localhost:8080` 
- Inventory Service: `http://localhost:8081`

### Desenvolvimento Individual dos Serviços

```bash
# Frontend
cd frontend
npm install
npm start

# Order Service
cd order-service
mvn spring-boot:run

# Inventory Service  
cd inventory-service
mvn spring-boot:run
```

## 📄 Licença

Este projeto foi desenvolvido para fins educacionais como parte de um trabalho acadêmico sobre sistemas distribuídos.

---

**Sistema desenvolvido com foco em arquitetura distribuída, escalabilidade e facilidade de deploy em ambiente multi-máquina.**
./deployment/deploy.sh inventory-service
```

### 4. Verificação

**Acessar o sistema:**
- Frontend: `http://<IP_MAQUINA_1>:3000`
- Order Service: `http://<IP_MAQUINA_2>:8080/api/orders/health`
- Inventory Service: `http://<IP_MAQUINA_3>:8081/api/inventory/health`
- Node.js 18+ (para desenvolvimento local)
- PostgreSQL (ou usar containers)

## Início Rápido

Veja os READMEs específicos em cada diretório para instruções detalhadas de configuração e deployment.

## Fluxo de Negócio

1. Usuário navega pelos produtos no frontend
2. Adiciona produtos ao carrinho
3. Finaliza compra enviando pedido para Order Service
4. Order Service valida pedido e comunica com Inventory Service
5. Inventory Service verifica disponibilidade e reserva estoque
6. Order Service confirma ou cancela pedido baseado na resposta
7. Frontend exibe resultado final ao usuário
