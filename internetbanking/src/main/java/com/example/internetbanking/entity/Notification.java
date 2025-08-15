package com.example.internetbanking.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    private String message;

    private LocalDateTime timestamp;

    private boolean isRead = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    
    public Notification() {
    }

    
    public Notification(Long notificationId, String message, LocalDateTime timestamp, boolean isRead, User user) {
        this.notificationId = notificationId;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = isRead;
        this.user = user;
    }

   
    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

   
    @Override
    public String toString() {
        return "Notification{" +
                "notificationId=" + notificationId +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", isRead=" + isRead +
                ", user=" + user +
                '}';
    }
}
