export interface Product {
  id: string;
  name: string;
  price: number;
  description: string;
  image: string;
  category: string;
  stock: number;
}

export interface CartItem extends Product {
  quantity: number;
}

// Backend API Product (from services)
export interface ApiProduct {
  id: number;
  name: string;
  price: number;
  description: string;
  quantity: number;
  reservedQuantity: number;
  availableQuantity: number;
}

export interface Order {
  id: string;
  items: CartItem[];
  total: number;
  customerInfo: CustomerInfo;
  status: 'confirmed' | 'cancelled';
  createdAt?: string;
}

export interface CustomerInfo {
  name: string;
  email: string;
  phone: string;
  address: {
    street: string;
    city: string;
    zipCode: string;
    state: string;
  };
  paymentMethod: 'credit' | 'debit' | 'pix';
}

// Backend API types
export interface ApiOrderRequest {
  customerName: string;
  customerEmail: string;
  customerAddress: string;
  items: {
    productId: number;
    productName: string;
    quantity: number;
    price: number;
  }[];
  totalAmount: number;
}