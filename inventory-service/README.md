# Inventory Service - Sistema de E-commerce Distribuído

Este é o microserviço de gerenciamento de estoque do sistema de e-commerce distribuído.

## Responsabilidades

- Gerenciar inventário de produtos
- Processar reservas de estoque para pedidos
- Manter controle de quantidades disponíveis e reservadas
- Garantir consistência nas operações de estoque
- Fornecer APIs para consulta de produtos e estatísticas

## Tecnologias

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- PostgreSQL
- Maven

## Configuração

### Banco de Dados

O serviço requer uma instância PostgreSQL. Configure no `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/inventory_service_db
spring.datasource.username=inventory_user
spring.datasource.password=inventory_password
```

### Inicialização de Dados

O serviço pode inicializar dados de exemplo automaticamente:

```properties
inventory.initialize-sample-data=true
```

## Endpoints da API

### Reservar Estoque
- **POST** `/api/inventory/reserve`
- **Payload**:
```json
{
  "orderId": 123,
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 2,
      "quantity": 1
    }
  ]
}
```

### Liberar Reserva
- **POST** `/api/inventory/release`
- **Payload**:
```json
{
  "orderId": 123
}
```

### Confirmar Reserva
- **POST** `/api/inventory/confirm/{orderId}`

### Listar Produtos
- **GET** `/api/inventory/products`

### Consultar Produto
- **GET** `/api/inventory/products/{productId}`

### Estatísticas
- **GET** `/api/inventory/statistics`

### Health Check
- **GET** `/api/inventory/health`

## Modelo de Dados

### Products
- `id`: ID único do produto
- `name`: Nome do produto
- `description`: Descrição do produto
- `price`: Preço do produto
- `quantity`: Quantidade total em estoque
- `reserved_quantity`: Quantidade reservada
- `created_at`: Data de criação
- `updated_at`: Data da última atualização

### Reservations
- `id`: ID único da reserva
- `order_id`: ID do pedido associado
- `status`: Status da reserva (ACTIVE, CONFIRMED, CANCELLED, RELEASED)
- `created_at`: Data de criação
- `updated_at`: Data da última atualização

### Reservation_Items
- `id`: ID único do item de reserva
- `reservation_id`: ID da reserva
- `product_id`: ID do produto
- `quantity`: Quantidade reservada

## Fluxo de Operações

### Reserva de Estoque
1. Recebe requisição de reserva com ID do pedido e itens
2. Verifica se já existe reserva para o pedido
3. Para cada item:
   - Busca produto no banco (com lock pessimista)
   - Verifica disponibilidade
   - Reserva estoque se disponível
4. Se todos os itens foram reservados com sucesso:
   - Salva reserva no banco
   - Retorna sucesso
5. Se algum item não está disponível:
   - Reverte todas as reservas feitas
   - Retorna erro com detalhes

### Liberação de Reserva
1. Busca reserva pelo ID do pedido
2. Verifica se reserva está ativa
3. Para cada item da reserva:
   - Libera quantidade reservada do produto
4. Marca reserva como RELEASED

### Confirmação de Reserva
1. Busca reserva pelo ID do pedido
2. Verifica se reserva está ativa
3. Para cada item da reserva:
   - Reduz quantidade total do produto
   - Reduz quantidade reservada
4. Marca reserva como CONFIRMED

## Estados da Reserva

- **ACTIVE**: Reserva ativa, estoque reservado
- **CONFIRMED**: Reserva confirmada, estoque baixado
- **CANCELLED**: Reserva cancelada
- **RELEASED**: Reserva liberada, estoque disponível novamente

## Controle de Concorrência

O serviço utiliza:
- **Pessimistic Locking**: Locks nos produtos durante operações de reserva
- **Transações**: Garantem consistência nas operações
- **Rollback**: Revertem operações em caso de erro

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
docker build -t inventory-service .

# Executar container
docker run -d \
  --name inventory-service \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://192.168.1.100:5432/inventory_service_db \
  inventory-service
```

## Configuração de Rede

Para deploy em máquina separada:

1. **PostgreSQL**: Instalar e configurar PostgreSQL
2. **Firewall**: Liberar porta 8081
3. **Order Service**: Permitir conexões do Order Service

## Estrutura do Projeto

```
inventory-service/
├── src/main/java/com/distributed/ecommerce/inventory/
│   ├── InventoryServiceApplication.java  # Aplicação principal
│   ├── config/                          # Configurações
│   ├── controller/                      # Controllers REST
│   ├── dto/                            # Data Transfer Objects
│   ├── model/                          # Entidades JPA
│   ├── repository/                     # Repositórios
│   └── service/                        # Lógica de negócio
├── src/main/resources/
│   └── application.properties          # Configurações
├── pom.xml                            # Dependências Maven
├── Dockerfile                         # Configuração Docker
└── README.md                          # Este arquivo
```

## Produtos de Exemplo

O serviço inicializa com os seguintes produtos:

1. **Smartphone Galaxy** - R$ 899,99 (15 unidades)
2. **Notebook Gamer** - R$ 3.299,99 (8 unidades)
3. **Fone de Ouvido Bluetooth** - R$ 199,99 (25 unidades)
4. **Tablet 10 polegadas** - R$ 549,99 (12 unidades)
5. **Smart TV 55"** - R$ 1.899,99 (6 unidades)
6. **Console de Videogame** - R$ 2.499,99 (4 unidades)

## Monitoramento

O serviço oferece endpoints para monitoramento:

- `/api/inventory/health` - Status do serviço
- `/api/inventory/statistics` - Estatísticas do inventário

## Tratamento de Erros

- **400 Bad Request**: Dados inválidos na requisição
- **409 Conflict**: Estoque insuficiente para reserva
- **404 Not Found**: Produto ou reserva não encontrados
- **500 Internal Server Error**: Erro interno do sistema

## Logs

Logs detalhados são gerados para:
- Operações de reserva, liberação e confirmação
- Mudanças no estoque
- Erros e exceções
- Operações de rollback

Configure o nível de log no `application.properties`:

```properties
logging.level.com.distributed.ecommerce=DEBUG
```
