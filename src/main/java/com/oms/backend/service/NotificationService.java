package com.oms.backend.service;

import com.oms.backend.entity.Notification;
import java.util.List;

public interface NotificationService {
    Notification notify(Long userId, String title, String message);
    List<Notification> getForUser(String username);
    void markAsRead(Long notificationId);
}
