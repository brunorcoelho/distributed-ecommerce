package com.distributed.ecommerce.orders.service;

import com.distributed.ecommerce.orders.dto.InventoryReservationRequest;
import com.distributed.ecommerce.orders.dto.InventoryReservationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class InventoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    
    private final WebClient webClient;
    
    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;
    
    @Value("${inventory.service.timeout:30000}")
    private int timeoutMillis;
    
    public InventoryService() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }
    
    /**
     * Attempts to reserve inventory for the given order items.
     * 
     * @param reservationRequest the reservation request containing order details and items
     * @return InventoryReservationResponse indicating success or failure
     */
    public InventoryReservationResponse reserveInventory(InventoryReservationRequest reservationRequest) {
        logger.info("Attempting to reserve inventory for order {}: {}", 
                   reservationRequest.getOrderId(), reservationRequest);
        
        try {
            InventoryReservationResponse response = webClient
                    .post()
                    .uri(inventoryServiceUrl + "/api/inventory/reserve")
                    .bodyValue(reservationRequest)
                    .retrieve()
                    .bodyToMono(InventoryReservationResponse.class)
                    .timeout(Duration.ofMillis(timeoutMillis))
                    .block();
            
            logger.info("Inventory reservation response for order {}: {}", 
                       reservationRequest.getOrderId(), response);
            
            return response != null ? response : new InventoryReservationResponse(false, "Empty response from inventory service");
            
        } catch (WebClientResponseException e) {
            logger.error("HTTP error while reserving inventory for order {}: Status={}, Body={}", 
                        reservationRequest.getOrderId(), e.getStatusCode(), e.getResponseBodyAsString());
            
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                return new InventoryReservationResponse(false, "Insufficient stock for one or more items");
            } else if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                return new InventoryReservationResponse(false, "Inventory service temporarily unavailable");
            } else {
                return new InventoryReservationResponse(false, "Error communicating with inventory service: " + e.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error while reserving inventory for order {}: {}", 
                        reservationRequest.getOrderId(), e.getMessage(), e);
            return new InventoryReservationResponse(false, "Failed to communicate with inventory service: " + e.getMessage());
        }
    }
    
    /**
     * Releases a previously made inventory reservation.
     * This is typically called when an order needs to be cancelled or rolled back.
     * 
     * @param orderId the order ID for which to release the reservation
     * @return true if successful, false otherwise
     */
    public boolean releaseInventoryReservation(Long orderId) {
        logger.info("Attempting to release inventory reservation for order {}", orderId);
        
        try {
            String response = webClient
                    .post()
                    .uri(inventoryServiceUrl + "/api/inventory/release")
                    .bodyValue(new ReleaseReservationRequest(orderId))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(timeoutMillis))
                    .block();
            
            logger.info("Inventory release response for order {}: {}", orderId, response);
            return true;
            
        } catch (Exception e) {
            logger.error("Error while releasing inventory reservation for order {}: {}", orderId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Checks if the inventory service is available.
     * 
     * @return true if the service is reachable, false otherwise
     */
    public boolean isInventoryServiceAvailable() {
        try {
            String response = webClient
                    .get()
                    .uri(inventoryServiceUrl + "/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(5000))
                    .block();
            
            return response != null;
            
        } catch (Exception e) {
            logger.warn("Inventory service health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    // Inner class for release reservation request
    private static class ReleaseReservationRequest {
        private Long orderId;
        
        public ReleaseReservationRequest(Long orderId) {
            this.orderId = orderId;
        }
        
        public Long getOrderId() {
            return orderId;
        }
        
        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }
    }
}
