package com.oms.backend.serviceimpl;

import com.oms.backend.entity.Notification;
import com.oms.backend.entity.User;
import com.oms.backend.exception.ResourceNotFoundException;
import com.oms.backend.repository.NotificationRepository;
import com.oms.backend.repository.UserRepository;
import com.oms.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public Notification notify(Long userId, String title, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Notification n = Notification.builder().user(user).title(title).message(message).isRead(false).build();
        return notificationRepository.save(n);
    }

    @Override
    public List<Notification> getForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        n.setIsRead(true);
        notificationRepository.save(n);
    }
}
