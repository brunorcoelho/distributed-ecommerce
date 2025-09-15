import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { CartItem } from "@/types/product";
import { Minus, Plus, Trash2, ShoppingBag } from "lucide-react";

interface CartProps {
  items: CartItem[];
  onUpdateQuantity: (id: string, quantity: number) => void;
  onRemoveItem: (id: string) => void;
  onCheckout: () => void;
}

export const Cart = ({ items, onUpdateQuantity, onRemoveItem, onCheckout }: CartProps) => {
  const total = items.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  const totalItems = items.reduce((sum, item) => sum + item.quantity, 0);

  if (items.length === 0) {
    return (
      <Card>
        <CardContent className="flex flex-col items-center justify-center py-8">
          <ShoppingBag className="h-12 w-12 text-muted-foreground mb-4" />
          <p className="text-lg text-muted-foreground">Seu carrinho está vazio</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center justify-between">
          Carrinho de Compras
          <Badge variant="secondary">{totalItems} {totalItems === 1 ? 'item' : 'itens'}</Badge>
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        {items.map(item => (
          <div key={item.id} className="flex items-center justify-between p-4 border rounded-lg">
            <div className="flex items-center space-x-4">
              <img 
                src={item.image} 
                alt={item.name}
                className="w-16 h-16 object-cover rounded"
              />
              <div>
                <h3 className="font-medium">{item.name}</h3>
                <p className="text-sm text-muted-foreground">R$ {item.price.toFixed(2)}</p>
              </div>
            </div>
            
            <div className="flex items-center space-x-2">
              <Button
                variant="outline"
                size="icon"
                onClick={() => onUpdateQuantity(item.id, Math.max(0, item.quantity - 1))}
              >
                <Minus className="h-4 w-4" />
              </Button>
              
              <span className="w-8 text-center">{item.quantity}</span>
              
              <Button
                variant="outline"
                size="icon"
                onClick={() => onUpdateQuantity(item.id, item.quantity + 1)}
              >
                <Plus className="h-4 w-4" />
              </Button>
              
              <Button
                variant="destructive"
                size="icon"
                onClick={() => onRemoveItem(item.id)}
              >
                <Trash2 className="h-4 w-4" />
              </Button>
            </div>
          </div>
        ))}
        
        <div className="border-t pt-4">
          <div className="flex justify-between items-center text-xl font-bold">
            <span>Total:</span>
            <span>R$ {total.toFixed(2)}</span>
          </div>
        </div>
        
        <Button onClick={onCheckout} className="w-full" size="lg">
          Finalizar Compra
        </Button>
      </CardContent>
    </Card>
  );
};