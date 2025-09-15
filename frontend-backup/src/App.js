import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './App.css';

// Configuração da URL base da API
const API_BASE_URL = process.env.REACT_APP_ORDER_SERVICE_URL || 'http://localhost:8080';

// Produtos mock para demonstração
const MOCK_PRODUCTS = [
  {
    id: 1,
    name: 'Smartphone Galaxy',
    description: 'Smartphone Android com 128GB de armazenamento, câmera de 48MP e tela de 6.1 polegadas.',
    price: 899.99,
    stock: 15
  },
  {
    id: 2,
    name: 'Notebook Gamer',
    description: 'Notebook para jogos com processador Intel i7, 16GB RAM, SSD 512GB e placa de vídeo RTX 3060.',
    price: 3299.99,
    stock: 8
  },
  {
    id: 3,
    name: 'Fone de Ouvido Bluetooth',
    description: 'Fone de ouvido sem fio com cancelamento de ruído, autonomia de 30 horas.',
    price: 199.99,
    stock: 25
  },
  {
    id: 4,
    name: 'Tablet 10 polegadas',
    description: 'Tablet com tela de 10 polegadas, 64GB de armazenamento e suporte a caneta stylus.',
    price: 549.99,
    stock: 12
  },
  {
    id: 5,
    name: 'Smart TV 55"',
    description: 'Smart TV LED 55 polegadas 4K com sistema Android TV e HDR.',
    price: 1899.99,
    stock: 6
  },
  {
    id: 6,
    name: 'Console de Videogame',
    description: 'Console de última geração com SSD de 1TB e suporte a jogos em 4K.',
    price: 2499.99,
    stock: 4
  }
];

