package com.distributed.ecommerce.orders.dto;

import java.util.List;

public class InventoryReservationRequest {
    
    private Long orderId;
    private List<InventoryItemRequest> items;
    
    // Constructors
    public InventoryReservationRequest() {}
    
    public InventoryReservationRequest(Long orderId, List<InventoryItemRequest> items) {
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
    
    public List<InventoryItemRequest> getItems() {
        return items;
    }
    
    public void setItems(List<InventoryItemRequest> items) {
        this.items = items;
    }
    
    @Override
    public String toString() {
        return "InventoryReservationRequest{" +
                "orderId=" + orderId +
                ", items=" + items +
                '}';
    }
}
