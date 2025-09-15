import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import { Order } from "@/types/product";
import { CheckCircle, XCircle, Package, Home } from "lucide-react";

interface OrderConfirmationProps {
  order: Order;
  onNewOrder: () => void;
}

export const OrderConfirmation = ({ order, onNewOrder }: OrderConfirmationProps) => {
  const isConfirmed = order.status === 'confirmed';

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <Card>
        <CardHeader className="text-center">
          <div className="flex justify-center mb-4">
            {isConfirmed ? (
              <CheckCircle className="h-16 w-16 text-green-500" />
            ) : (
              <XCircle className="h-16 w-16 text-destructive" />
            )}
          </div>
          <CardTitle className="text-2xl">
            {isConfirmed ? 'Pedido Confirmado!' : 'Pedido Cancelado'}
          </CardTitle>
          <div className="flex justify-center items-center space-x-2 mt-2">
            <span className="text-muted-foreground">Status:</span>
            <Badge variant={isConfirmed ? "default" : "destructive"}>
              {isConfirmed ? 'Confirmado' : 'Cancelado'}
            </Badge>
          </div>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="text-center space-y-2">
            <p className="text-lg">
              {isConfirmed 
                ? 'Seu pedido foi recebido e está sendo processado!'
                : 'Infelizmente seu pedido não pôde ser processado.'
              }
            </p>
            <p className="text-sm text-muted-foreground">
              Número do pedido: <strong>{order.id}</strong>
            </p>
            <p className="text-sm text-muted-foreground">
              Data: {order.createdAt.toLocaleDateString('pt-BR')} às {order.createdAt.toLocaleTimeString('pt-BR')}
            </p>
          </div>

          <Separator />

          <div>
            <h3 className="font-semibold mb-3 flex items-center">
              <Package className="h-4 w-4 mr-2" />
              Itens do Pedido
            </h3>
            <div className="space-y-2">
              {order.items.map(item => (
                <div key={item.id} className="flex justify-between items-center p-2 bg-muted rounded">
                  <div className="flex items-center space-x-2">
                    <img 
                      src={item.image} 
                      alt={item.name}
                      className="w-8 h-8 object-cover rounded"
                    />
                    <span className="font-medium">{item.name}</span>
                    <span className="text-sm text-muted-foreground">
                      {item.quantity}x
                    </span>
                  </div>
                  <span className="font-medium">
                    R$ {(item.price * item.quantity).toFixed(2)}
                  </span>
                </div>
              ))}
            </div>
          </div>

          <Separator />

          <div className="flex justify-between items-center text-xl font-bold">
            <span>Total:</span>
            <span>R$ {order.total.toFixed(2)}</span>
          </div>

          <div className="space-y-2">
            <h3 className="font-semibold flex items-center">
              <Home className="h-4 w-4 mr-2" />
              Informações de Entrega
            </h3>
            <div className="text-sm space-y-1 text-muted-foreground">
              <p><strong>Nome:</strong> {order.customerInfo.name}</p>
              <p><strong>Email:</strong> {order.customerInfo.email}</p>
              <p><strong>Telefone:</strong> {order.customerInfo.phone}</p>
              <p><strong>Endereço:</strong> {order.customerInfo.address.street}</p>
              <p><strong>Cidade:</strong> {order.customerInfo.address.city} - {order.customerInfo.address.state}</p>
              <p><strong>CEP:</strong> {order.customerInfo.address.zipCode}</p>
              <p><strong>Pagamento:</strong> {
                order.customerInfo.paymentMethod === 'credit' ? 'Cartão de Crédito' :
                order.customerInfo.paymentMethod === 'debit' ? 'Cartão de Débito' : 'PIX'
              }</p>
            </div>
          </div>

          {isConfirmed && (
            <div className="bg-muted p-4 rounded-lg">
              <p className="text-sm text-muted-foreground">
                <strong>Próximos passos:</strong> Você receberá um email com os detalhes do seu pedido e informações de acompanhamento. O prazo de entrega é de 3-7 dias úteis.
              </p>
            </div>
          )}

          <Button onClick={onNewOrder} className="w-full" size="lg">
            Fazer Novo Pedido
          </Button>
        </CardContent>
      </Card>
    </div>
  );
};