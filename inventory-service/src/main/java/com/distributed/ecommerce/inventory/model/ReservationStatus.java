package com.distributed.ecommerce.inventory.model;

public enum ReservationStatus {
    ACTIVE("Reserva ativa"),
    CONFIRMED("Reserva confirmada e estoque baixado"),
    CANCELLED("Reserva cancelada"),
    RELEASED("Reserva liberada");
    
    private final String description;
    
    ReservationStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
