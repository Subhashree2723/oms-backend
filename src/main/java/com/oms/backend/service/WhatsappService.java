package com.oms.backend.service;

public interface WhatsappService {
    /**
     * Simulated WhatsApp notification. Logs the message to the whatsapp_logs table.
     * To wire this up to a real WhatsApp Business API / Twilio integration,
     * replace the implementation in WhatsappServiceImpl with an actual HTTP call
     * and plug in your account credentials via application.properties.
     */
    void sendOrderNotification(Long orderId, String phoneNumber, String message);
}
