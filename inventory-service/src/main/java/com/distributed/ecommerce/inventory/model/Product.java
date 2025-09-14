package com.distributed.ecommerce.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Product name is required")
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @NotNull(message = "Price is required")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @NotNull(message = "Quantity is required")
    @PositiveOrZero(message = "Quantity must be zero or positive")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @PositiveOrZero(message = "Reserved quantity must be zero or positive")
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Product() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Product(String name, String description, BigDecimal price, Integer quantity) {
        this();
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }
    
    // Lifecycle methods
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    public Integer getAvailableQuantity() {
        return quantity - reservedQuantity;
    }
    
    public boolean hasAvailableStock(Integer requestedQuantity) {
        return getAvailableQuantity() >= requestedQuantity;
    }
    
    public boolean reserveStock(Integer requestedQuantity) {
        if (hasAvailableStock(requestedQuantity)) {
            this.reservedQuantity += requestedQuantity;
            this.updatedAt = LocalDateTime.now();
            return true;
        }
        return false;
    }
    
    public void releaseReservation(Integer reservedAmount) {
        this.reservedQuantity = Math.max(0, this.reservedQuantity - reservedAmount);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void confirmReservation(Integer confirmedAmount) {
        int amountToDeduct = Math.min(confirmedAmount, reservedQuantity);
        this.quantity -= amountToDeduct;
        this.reservedQuantity -= amountToDeduct;
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public Integer getReservedQuantity() {
        return reservedQuantity;
    }
    
    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", reservedQuantity=" + reservedQuantity +
                ", availableQuantity=" + getAvailableQuantity() +
                '}';
    }
}
