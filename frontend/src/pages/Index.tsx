import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { ProductCatalog } from "@/components/ProductCatalog";
import { Cart } from "@/components/Cart";
import { Checkout } from "@/components/Checkout";
import { OrderConfirmation } from "@/components/OrderConfirmation";
import { Product, CartItem, Order, CustomerInfo, ApiOrderRequest } from "@/types/product";
import type { ApiOrder } from "@/services/api";
import { ShoppingCart, ArrowLeft, AlertTriangle, Wifi, WifiOff } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
// API functions defined locally to avoid import issues
const healthCheck = async () => {
  try {
    const [orderResponse, inventoryResponse] = await Promise.allSettled([
      fetch(`${import.meta.env.VITE_ORDER_SERVICE_URL}/api/orders/health`),
      fetch(`${import.meta.env.VITE_INVENTORY_SERVICE_URL}/api/inventory/health`)
    ]);

    return {
      orderService: orderResponse.status === 'fulfilled' && orderResponse.value.ok,
      inventoryService: inventoryResponse.status === 'fulfilled' && inventoryResponse.value.ok,
      overall: orderResponse.status === 'fulfilled' && orderResponse.value.ok && 
               inventoryResponse.status === 'fulfilled' && inventoryResponse.value.ok
    };
  } catch (error) {
    return {
      orderService: false,
      inventoryService: false,
      overall: false
    };
  }
};

const inventoryApi = {
  getProducts: async (): Promise<Product[]> => {
    try {
      const response = await fetch(`${import.meta.env.VITE_INVENTORY_SERVICE_URL}/api/inventory/products`);
      if (!response.ok) throw new Error('Network response was not ok');
      const data = await response.json();
      return data.map((apiProduct: any) => ({
        id: apiProduct.id.toString(),
        name: apiProduct.name,
        price: apiProduct.price,
        description: apiProduct.description,
        image: '/placeholder.svg',
        category: 'Electronics',
        stock: apiProduct.availableQuantity || apiProduct.quantity || 0
      }));
    } catch (error) {
      console.error('Failed to fetch products:', error);
      throw new Error('Failed to load products from inventory service');
    }
  }
};

const orderApi = {
  createOrder: async (orderData: ApiOrderRequest) => {
    try {
      const response = await fetch(`${import.meta.env.VITE_ORDER_SERVICE_URL}/api/orders`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(orderData),
      });
      if (!response.ok) throw new Error('Network response was not ok');
      return await response.json();
    } catch (error) {
      console.error('Failed to create order:', error);
      throw new Error('Failed to create order');
    }
  }
};
import { Alert, AlertDescription } from "@/components/ui/alert";

