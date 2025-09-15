import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Separator } from "@/components/ui/separator";
import { CartItem, CustomerInfo } from "@/types/product";
import { ArrowLeft } from "lucide-react";

interface CheckoutProps {
  items: CartItem[];
  onSubmitOrder: (customerInfo: CustomerInfo) => void;
  onBack: () => void;
}

export const Checkout = ({ items, onSubmitOrder, onBack }: CheckoutProps) => {
  const [customerInfo, setCustomerInfo] = useState<CustomerInfo>({
    name: '',
    email: '',
    phone: '',
    address: {
      street: '',
      city: '',
      zipCode: '',
      state: ''
    },
    paymentMethod: 'credit'
  });

  const total = items.reduce((sum, item) => sum + (item.price * item.quantity), 0);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmitOrder(customerInfo);
  };

  const updateCustomerInfo = (field: string, value: any) => {
    if (field.includes('.')) {
      const [parent, child] = field.split('.');
      setCustomerInfo(prev => ({
        ...prev,
        [parent]: {
          ...(prev[parent as keyof CustomerInfo] as any),
          [child]: value
        }
      }));
    } else {
      setCustomerInfo(prev => ({ ...prev, [field]: value }));
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center space-x-4">
        <Button variant="outline" onClick={onBack}>
          <ArrowLeft className="h-4 w-4 mr-2" />
          Voltar ao Carrinho
        </Button>
        <h1 className="text-3xl font-bold">Finalizar Compra</h1>
      </div>

      <div className="grid md:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle>Informações de Entrega</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="name">Nome Completo</Label>
                <Input
                  id="name"
                  value={customerInfo.name}
                  onChange={(e) => updateCustomerInfo('name', e.target.value)}
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="email">Email</Label>
                <Input
                  id="email"
                  type="email"
                  value={customerInfo.email}
                  onChange={(e) => updateCustomerInfo('email', e.target.value)}
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="phone">Telefone</Label>
                <Input
                  id="phone"
                  value={customerInfo.phone}
                  onChange={(e) => updateCustomerInfo('phone', e.target.value)}
                  required
                />
              </div>

              <Separator />

              <div className="space-y-2">
                <Label htmlFor="street">Endereço</Label>
                <Input
                  id="street"
                  value={customerInfo.address.street}
                  onChange={(e) => updateCustomerInfo('address.street', e.target.value)}
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="city">Cidade</Label>
                  <Input
                    id="city"
                    value={customerInfo.address.city}
                    onChange={(e) => updateCustomerInfo('address.city', e.target.value)}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="zipCode">CEP</Label>
                  <Input
                    id="zipCode"
                    value={customerInfo.address.zipCode}
                    onChange={(e) => updateCustomerInfo('address.zipCode', e.target.value)}
                    required
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="state">Estado</Label>
                <Input
                  id="state"
                  value={customerInfo.address.state}
                  onChange={(e) => updateCustomerInfo('address.state', e.target.value)}
                  required
                />
              </div>

              <Separator />

              <div className="space-y-2">
                <Label>Método de Pagamento</Label>
                <Select
                  value={customerInfo.paymentMethod}
                  onValueChange={(value) => updateCustomerInfo('paymentMethod', value)}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="credit">Cartão de Crédito</SelectItem>
                    <SelectItem value="debit">Cartão de Débito</SelectItem>
                    <SelectItem value="pix">PIX</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <Button type="submit" className="w-full" size="lg">
                Confirmar Pedido - R$ {total.toFixed(2)}
              </Button>
            </form>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Resumo do Pedido</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {items.map(item => (
              <div key={item.id} className="flex justify-between items-center">
                <div className="flex items-center space-x-2">
                  <img 
                    src={item.image} 
                    alt={item.name}
                    className="w-10 h-10 object-cover rounded"
                  />
                  <div>
                    <p className="font-medium">{item.name}</p>
                    <p className="text-sm text-muted-foreground">
                      {item.quantity}x R$ {item.price.toFixed(2)}
                    </p>
                  </div>
                </div>
                <span className="font-medium">
                  R$ {(item.price * item.quantity).toFixed(2)}
                </span>
              </div>
            ))}
            
            <Separator />
            
            <div className="flex justify-between items-center text-xl font-bold">
              <span>Total:</span>
              <span>R$ {total.toFixed(2)}</span>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};