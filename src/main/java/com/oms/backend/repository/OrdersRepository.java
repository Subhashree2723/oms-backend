package com.oms.backend.repository;

import com.oms.backend.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
    Optional<Orders> findByOrderNumber(String orderNumber);
    List<Orders> findByCustomerId(Long customerId);
    List<Orders> findByStatus(Orders.OrderStatus status);

    @Query("SELECT o FROM Orders o WHERE o.orderDate BETWEEN :start AND :end")
    List<Orders> findByOrderDateBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
