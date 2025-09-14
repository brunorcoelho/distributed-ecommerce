package com.distributed.ecommerce.orders.dto;

public class InventoryItemRequest {
    
    private Long productId;
    private Integer quantity;
    
    // Constructors
    public InventoryItemRequest() {}
    
    public InventoryItemRequest(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
    
    // Getters and Setters
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    @Override
    public String toString() {
        return "InventoryItemRequest{" +
                "productId=" + productId +
                ", quantity=" + quantity +
                '}';
    }
}
