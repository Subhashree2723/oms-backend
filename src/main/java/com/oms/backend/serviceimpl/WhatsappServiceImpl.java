package com.oms.backend.serviceimpl;

import com.oms.backend.entity.Orders;
import com.oms.backend.entity.WhatsappLog;
import com.oms.backend.repository.OrdersRepository;
import com.oms.backend.repository.WhatsappLogRepository;
import com.oms.backend.service.WhatsappService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WhatsappServiceImpl implements WhatsappService {

    private static final Logger log = LoggerFactory.getLogger(WhatsappServiceImpl.class);

    private final WhatsappLogRepository whatsappLogRepository;
    private final OrdersRepository ordersRepository;

    @Override
    public void sendOrderNotification(Long orderId, String phoneNumber, String message) {
        // NOTE: This is a simulated send. Wire this method to the WhatsApp
        // Business Cloud API or Twilio's WhatsApp API here (HTTP POST with
        // your access token) to make it a real integration.
        Orders order = orderId != null ? ordersRepository.findById(orderId).orElse(null) : null;

        String status = "SENT";
        log.info("[WhatsApp SIMULATED] To: {} | Message: {}", phoneNumber, message);

        WhatsappLog logEntry = WhatsappLog.builder()
                .order(order)
                .phoneNumber(phoneNumber)
                .message(message)
                .status(status)
                .build();
        whatsappLogRepository.save(logEntry);
    }
}
