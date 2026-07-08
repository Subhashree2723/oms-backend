package com.oms.backend.serviceimpl;

import com.oms.backend.entity.Delivery;
import com.oms.backend.exception.BadRequestException;
import com.oms.backend.exception.ResourceNotFoundException;
import com.oms.backend.repository.DeliveryRepository;
import com.oms.backend.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;

    @Override
    public Delivery getByOrderId(Long orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery record not found for order: " + orderId));
    }

    @Override
    public Delivery updateStatus(Long orderId, String status, String notes) {
        Delivery delivery = getByOrderId(orderId);
        Delivery.DeliveryStatus newStatus;
        try {
            newStatus = Delivery.DeliveryStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid delivery status: " + status);
        }
        delivery.setStatus(newStatus);
        if (notes != null) delivery.setTrackingNotes(notes);
        if (newStatus == Delivery.DeliveryStatus.DELIVERED) {
            delivery.setDeliveryDate(LocalDateTime.now());
        }
        return deliveryRepository.save(delivery);
    }

    @Override
    public List<Delivery> getAll() {
        return deliveryRepository.findAll();
    }
}
