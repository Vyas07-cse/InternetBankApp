package com.example.internetbanking.repository;
import com.example.internetbanking.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByAccountNumber(String accountNumber);
    List<Account> findByUserId(String userId);
    Optional<Account> findByAccountNumber(String fromAccount);

}
