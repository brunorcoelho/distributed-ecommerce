package com.distributed.ecommerce.inventory.controller;

import com.distributed.ecommerce.inventory.dto.*;
import com.distributed.ecommerce.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);
    
    @Autowired
    private InventoryService inventoryService;
    
    /**
     * Reserves inventory for a given order.
     * 
     * @param reservationRequest the reservation request
     * @return ResponseEntity with reservation result
     */
    @PostMapping("/reserve")
    public ResponseEntity<?> reserveInventory(@Valid @RequestBody ReservationRequest reservationRequest) {
        logger.info("Received inventory reservation request for order: {}", reservationRequest.getOrderId());
        logger.debug("Reservation request details: {}", reservationRequest);
        
        try {
            ReservationResponse response = inventoryService.reserveInventory(reservationRequest);
            
            if (response.isSuccess()) {
                logger.info("Inventory reservation successful for order: {}", reservationRequest.getOrderId());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Inventory reservation failed for order: {} - {}", 
                           reservationRequest.getOrderId(), response.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error during inventory reservation for order {}: {}", 
                        reservationRequest.getOrderId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ReservationResponse(false, "Internal server error during reservation"));
        }
    }
    
    /**
     * Releases a reservation, making stock available again.
     * 
     * @param releaseRequest the release request
     * @return ResponseEntity with release result
     */
    @PostMapping("/release")
    public ResponseEntity<?> releaseReservation(@Valid @RequestBody ReleaseReservationRequest releaseRequest) {
        logger.info("Received reservation release request for order: {}", releaseRequest.getOrderId());
        
        try {
            ReservationResponse response = inventoryService.releaseReservation(releaseRequest);
            
            if (response.isSuccess()) {
                logger.info("Reservation release successful for order: {}", releaseRequest.getOrderId());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Reservation release failed for order: {} - {}", 
                           releaseRequest.getOrderId(), response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error during reservation release for order {}: {}", 
                        releaseRequest.getOrderId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ReservationResponse(false, "Internal server error during release"));
        }
    }
    
    /**
     * Confirms a reservation, permanently reducing inventory.
     * 
     * @param orderId the order ID
     * @return ResponseEntity with confirmation result
     */
    @PostMapping("/confirm/{orderId}")
    public ResponseEntity<?> confirmReservation(@PathVariable Long orderId) {
        logger.info("Received reservation confirmation request for order: {}", orderId);
        
        try {
            ReservationResponse response = inventoryService.confirmReservation(orderId);
            
            if (response.isSuccess()) {
                logger.info("Reservation confirmation successful for order: {}", orderId);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Reservation confirmation failed for order: {} - {}", orderId, response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error during reservation confirmation for order {}: {}", 
                        orderId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ReservationResponse(false, "Internal server error during confirmation"));
        }
    }
    
    /**
     * Gets all products with inventory information.
     * 
     * @return ResponseEntity with list of products
     */
    @GetMapping("/products")
    public ResponseEntity<?> getAllProducts() {
        logger.debug("Received request to get all products");
        
        try {
            List<ProductResponse> products = inventoryService.getAllProducts();
            return ResponseEntity.ok(products);
            
        } catch (Exception e) {
            logger.error("Error retrieving products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal server error while retrieving products"));
        }
    }
    
    /**
     * Gets a specific product by ID.
     * 
     * @param productId the product ID
     * @return ResponseEntity with product information
     */
    @GetMapping("/products/{productId}")
    public ResponseEntity<?> getProductById(@PathVariable Long productId) {
        logger.debug("Received request to get product with ID: {}", productId);
        
        try {
            Optional<ProductResponse> productOpt = inventoryService.getProductById(productId);
            
            if (productOpt.isPresent()) {
                return ResponseEntity.ok(productOpt.get());
            } else {
                logger.warn("Product not found with ID: {}", productId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Product not found with ID: " + productId));
            }
            
        } catch (Exception e) {
            logger.error("Error retrieving product {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal server error while retrieving product"));
        }
    }
    
    /**
     * Gets inventory statistics.
     * 
     * @return ResponseEntity with inventory statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getInventoryStatistics() {
        logger.debug("Received request for inventory statistics");
        
        try {
            InventoryService.InventoryStatistics statistics = inventoryService.getInventoryStatistics();
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            logger.error("Error retrieving inventory statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal server error while retrieving statistics"));
        }
    }
    
    /**
     * Health check endpoint.
     * 
     * @return ResponseEntity indicating service health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "inventory-service",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}
