package com.distributed.ecommerce.orders.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

public class CreateOrderRequest {
    
    @NotBlank(message = "Customer name is required")
    private String customerName;
    
    @NotBlank(message = "Customer email is required")
    private String customerEmail;
    
    @NotBlank(message = "Customer address is required")
    private String customerAddress;
    
    private String customerPhone;
    
    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<OrderItemDto> items;
    
    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    private BigDecimal totalAmount;
    
    // Constructors
    public CreateOrderRequest() {}
    
    public CreateOrderRequest(String customerName, String customerEmail, String customerAddress,
                             String customerPhone, List<OrderItemDto> items, BigDecimal totalAmount) {
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerAddress = customerAddress;
        this.customerPhone = customerPhone;
        this.items = items;
        this.totalAmount = totalAmount;
    }
    
    // Getters and Setters
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getCustomerEmail() {
        return customerEmail;
    }
    
    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
    
    public String getCustomerAddress() {
        return customerAddress;
    }
    
    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }
    
    public String getCustomerPhone() {
        return customerPhone;
    }
    
    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
    
    public List<OrderItemDto> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItemDto> items) {
        this.items = items;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    @Override
    public String toString() {
        return "CreateOrderRequest{" +
                "customerName='" + customerName + '\'' +
                ", customerEmail='" + customerEmail + '\'' +
                ", customerAddress='" + customerAddress + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", items=" + items +
                ", totalAmount=" + totalAmount +
                '}';
    }
}
