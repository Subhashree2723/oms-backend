package com.oms.backend.repository;

import com.oms.backend.entity.WhatsappLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WhatsappLogRepository extends JpaRepository<WhatsappLog, Long> {
    List<WhatsappLog> findByOrderId(Long orderId);
}
