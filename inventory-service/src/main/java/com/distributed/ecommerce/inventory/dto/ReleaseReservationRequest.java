package com.distributed.ecommerce.inventory.dto;

import jakarta.validation.constraints.NotNull;

public class ReleaseReservationRequest {
    
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    // Constructors
    public ReleaseReservationRequest() {}
    
    public ReleaseReservationRequest(Long orderId) {
        this.orderId = orderId;
    }
    
    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    @Override
    public String toString() {
        return "ReleaseReservationRequest{" +
                "orderId=" + orderId +
                '}';
    }
}
