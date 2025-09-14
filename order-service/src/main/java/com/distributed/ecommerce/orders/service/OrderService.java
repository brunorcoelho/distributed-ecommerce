package com.distributed.ecommerce.orders.service;

import com.distributed.ecommerce.orders.dto.*;
import com.distributed.ecommerce.orders.model.Order;
import com.distributed.ecommerce.orders.model.OrderItem;
import com.distributed.ecommerce.orders.model.OrderStatus;
import com.distributed.ecommerce.orders.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private InventoryService inventoryService;
    
    /**
     * Creates a new order and processes it by attempting to reserve inventory.
     * 
     * @param createOrderRequest the order creation request
     * @return OrderResponse with the created order and its final status
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest createOrderRequest) {
        logger.info("Creating new order for customer: {}", createOrderRequest.getCustomerName());
        logger.debug("Order details: {}", createOrderRequest);
        
        // Create and save the order entity
        Order order = new Order(
                createOrderRequest.getCustomerName(),
                createOrderRequest.getCustomerEmail(),
                createOrderRequest.getCustomerAddress(),
                createOrderRequest.getCustomerPhone(),
                createOrderRequest.getTotalAmount()
        );
        
        // Add order items
        for (OrderItemDto itemDto : createOrderRequest.getItems()) {
            OrderItem orderItem = new OrderItem(
                    itemDto.getProductId(),
                    itemDto.getProductName(),
                    itemDto.getQuantity(),
                    itemDto.getPrice()
            );
            order.addItem(orderItem);
        }
        
        // Save the order with PENDENTE status
        order = orderRepository.save(order);
        logger.info("Order created with ID: {} and status: {}", order.getId(), order.getStatus());
        
        // Attempt to reserve inventory
        try {
            InventoryReservationRequest reservationRequest = createInventoryReservationRequest(order);
            InventoryReservationResponse reservationResponse = inventoryService.reserveInventory(reservationRequest);
            
            if (reservationResponse.isSuccess()) {
                order.approve();
                logger.info("Order {} approved - inventory reserved successfully", order.getId());
            } else {
                order.cancel();
                logger.warn("Order {} cancelled - inventory reservation failed: {}", 
                           order.getId(), reservationResponse.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error during inventory reservation for order {}: {}", 
                        order.getId(), e.getMessage(), e);
            order.fail();
        }
        
        // Save the updated order status
        order = orderRepository.save(order);
        
        logger.info("Order {} processing completed with final status: {}", order.getId(), order.getStatus());
        
        return convertToOrderResponse(order);
    }
    
    /**
     * Retrieves an order by its ID.
     * 
     * @param orderId the order ID
     * @return Optional containing the OrderResponse if found
     */
    public Optional<OrderResponse> getOrderById(Long orderId) {
        logger.debug("Retrieving order with ID: {}", orderId);
        
        Optional<Order> orderOpt = orderRepository.findByIdWithItems(orderId);
        
        if (orderOpt.isPresent()) {
            OrderResponse response = convertToOrderResponse(orderOpt.get());
            logger.debug("Order found: {}", response);
            return Optional.of(response);
        } else {
            logger.warn("Order not found with ID: {}", orderId);
            return Optional.empty();
        }
    }
    
    /**
     * Retrieves all orders for a specific customer email.
     * 
     * @param customerEmail the customer email
     * @return List of OrderResponse
     */
    public List<OrderResponse> getOrdersByCustomerEmail(String customerEmail) {
        logger.debug("Retrieving orders for customer: {}", customerEmail);
        
        List<Order> orders = orderRepository.findByCustomerEmail(customerEmail);
        
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Retrieves all orders with a specific status.
     * 
     * @param status the order status
     * @return List of OrderResponse
     */
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        logger.debug("Retrieving orders with status: {}", status);
        
        List<Order> orders = orderRepository.findByStatus(status);
        
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets order statistics.
     * 
     * @return OrderStatistics containing counts by status
     */
    public OrderStatistics getOrderStatistics() {
        logger.debug("Retrieving order statistics");
        
        long pendingCount = orderRepository.countByStatus(OrderStatus.PENDENTE);
        long approvedCount = orderRepository.countByStatus(OrderStatus.APROVADO);
        long cancelledCount = orderRepository.countByStatus(OrderStatus.CANCELADO);
        long failedCount = orderRepository.countByStatus(OrderStatus.FALHOU);
        
        return new OrderStatistics(pendingCount, approvedCount, cancelledCount, failedCount);
    }
    
    /**
     * Converts an Order entity to OrderResponse DTO.
     */
    private OrderResponse convertToOrderResponse(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(item -> new OrderItemDto(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
        
        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getCustomerAddress(),
                order.getCustomerPhone(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                itemDtos
        );
    }
    
    /**
     * Creates an inventory reservation request from an order.
     */
    private InventoryReservationRequest createInventoryReservationRequest(Order order) {
        List<InventoryItemRequest> inventoryItems = order.getItems().stream()
                .map(item -> new InventoryItemRequest(item.getProductId(), item.getQuantity()))
                .collect(Collectors.toList());
        
        return new InventoryReservationRequest(order.getId(), inventoryItems);
    }
    
    // Inner class for order statistics
    public static class OrderStatistics {
        private final long pendingCount;
        private final long approvedCount;
        private final long cancelledCount;
        private final long failedCount;
        
        public OrderStatistics(long pendingCount, long approvedCount, long cancelledCount, long failedCount) {
            this.pendingCount = pendingCount;
            this.approvedCount = approvedCount;
            this.cancelledCount = cancelledCount;
            this.failedCount = failedCount;
        }
        
        public long getPendingCount() { return pendingCount; }
        public long getApprovedCount() { return approvedCount; }
        public long getCancelledCount() { return cancelledCount; }
        public long getFailedCount() { return failedCount; }
        public long getTotalCount() { return pendingCount + approvedCount + cancelledCount + failedCount; }
    }
}