const Index = () => {
  const { toast } = useToast();
  const [view, setView] = useState<'catalog' | 'cart' | 'checkout' | 'confirmation'>('catalog');
  const [cart, setCart] = useState<CartItem[]>([]);
  const [currentOrder, setCurrentOrder] = useState<Order | null>(null);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [servicesHealth, setServicesHealth] = useState({ 
    orderService: false, 
    inventoryService: false, 
    overall: false 
  });

  // Load products and check health on component mount
  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    
    try {
      // Check services health
      const health = await healthCheck();
      setServicesHealth(health);
      
      if (health.inventoryService) {
        // Load products from inventory service
        const productsData = await inventoryApi.getProducts();
        setProducts(productsData);
      } else {
        throw new Error('Inventory service is not available');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load data');
      toast({
        title: "Error",
        description: "Failed to load products. Please check if the services are running.",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const addToCart = (product: Product) => {
    if (product.stock <= 0) {
      toast({
        title: "Out of Stock",
        description: `${product.name} is currently out of stock.`,
        variant: "destructive",
      });
      return;
    }

    setCart(prev => {
      const existingItem = prev.find(item => item.id === product.id);
      
      if (existingItem) {
        const newQuantity = existingItem.quantity + 1;
        if (newQuantity > product.stock) {
          toast({
            title: "Stock Limit",
            description: `Only ${product.stock} units available.`,
            variant: "destructive",
          });
          return prev;
        }
        
        return prev.map(item =>
          item.id === product.id 
            ? { ...item, quantity: newQuantity }
            : item
        );
      } else {
        return [...prev, { ...product, quantity: 1 }];
      }
    });

    toast({
      title: "Added to Cart",
      description: `${product.name} added to cart.`,
    });
  };

  const updateQuantity = (productId: string, quantity: number) => {
    if (quantity <= 0) {
      removeFromCart(productId);
      return;
    }

    const product = products.find(p => p.id === productId);
    if (product && quantity > product.stock) {
      toast({
        title: "Stock Limit",
        description: `Only ${product.stock} units available.`,
        variant: "destructive",
      });
      return;
    }

    setCart(prev =>
      prev.map(item =>
        item.id === productId ? { ...item, quantity } : item
      )
    );
  };

  const removeFromCart = (productId: string) => {
    setCart(prev => prev.filter(item => item.id !== productId));
    const product = products.find(p => p.id === productId);
    if (product) {
      toast({
        title: "Removed from Cart",
        description: `${product.name} removed from cart.`,
      });
    }
  };

  const handleCheckout = async (customerInfo: CustomerInfo) => {
    if (!servicesHealth.orderService) {
      toast({
        title: "Service Unavailable",
        description: "Order service is not available. Please try again later.",
        variant: "destructive",
      });
      return;
    }

    try {
      const orderRequest: ApiOrderRequest = {
        customerName: customerInfo.name,
        customerEmail: customerInfo.email,
        customerAddress: `${customerInfo.address.street}, ${customerInfo.address.city}, ${customerInfo.address.state} ${customerInfo.address.zipCode}`,
        items: cart.map(item => ({
          productId: parseInt(item.id),
          productName: item.name,
          quantity: item.quantity,
          price: item.price
        })),
        totalAmount: cart.reduce((sum, item) => sum + (item.price * item.quantity), 0)
      };

      const createdOrder = await orderApi.createOrder(orderRequest);
      
      const order: Order = {
        id: createdOrder.id.toString(),
        items: cart,
        total: createdOrder.totalAmount,
        customerInfo,
        status: 'confirmed',
        createdAt: createdOrder.createdAt
      };

      setCurrentOrder(order);
      setCart([]);
      setView('confirmation');
      
      toast({
        title: "Order Confirmed",
        description: `Order #${createdOrder.id} has been confirmed!`,
      });

      // Refresh products to update stock
      await loadData();
    } catch (err) {
      toast({
        title: "Order Failed",
        description: "Failed to create order. Please try again.",
        variant: "destructive",
      });
    }
  };

  const getTotalItems = () => {
    return cart.reduce((sum, item) => sum + item.quantity, 0);
  };

  const getTotalPrice = () => {
    return cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-cyan-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto mb-4"></div>
          <p className="text-lg text-gray-600">Loading products...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-cyan-50">
      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-gray-800 mb-2">
            Sistema de E-commerce Distribuído
          </h1>
          <p className="text-lg text-gray-600">
            Microserviços em ação: Frontend, Order Service e Inventory Service
          </p>
          
          {/* Services Status */}
          <div className="flex items-center justify-center gap-4 mt-4">
            <div className="flex items-center gap-2">
              {servicesHealth.orderService ? (
                <Wifi className="h-4 w-4 text-green-500" />
              ) : (
                <WifiOff className="h-4 w-4 text-red-500" />
              )}
              <span className={`text-sm ${servicesHealth.orderService ? 'text-green-600' : 'text-red-600'}`}>
                Order Service
              </span>
            </div>
            <div className="flex items-center gap-2">
              {servicesHealth.inventoryService ? (
                <Wifi className="h-4 w-4 text-green-500" />
              ) : (
                <WifiOff className="h-4 w-4 text-red-500" />
              )}
              <span className={`text-sm ${servicesHealth.inventoryService ? 'text-green-600' : 'text-red-600'}`}>
                Inventory Service
              </span>
            </div>
          </div>
        </div>

        {/* Error Alert */}
        {error && (
          <Alert className="mb-6 border-red-200 bg-red-50">
            <AlertTriangle className="h-4 w-4 text-red-600" />
            <AlertDescription className="text-red-800">
              {error}
              <Button 
                variant="outline" 
                size="sm" 
                className="ml-4"
                onClick={loadData}
              >
                Retry
              </Button>
            </AlertDescription>
          </Alert>
        )}

        {/* Navigation */}
        {view !== 'catalog' && (
          <div className="mb-6">
            <Button
              variant="outline"
              onClick={() => {
                if (view === 'cart') setView('catalog');
                else if (view === 'checkout') setView('cart');
                else if (view === 'confirmation') setView('catalog');
              }}
              className="flex items-center gap-2"
            >
              <ArrowLeft className="h-4 w-4" />
              Back
            </Button>
          </div>
        )}

        {/* Cart Button (shown on catalog view) */}
        {view === 'catalog' && (
          <div className="fixed top-4 right-4 z-50">
            <Button
              onClick={() => setView('cart')}
              className="relative bg-indigo-600 hover:bg-indigo-700"
            >
              <ShoppingCart className="h-5 w-5 mr-2" />
              Cart
              {getTotalItems() > 0 && (
                <Badge className="absolute -top-2 -right-2 bg-red-500">
                  {getTotalItems()}
                </Badge>
              )}
            </Button>
          </div>
        )}

        {/* Main Content */}
        {view === 'catalog' && (
          <ProductCatalog 
            products={products} 
            onAddToCart={addToCart}
          />
        )}

        {view === 'cart' && (
          <Cart
            items={cart}
            onUpdateQuantity={updateQuantity}
            onRemoveItem={removeFromCart}
            onCheckout={() => setView('checkout')}
          />
        )}

        {view === 'checkout' && (
          <Checkout
            items={cart}
            onSubmitOrder={handleCheckout}
            onBack={() => setView('cart')}
          />
        )}

        {view === 'confirmation' && currentOrder && (
          <OrderConfirmation
            order={currentOrder}
            onNewOrder={() => {
              setView('catalog');
              setCurrentOrder(null);
            }}
          />
        )}
      </div>
    </div>
  );
};

export default Index;
