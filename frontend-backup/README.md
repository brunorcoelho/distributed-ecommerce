# Frontend - Sistema de E-commerce Distribuído

Esta é a aplicação frontend em React.js do sistema de e-commerce distribuído.

## Funcionalidades

- Catálogo de produtos com informações detalhadas
- Carrinho de compras com adição/remoção de itens
- Formulário de checkout com dados do cliente
- Comunicação com o Order Service via REST API
- Interface responsiva e moderna

## Tecnologias

- React.js 18
- Axios (para requisições HTTP)
- CSS3 (estilização)
- HTML5

## Configuração

### Variáveis de Ambiente

Crie um arquivo `.env` na raiz do diretório frontend:

```
REACT_APP_ORDER_SERVICE_URL=http://IP_DA_MAQUINA_ORDER_SERVICE:8080
```

Substitua `IP_DA_MAQUINA_ORDER_SERVICE` pelo IP real da máquina que está executando o Order Service.

### Desenvolvimento Local

1. Instalar dependências:
```bash
npm install
```

2. Executar em modo de desenvolvimento:
```bash
npm start
```

O frontend estará disponível em `http://localhost:3000`

### Build para Produção

```bash
npm run build
```

## Deploy com Docker

### 1. Build da Imagem Docker

```bash
docker build -t ecommerce-frontend .
```

### 2. Executar Container

```bash
docker run -d \
  --name ecommerce-frontend \
  -p 3000:3000 \
  -e REACT_APP_ORDER_SERVICE_URL=http://IP_ORDER_SERVICE:8080 \
  ecommerce-frontend
```

### 3. Deploy em Máquina Separada

Para deploy em uma máquina dedicada:

1. Copie o diretório `frontend/` para a máquina
2. Configure as variáveis de ambiente
3. Execute os comandos Docker acima
4. Certifique-se de que a porta 3000 está liberada no firewall

## Configuração de Rede

Para que o frontend se comunique com o Order Service em outra máquina:

1. **Configure o IP do Order Service** no arquivo `.env` ou variável de ambiente
2. **Liberação de Portas**: Certifique-se de que a porta 8080 (Order Service) está acessível
3. **CORS**: O Order Service deve permitir requisições do IP da máquina do frontend

## Estrutura do Projeto

```
frontend/
├── public/
│   └── index.html          # HTML principal
├── src/
│   ├── App.js             # Componente principal
│   ├── index.js           # Entry point
│   └── index.css          # Estilos globais
├── package.json           # Dependências e scripts
├── Dockerfile            # Configuração Docker
└── README.md             # Este arquivo
```

## Fluxo de Uso

1. **Navegação**: Usuário visualiza catálogo de produtos
2. **Carrinho**: Adiciona produtos ao carrinho
3. **Checkout**: Preenche dados pessoais
4. **Pedido**: Clica em "Finalizar Compra"
5. **Processamento**: Frontend envia dados para Order Service
6. **Resultado**: Exibe confirmação ou erro do pedido

## Comunicação com Backend

A aplicação se comunica com o Order Service através de:

- **Endpoint**: `POST /api/orders`
- **Formato**: JSON
- **Protocolo**: HTTP sobre TCP
- **Timeout**: 30 segundos

### Exemplo de Payload

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

## Troubleshooting

### Problema: Não consegue conectar ao Order Service

**Solução**:
1. Verifique se o Order Service está rodando
2. Confirme o IP e porta no arquivo `.env`
3. Teste conectividade: `telnet IP_ORDER_SERVICE 8080`
4. Verifique configuração de CORS no backend

### Problema: Erro 409 (Conflict) no checkout

**Causa**: Produto indisponível em estoque
**Solução**: Erro esperado quando não há estoque suficiente

### Problema: Erro 503 (Service Unavailable)

**Causa**: Inventory Service indisponível
**Solução**: Verificar se o Inventory Service está funcionando