function App() {
  const [products] = useState(MOCK_PRODUCTS);
  const [cart, setCart] = useState([]);
  const [customerInfo, setCustomerInfo] = useState({
    name: '',
    email: '',
    address: '',
    phone: ''
  });
  const [orderStatus, setOrderStatus] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  // Adicionar produto ao carrinho
  const addToCart = (product) => {
    setCart(prevCart => {
      const existingItem = prevCart.find(item => item.id === product.id);
      if (existingItem) {
        return prevCart.map(item =>
          item.id === product.id
            ? { ...item, quantity: item.quantity + 1 }
            : item
        );
      } else {
        return [...prevCart, { ...product, quantity: 1 }];
      }
    });
  };

  // Remover produto do carrinho
  const removeFromCart = (productId) => {
    setCart(prevCart => prevCart.filter(item => item.id !== productId));
  };

  // Atualizar quantidade no carrinho
  const updateQuantity = (productId, newQuantity) => {
    if (newQuantity <= 0) {
      removeFromCart(productId);
      return;
    }
    
    setCart(prevCart =>
      prevCart.map(item =>
        item.id === productId
          ? { ...item, quantity: newQuantity }
          : item
      )
    );
  };

  // Calcular total do carrinho
  const getCartTotal = () => {
    return cart.reduce((total, item) => total + (item.price * item.quantity), 0);
  };

  // Atualizar informações do cliente
  const handleCustomerInfoChange = (e) => {
    setCustomerInfo({
      ...customerInfo,
      [e.target.name]: e.target.value
    });
  };

  // Finalizar pedido
  const handleCheckout = async () => {
    if (cart.length === 0) {
      setOrderStatus({ type: 'error', message: 'Seu carrinho está vazio!' });
      return;
    }

    if (!customerInfo.name || !customerInfo.email || !customerInfo.address) {
      setOrderStatus({ type: 'error', message: 'Por favor, preencha todos os campos obrigatórios.' });
      return;
    }

    setIsLoading(true);
    setOrderStatus({ type: 'loading', message: 'Processando seu pedido...' });

    try {
      // Preparar dados do pedido
      const orderData = {
        customerName: customerInfo.name,
        customerEmail: customerInfo.email,
        customerAddress: customerInfo.address,
        customerPhone: customerInfo.phone,
        items: cart.map(item => ({
          productId: item.id,
          productName: item.name,
          quantity: item.quantity,
          price: item.price
        })),
        totalAmount: getCartTotal()
      };

      console.log('Enviando pedido:', orderData);

      // Enviar pedido para o Order Service
      const response = await axios.post(`${API_BASE_URL}/api/orders`, orderData, {
        headers: {
          'Content-Type': 'application/json'
        },
        timeout: 30000 // 30 segundos de timeout
      });

      console.log('Resposta do servidor:', response.data);

      if (response.status === 200 || response.status === 201) {
        setOrderStatus({
          type: 'success',
          message: `Pedido confirmado com sucesso! ID: ${response.data.id || 'N/A'}. Status: ${response.data.status || 'CONFIRMADO'}`
        });
        
        // Limpar carrinho e formulário após sucesso
        setCart([]);
        setCustomerInfo({ name: '', email: '', address: '', phone: '' });
      } else {
        throw new Error('Resposta inesperada do servidor');
      }

    } catch (error) {
      console.error('Erro ao processar pedido:', error);
      
      let errorMessage = 'Erro ao processar pedido. ';
      
      if (error.response) {
        // Erro HTTP com resposta do servidor
        errorMessage += `Status: ${error.response.status}. `;
        if (error.response.data && error.response.data.message) {
          errorMessage += error.response.data.message;
        } else if (error.response.status === 409) {
          errorMessage += 'Alguns produtos não estão disponíveis em estoque.';
        } else if (error.response.status === 503) {
          errorMessage += 'Serviço temporariamente indisponível. Tente novamente em alguns minutos.';
        } else {
          errorMessage += 'Erro interno do servidor.';
        }
      } else if (error.request) {
        // Erro de rede
        errorMessage += 'Não foi possível conectar ao servidor. Verifique sua conexão de rede.';
      } else {
        errorMessage += error.message;
      }

      setOrderStatus({ type: 'error', message: errorMessage });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="App">
      <header className="header">
        <div className="container">
          <h1>Loja Virtual Distribuída</h1>
          <p>Sistema de E-commerce com Arquitetura de Microsserviços</p>
        </div>
      </header>

      <div className="container">
        {/* Seção de Produtos */}
        <section>
          <h2>Catálogo de Produtos</h2>
          <div className="products-grid">
            {products.map(product => (
              <div key={product.id} className="product-card">
                <h3>{product.name}</h3>
                <p>{product.description}</p>
                <div className="product-price">
                  R$ {product.price.toFixed(2)}
                </div>
                <div className="product-stock">
                  Estoque: {product.stock} unidades
                </div>
                <button
                  className="btn-add-cart"
                  onClick={() => addToCart(product)}
                  disabled={product.stock === 0}
                >
                  {product.stock === 0 ? 'Fora de Estoque' : 'Adicionar ao Carrinho'}
                </button>
              </div>
            ))}
          </div>
        </section>

        {/* Seção do Carrinho */}
        <section className="cart-section">
          <h2>Carrinho de Compras</h2>
          {cart.length === 0 ? (
            <div className="empty-cart">
              Seu carrinho está vazio. Adicione alguns produtos para continuar.
            </div>
          ) : (
            <>
              <div className="cart-items">
                {cart.map(item => (
                  <div key={item.id} className="cart-item">
                    <div className="cart-item-info">
                      <h4>{item.name}</h4>
                      <span>R$ {item.price.toFixed(2)} cada</span>
                    </div>
                    <div className="cart-item-quantity">
                      <button
                        className="btn-quantity"
                        onClick={() => updateQuantity(item.id, item.quantity - 1)}
                      >
                        -
                      </button>
                      <span>{item.quantity}</span>
                      <button
                        className="btn-quantity"
                        onClick={() => updateQuantity(item.id, item.quantity + 1)}
                      >
                        +
                      </button>
                      <button
                        className="btn-quantity"
                        onClick={() => removeFromCart(item.id)}
                        style={{ marginLeft: '1rem', backgroundColor: '#e74c3c', color: 'white' }}
                      >
                        Remover
                      </button>
                    </div>
                  </div>
                ))}
              </div>
              <div className="cart-total">
                Total: R$ {getCartTotal().toFixed(2)}
              </div>
            </>
          )}
        </section>

        {/* Seção de Checkout */}
        {cart.length > 0 && (
          <section className="checkout-section">
            <h3>Informações para Entrega</h3>
            <div className="checkout-form">
              <div className="form-group">
                <label htmlFor="name">Nome Completo *</label>
                <input
                  type="text"
                  id="name"
                  name="name"
                  value={customerInfo.name}
                  onChange={handleCustomerInfoChange}
                  required
                />
              </div>
              <div className="form-group">
                <label htmlFor="email">E-mail *</label>
                <input
                  type="email"
                  id="email"
                  name="email"
                  value={customerInfo.email}
                  onChange={handleCustomerInfoChange}
                  required
                />
              </div>
              <div className="form-group">
                <label htmlFor="address">Endereço Completo *</label>
                <input
                  type="text"
                  id="address"
                  name="address"
                  value={customerInfo.address}
                  onChange={handleCustomerInfoChange}
                  required
                />
              </div>
              <div className="form-group">
                <label htmlFor="phone">Telefone</label>
                <input
                  type="tel"
                  id="phone"
                  name="phone"
                  value={customerInfo.phone}
                  onChange={handleCustomerInfoChange}
                />
              </div>
            </div>
            
            <button
              className="btn-checkout"
              onClick={handleCheckout}
              disabled={isLoading}
            >
              {isLoading ? 'Processando...' : 'Finalizar Compra'}
            </button>

            {/* Status do Pedido */}
            {orderStatus && (
              <div className={`order-status ${orderStatus.type}`}>
                {orderStatus.message}
              </div>
            )}
          </section>
        )}
      </div>
    </div>
  );
}

export default App;
