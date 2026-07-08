package com.oms.backend.service;

import com.oms.backend.entity.Delivery;
import java.util.List;

public interface DeliveryService {
    Delivery getByOrderId(Long orderId);
    Delivery updateStatus(Long orderId, String status, String notes);
    List<Delivery> getAll();
}
