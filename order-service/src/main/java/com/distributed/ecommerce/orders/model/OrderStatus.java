package com.distributed.ecommerce.orders.model;

public enum OrderStatus {
    PENDENTE("Pedido pendente de processamento"),
    APROVADO("Pedido aprovado e estoque reservado"),
    CANCELADO("Pedido cancelado por falta de estoque"),
    FALHOU("Falha no processamento do pedido");
    
    private final String description;
    
    OrderStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
