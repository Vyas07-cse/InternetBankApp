package com.example.internetbanking.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class LogHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    private LocalDateTime loginTime;

    private String ipAddress;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

   
    public LogHistory() {
    }

   
    public LogHistory(Long logId, LocalDateTime loginTime, String ipAddress, User user) {
        this.logId = logId;
        this.loginTime = loginTime;
        this.ipAddress = ipAddress;
        this.user = user;
    }

  
    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

   
    @Override
    public String toString() {
        return "LogHistory{" +
                "logId=" + logId +
                ", loginTime=" + loginTime +
                ", ipAddress='" + ipAddress + '\'' +
                ", user=" + user +
                '}';
    }
}
