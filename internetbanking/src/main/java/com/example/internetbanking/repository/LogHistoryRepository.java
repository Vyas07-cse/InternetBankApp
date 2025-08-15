package com.example.internetbanking.repository;

import com.example.internetbanking.entity.LogHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LogHistoryRepository extends JpaRepository<LogHistory, Long> {
    List<LogHistory> findByUser_UserIdOrderByLoginTimeDesc(String userId);
}
