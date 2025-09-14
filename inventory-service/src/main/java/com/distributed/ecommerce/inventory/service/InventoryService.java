package com.distributed.ecommerce.inventory.service;

import com.distributed.ecommerce.inventory.dto.*;
import com.distributed.ecommerce.inventory.model.Product;
import com.distributed.ecommerce.inventory.model.Reservation;
import com.distributed.ecommerce.inventory.model.ReservationItem;
import com.distributed.ecommerce.inventory.model.ReservationStatus;
import com.distributed.ecommerce.inventory.repository.ProductRepository;
import com.distributed.ecommerce.inventory.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    /**
     * Attempts to reserve inventory for the requested items.
     * This method is transactional to ensure consistency.
     * 
     * @param reservationRequest the reservation request
     * @return ReservationResponse indicating success or failure
     */
    @Transactional
    public ReservationResponse reserveInventory(ReservationRequest reservationRequest) {
        logger.info("Processing inventory reservation for order: {}", reservationRequest.getOrderId());
        logger.debug("Reservation request details: {}", reservationRequest);
        
        // Check if there's already a reservation for this order
        Optional<Reservation> existingReservation = reservationRepository.findByOrderId(reservationRequest.getOrderId());
        if (existingReservation.isPresent()) {
            logger.warn("Reservation already exists for order: {}", reservationRequest.getOrderId());
            return new ReservationResponse(false, "Reservation already exists for this order", existingReservation.get().getId());
        }
        
        // Create new reservation
        Reservation reservation = new Reservation(reservationRequest.getOrderId());
        List<String> unavailableItems = new ArrayList<>();
        
        try {
            // Process each item in the reservation request
            for (ReservationItemRequest itemRequest : reservationRequest.getItems()) {
                logger.debug("Processing item: productId={}, quantity={}", 
                           itemRequest.getProductId(), itemRequest.getQuantity());
                
                // Find product with pessimistic lock to prevent concurrent modifications
                Optional<Product> productOpt = productRepository.findByIdForUpdate(itemRequest.getProductId());
                
                if (productOpt.isEmpty()) {
                    logger.warn("Product not found: {}", itemRequest.getProductId());
                    unavailableItems.add("Product ID " + itemRequest.getProductId() + " not found");
                    continue;
                }
                
                Product product = productOpt.get();
                logger.debug("Found product: {}", product);
                
                // Check if product has enough available stock
                if (!product.hasAvailableStock(itemRequest.getQuantity())) {
                    logger.warn("Insufficient stock for product {}: requested={}, available={}", 
                               product.getId(), itemRequest.getQuantity(), product.getAvailableQuantity());
                    unavailableItems.add(String.format("Product '%s' (ID: %d): requested %d, available %d", 
                                       product.getName(), product.getId(), 
                                       itemRequest.getQuantity(), product.getAvailableQuantity()));
                    continue;
                }
                
                // Reserve the stock
                boolean reserved = product.reserveStock(itemRequest.getQuantity());
                if (!reserved) {
                    logger.error("Failed to reserve stock for product {}: unexpected error", product.getId());
                    unavailableItems.add("Failed to reserve stock for product ID " + product.getId());
                    continue;
                }
                
                // Save the updated product
                productRepository.save(product);
                
                // Add reservation item
                ReservationItem reservationItem = new ReservationItem(itemRequest.getProductId(), itemRequest.getQuantity());
                reservation.addItem(reservationItem);
                
                logger.debug("Successfully reserved {} units of product {}", itemRequest.getQuantity(), product.getId());
            }
            
            // If any items are unavailable, rollback the entire reservation
            if (!unavailableItems.isEmpty()) {
                logger.warn("Reservation failed for order {} due to unavailable items: {}", 
                           reservationRequest.getOrderId(), unavailableItems);
                
                // Rollback: release any reservations that were made
                for (ReservationItem item : reservation.getItems()) {
                    Optional<Product> productOpt = productRepository.findById(item.getProductId());
                    if (productOpt.isPresent()) {
                        Product product = productOpt.get();
                        product.releaseReservation(item.getQuantity());
                        productRepository.save(product);
                    }
                }
                
                String errorMessage = "Some items are not available: " + String.join("; ", unavailableItems);
                return new ReservationResponse(false, errorMessage);
            }
            
            // Save the reservation
            reservation = reservationRepository.save(reservation);
            
            logger.info("Successfully created reservation {} for order {}", 
                       reservation.getId(), reservationRequest.getOrderId());
            
            return new ReservationResponse(true, "Inventory reserved successfully", reservation.getId());
            
        } catch (Exception e) {
            logger.error("Unexpected error during inventory reservation for order {}: {}", 
                        reservationRequest.getOrderId(), e.getMessage(), e);
            
            // Rollback: try to release any reservations that might have been made
            try {
                for (ReservationItem item : reservation.getItems()) {
                    Optional<Product> productOpt = productRepository.findById(item.getProductId());
                    if (productOpt.isPresent()) {
                        Product product = productOpt.get();
                        product.releaseReservation(item.getQuantity());
                        productRepository.save(product);
                    }
                }
            } catch (Exception rollbackException) {
                logger.error("Error during rollback for order {}: {}", 
                           reservationRequest.getOrderId(), rollbackException.getMessage());
            }
            
            return new ReservationResponse(false, "Internal error during reservation process");
        }
    }
    
    /**
     * Releases a reservation, making the reserved stock available again.
     * 
     * @param releaseRequest the release request
     * @return ReservationResponse indicating success or failure
     */
    @Transactional
    public ReservationResponse releaseReservation(ReleaseReservationRequest releaseRequest) {
        logger.info("Processing reservation release for order: {}", releaseRequest.getOrderId());
        
        Optional<Reservation> reservationOpt = reservationRepository.findByOrderIdWithItems(releaseRequest.getOrderId());
        
        if (reservationOpt.isEmpty()) {
            logger.warn("No reservation found for order: {}", releaseRequest.getOrderId());
            return new ReservationResponse(false, "No reservation found for this order");
        }
        
        Reservation reservation = reservationOpt.get();
        
        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            logger.warn("Reservation {} for order {} is not active (status: {})", 
                       reservation.getId(), releaseRequest.getOrderId(), reservation.getStatus());
            return new ReservationResponse(false, "Reservation is not active: " + reservation.getStatus());
        }
        
        try {
            // Release reserved stock for each item
            for (ReservationItem item : reservation.getItems()) {
                Optional<Product> productOpt = productRepository.findByIdForUpdate(item.getProductId());
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    product.releaseReservation(item.getQuantity());
                    productRepository.save(product);
                    
                    logger.debug("Released {} units of product {}", item.getQuantity(), product.getId());
                } else {
                    logger.warn("Product {} not found during reservation release", item.getProductId());
                }
            }
            
            // Update reservation status
            reservation.release();
            reservationRepository.save(reservation);
            
            logger.info("Successfully released reservation {} for order {}", 
                       reservation.getId(), releaseRequest.getOrderId());
            
            return new ReservationResponse(true, "Reservation released successfully", reservation.getId());
            
        } catch (Exception e) {
            logger.error("Error releasing reservation for order {}: {}", 
                        releaseRequest.getOrderId(), e.getMessage(), e);
            return new ReservationResponse(false, "Error releasing reservation: " + e.getMessage());
        }
    }
    
    /**
     * Confirms a reservation and permanently reduces the inventory.
     * 
     * @param orderId the order ID
     * @return ReservationResponse indicating success or failure
     */
    @Transactional
    public ReservationResponse confirmReservation(Long orderId) {
        logger.info("Processing reservation confirmation for order: {}", orderId);
        
        Optional<Reservation> reservationOpt = reservationRepository.findByOrderIdWithItems(orderId);
        
        if (reservationOpt.isEmpty()) {
            logger.warn("No reservation found for order: {}", orderId);
            return new ReservationResponse(false, "No reservation found for this order");
        }
        
        Reservation reservation = reservationOpt.get();
        
        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            logger.warn("Reservation {} for order {} is not active (status: {})", 
                       reservation.getId(), orderId, reservation.getStatus());
            return new ReservationResponse(false, "Reservation is not active: " + reservation.getStatus());
        }
        
        try {
            // Confirm reservation for each item (reduce actual inventory)
            for (ReservationItem item : reservation.getItems()) {
                Optional<Product> productOpt = productRepository.findByIdForUpdate(item.getProductId());
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    product.confirmReservation(item.getQuantity());
                    productRepository.save(product);
                    
                    logger.debug("Confirmed {} units of product {}", item.getQuantity(), product.getId());
                } else {
                    logger.warn("Product {} not found during reservation confirmation", item.getProductId());
                }
            }
            
            // Update reservation status
            reservation.confirm();
            reservationRepository.save(reservation);
            
            logger.info("Successfully confirmed reservation {} for order {}", 
                       reservation.getId(), orderId);
            
            return new ReservationResponse(true, "Reservation confirmed successfully", reservation.getId());
            
        } catch (Exception e) {
            logger.error("Error confirming reservation for order {}: {}", orderId, e.getMessage(), e);
            return new ReservationResponse(false, "Error confirming reservation: " + e.getMessage());
        }
    }
    
    /**
     * Gets all products with their inventory information.
     * 
     * @return List of ProductResponse
     */
    public List<ProductResponse> getAllProducts() {
        logger.debug("Retrieving all products");
        
        List<Product> products = productRepository.findAll();
        
        return products.stream()
                .map(this::convertToProductResponse)
                .toList();
    }
    
    /**
     * Gets a product by its ID.
     * 
     * @param productId the product ID
     * @return Optional containing ProductResponse if found
     */
    public Optional<ProductResponse> getProductById(Long productId) {
        logger.debug("Retrieving product with ID: {}", productId);
        
        Optional<Product> productOpt = productRepository.findById(productId);
        
        return productOpt.map(this::convertToProductResponse);
    }
    
    /**
     * Gets inventory statistics.
     * 
     * @return InventoryStatistics
     */
    public InventoryStatistics getInventoryStatistics() {
        logger.debug("Retrieving inventory statistics");
        
        long totalProducts = productRepository.count();
        Long totalQuantity = productRepository.getTotalInventoryQuantity();
        Long totalReserved = productRepository.getTotalReservedQuantity();
        long activeReservations = reservationRepository.countByStatus(ReservationStatus.ACTIVE);
        
        return new InventoryStatistics(
                totalProducts,
                totalQuantity != null ? totalQuantity : 0L,
                totalReserved != null ? totalReserved : 0L,
                activeReservations
        );
    }
    
    /**
     * Converts a Product entity to ProductResponse DTO.
     */
    private ProductResponse convertToProductResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.getReservedQuantity(),
                product.getAvailableQuantity()
        );
    }
    
    // Inner class for inventory statistics
    public static class InventoryStatistics {
        private final long totalProducts;
        private final long totalQuantity;
        private final long totalReserved;
        private final long activeReservations;
        
        public InventoryStatistics(long totalProducts, long totalQuantity, long totalReserved, long activeReservations) {
            this.totalProducts = totalProducts;
            this.totalQuantity = totalQuantity;
            this.totalReserved = totalReserved;
            this.activeReservations = activeReservations;
        }
        
        public long getTotalProducts() { return totalProducts; }
        public long getTotalQuantity() { return totalQuantity; }
        public long getTotalReserved() { return totalReserved; }
        public long getAvailableQuantity() { return totalQuantity - totalReserved; }
        public long getActiveReservations() { return activeReservations; }
    }
}
