# Order Service - Sistema de E-commerce Distribuído

Este é o microserviço de gerenciamento de pedidos do sistema de e-commerce distribuído.

## Responsabilidades

- Receber e validar pedidos do frontend
- Orquestrar o ciclo de vida dos pedidos
- Comunicar com o Inventory Service para reserva de estoque
- Gerenciar estados dos pedidos (PENDENTE, APROVADO, CANCELADO, FALHOU)
- Persistir dados dos pedidos em PostgreSQL

## Tecnologias

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- PostgreSQL
- Spring WebFlux (para comunicação HTTP)
- Maven

## Configuração

### Banco de Dados

O serviço requer uma instância PostgreSQL. Configure no `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/order_service_db
spring.datasource.username=order_user
spring.datasource.password=order_password
```

### Inventory Service

Configure a URL do Inventory Service:

```properties
inventory.service.url=http://192.168.1.102:8081
```

### CORS

Configure as origens permitidas para o frontend:

```properties
cors.allowed-origins=http://localhost:3000,http://192.168.1.101:3000
```

## Endpoints da API

### Criar Pedido
- **POST** `/api/orders`
- **Payload**:
```json
{
  "customerName": "João Silva",
  "customerEmail": "joao@email.com",
  "customerAddress": "Rua das Flores, 123",
  "customerPhone": "(65) 99999-9999",
  "items": [
    {
      "productId": 1,
      "productName": "Smartphone Galaxy",
      "quantity": 2,
      "price": 899.99
    }
  ],
  "totalAmount": 1799.98
}
```

### Consultar Pedido
- **GET** `/api/orders/{orderId}`

### Listar Pedidos
- **GET** `/api/orders?customerEmail=joao@email.com`
- **GET** `/api/orders?status=APROVADO`

### Estatísticas
- **GET** `/api/orders/statistics`

### Health Check
- **GET** `/api/orders/health`

## Fluxo de Processamento

1. Recebe pedido via POST /api/orders
2. Valida dados do pedido
3. Persiste pedido com status PENDENTE
4. Chama Inventory Service para reservar estoque
5. Atualiza status baseado na resposta:
   - **APROVADO**: Estoque reservado com sucesso
   - **CANCELADO**: Estoque insuficiente
   - **FALHOU**: Erro de comunicação ou sistema

## Estados do Pedido

- **PENDENTE**: Pedido criado, aguardando processamento
- **APROVADO**: Pedido aprovado, estoque reservado
- **CANCELADO**: Pedido cancelado por falta de estoque
- **FALHOU**: Falha no processamento (erro de sistema)

## Build e Execução

### Desenvolvimento Local

```bash
# Instalar dependências e compilar
mvn clean install

# Executar aplicação
mvn spring-boot:run
```

### Docker

```bash
# Build da imagem
docker build -t order-service .

# Executar container
docker run -d \
  --name order-service \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://192.168.1.100:5432/order_service_db \
  -e INVENTORY_SERVICE_URL=http://192.168.1.102:8081 \
  -e CORS_ALLOWED_ORIGINS=http://192.168.1.101:3000 \
  order-service
```

## Configuração de Rede

Para deploy em máquina separada:

1. **PostgreSQL**: Instalar e configurar PostgreSQL
2. **Firewall**: Liberar porta 8080
3. **Inventory Service**: Configurar URL do Inventory Service
4. **Frontend**: Configurar CORS para permitir requisições do frontend

## Estrutura do Projeto

```
order-service/
├── src/main/java/com/distributed/ecommerce/orders/
│   ├── OrderServiceApplication.java    # Aplicação principal
│   ├── config/                         # Configurações
│   ├── controller/                     # Controllers REST
│   ├── dto/                           # Data Transfer Objects
│   ├── model/                         # Entidades JPA
│   ├── repository/                    # Repositórios
│   └── service/                       # Lógica de negócio
├── src/main/resources/
│   └── application.properties         # Configurações
├── pom.xml                           # Dependências Maven
├── Dockerfile                        # Configuração Docker
└── README.md                         # Este arquivo
```

## Monitoramento

O serviço oferece endpoints para monitoramento:

- `/api/orders/health` - Status do serviço
- `/api/orders/statistics` - Estatísticas dos pedidos

## Tratamento de Erros

- **400 Bad Request**: Dados inválidos no pedido
- **409 Conflict**: Estoque insuficiente
- **503 Service Unavailable**: Inventory Service indisponível
- **500 Internal Server Error**: Erro interno do sistema

## Logs

Logs detalhados são gerados para:
- Criação de pedidos
- Comunicação com Inventory Service
- Mudanças de status dos pedidos
- Erros e exceções

Configure o nível de log no `application.properties`:

```properties
logging.level.com.distributed.ecommerce=DEBUG
```
