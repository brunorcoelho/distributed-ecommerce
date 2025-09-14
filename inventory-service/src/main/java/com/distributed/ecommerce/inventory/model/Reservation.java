package com.distributed.ecommerce.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservations")
public class Reservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Order ID is required")
    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status = ReservationStatus.ACTIVE;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReservationItem> items = new ArrayList<>();
    
    // Constructors
    public Reservation() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Reservation(Long orderId) {
        this();
        this.orderId = orderId;
    }
    
    // Lifecycle methods
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    public void addItem(ReservationItem item) {
        items.add(item);
        item.setReservation(this);
    }
    
    public void removeItem(ReservationItem item) {
        items.remove(item);
        item.setReservation(null);
    }
    
    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void release() {
        this.status = ReservationStatus.RELEASED;
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public ReservationStatus getStatus() {
        return status;
    }
    
    public void setStatus(ReservationStatus status) {
        this.status = status;
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
    
    public List<ReservationItem> getItems() {
        return items;
    }
    
    public void setItems(List<ReservationItem> items) {
        this.items = items;
    }
    
    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", itemsCount=" + items.size() +
                '}';
    }
}
