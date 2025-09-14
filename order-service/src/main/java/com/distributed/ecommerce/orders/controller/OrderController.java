package com.distributed.ecommerce.orders.controller;

import com.distributed.ecommerce.orders.dto.CreateOrderRequest;
import com.distributed.ecommerce.orders.dto.OrderResponse;
import com.distributed.ecommerce.orders.model.OrderStatus;
import com.distributed.ecommerce.orders.service.OrderService;
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
@RequestMapping("/api/orders")
@CrossOrigin(origins = {"${cors.allowed-origins}"}, 
             methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
             allowedHeaders = {"${cors.allowed-headers}"})
public class OrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    @Autowired
    private OrderService orderService;
    
    /**
     * Creates a new order.
     * 
     * @param createOrderRequest the order creation request
     * @return ResponseEntity with the created order
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequest createOrderRequest) {
        logger.info("Received order creation request from customer: {}", createOrderRequest.getCustomerName());
        logger.debug("Order request details: {}", createOrderRequest);
        
        try {
            OrderResponse orderResponse = orderService.createOrder(createOrderRequest);
            
            if (orderResponse.getStatus() == OrderStatus.APROVADO) {
                logger.info("Order {} created and approved successfully", orderResponse.getId());
                return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
            } else if (orderResponse.getStatus() == OrderStatus.CANCELADO) {
                logger.warn("Order {} created but cancelled due to insufficient stock", orderResponse.getId());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of(
                                "message", "Order cancelled due to insufficient stock",
                                "order", orderResponse
                        ));
            } else if (orderResponse.getStatus() == OrderStatus.FALHOU) {
                logger.error("Order {} creation failed due to system error", orderResponse.getId());
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of(
                                "message", "Order processing failed due to system error. Please try again later.",
                                "order", orderResponse
                        ));
            } else {
                logger.warn("Order {} created with unexpected status: {}", orderResponse.getId(), orderResponse.getStatus());
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(orderResponse);
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error while creating order: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal server error while processing order"));
        }
    }
    
    /**
     * Retrieves an order by its ID.
     * 
     * @param orderId the order ID
     * @return ResponseEntity with the order if found
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
        logger.debug("Retrieving order with ID: {}", orderId);
        
        try {
            Optional<OrderResponse> orderOpt = orderService.getOrderById(orderId);
            
            if (orderOpt.isPresent()) {
                return ResponseEntity.ok(orderOpt.get());
            } else {
                logger.warn("Order not found with ID: {}", orderId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Order not found with ID: " + orderId));
            }
            
        } catch (Exception e) {
            logger.error("Error retrieving order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal server error while retrieving order"));
        }
    }
    
    /**
     * Retrieves orders by customer email.
     * 
     * @param customerEmail the customer email
     * @return ResponseEntity with list of orders
     */
    @GetMapping
    public ResponseEntity<?> getOrdersByCustomerEmail(@RequestParam(required = false) String customerEmail,
                                                     @RequestParam(required = false) OrderStatus status) {
        logger.debug("Retrieving orders with customerEmail: {} and status: {}", customerEmail, status);
        
        try {
            List<OrderResponse> orders;
            
            if (customerEmail != null && !customerEmail.trim().isEmpty()) {
                orders = orderService.getOrdersByCustomerEmail(customerEmail.trim());
            } else if (status != null) {
                orders = orderService.getOrdersByStatus(status);
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Either customerEmail or status parameter is required"));
            }
            
            return ResponseEntity.ok(orders);
            
        } catch (Exception e) {
            logger.error("Error retrieving orders: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal server error while retrieving orders"));
        }
    }
    
    /**
     * Retrieves order statistics.
     * 
     * @return ResponseEntity with order statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getOrderStatistics() {
        logger.debug("Retrieving order statistics");
        
        try {
            OrderService.OrderStatistics statistics = orderService.getOrderStatistics();
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            logger.error("Error retrieving order statistics: {}", e.getMessage(), e);
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
                "service", "order-service",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}
