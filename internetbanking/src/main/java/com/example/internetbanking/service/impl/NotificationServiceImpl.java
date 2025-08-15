package com.example.internetbanking.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.internetbanking.entity.Notification;
import com.example.internetbanking.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl {

	@Autowired
	private NotificationRepository notificationRepo;

    private final Logger logger = Logger.getLogger(NotificationServiceImpl.class.getName());

    public List<Notification> getUserNotifications(String userId) {
        logger.info("Fetching notifications for user " + userId);
        return notificationRepo.findByUser_UserIdOrderByTimestampDesc(userId);
    }

    public void markAsRead(String userId, Long notificationId) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (!notification.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Notification does not belong to the user");
        }
        notification.setRead(true);
        notificationRepo.save(notification);
        logger.info("Notification " + notificationId + " marked as read for user " + userId);
    }
}



