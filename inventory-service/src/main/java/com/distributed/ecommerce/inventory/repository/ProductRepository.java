package com.distributed.ecommerce.inventory.repository;

import com.distributed.ecommerce.inventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT p FROM Product p WHERE (p.quantity - p.reservedQuantity) >= :minQuantity")
    List<Product> findProductsWithAvailableStock(@Param("minQuantity") Integer minQuantity);
    
    @Query("SELECT p FROM Product p WHERE p.quantity > 0")
    List<Product> findProductsInStock();
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
    
    @Query("SELECT SUM(p.quantity) FROM Product p")
    Long getTotalInventoryQuantity();
    
    @Query("SELECT SUM(p.reservedQuantity) FROM Product p")
    Long getTotalReservedQuantity();
}
