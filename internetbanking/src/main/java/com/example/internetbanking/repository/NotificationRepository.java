package com.example.internetbanking.repository;

import com.example.internetbanking.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser_UserIdOrderByTimestampDesc(String userId);
}