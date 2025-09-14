package com.distributed.ecommerce.inventory.repository;

import com.distributed.ecommerce.inventory.model.Reservation;
import com.distributed.ecommerce.inventory.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    Optional<Reservation> findByOrderId(Long orderId);
    
    List<Reservation> findByStatus(ReservationStatus status);
    
    List<Reservation> findByCreatedAtBefore(LocalDateTime dateTime);
    
    @Query("SELECT r FROM Reservation r LEFT JOIN FETCH r.items WHERE r.id = :id")
    Optional<Reservation> findByIdWithItems(@Param("id") Long id);
    
    @Query("SELECT r FROM Reservation r LEFT JOIN FETCH r.items WHERE r.orderId = :orderId")
    Optional<Reservation> findByOrderIdWithItems(@Param("orderId") Long orderId);
    
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.status = :status")
    Long countByStatus(@Param("status") ReservationStatus status);
}
