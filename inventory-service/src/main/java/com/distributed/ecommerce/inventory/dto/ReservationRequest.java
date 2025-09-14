package com.distributed.ecommerce.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ReservationRequest {
    
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    private List<ReservationItemRequest> items;
    
    // Constructors
    public ReservationRequest() {}
    
    public ReservationRequest(Long orderId, List<ReservationItemRequest> items) {
        this.orderId = orderId;
        this.items = items;
    }
    
    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public List<ReservationItemRequest> getItems() {
        return items;
    }
    
    public void setItems(List<ReservationItemRequest> items) {
        this.items = items;
    }
    
    @Override
    public String toString() {
        return "ReservationRequest{" +
                "orderId=" + orderId +
                ", items=" + items +
                '}';
    }
}
