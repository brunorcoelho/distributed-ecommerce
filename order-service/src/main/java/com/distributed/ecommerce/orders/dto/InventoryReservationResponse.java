package com.distributed.ecommerce.orders.dto;

public class InventoryReservationResponse {
    
    private boolean success;
    private String message;
    private Long reservationId;
    
    // Constructors
    public InventoryReservationResponse() {}
    
    public InventoryReservationResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public InventoryReservationResponse(boolean success, String message, Long reservationId) {
        this.success = success;
        this.message = message;
        this.reservationId = reservationId;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Long getReservationId() {
        return reservationId;
    }
    
    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }
    
    @Override
    public String toString() {
        return "InventoryReservationResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", reservationId=" + reservationId +
                '}';
    }
}
