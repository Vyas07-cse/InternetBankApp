package com.example.internetbanking.repository;

import com.example.internetbanking.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, String> {
	@Query("SELECT b FROM Beneficiary b WHERE b.userId = :userId AND b.verified = true")
	List<Beneficiary> findByUserId(@Param("userId") String userId);

	

	boolean findByAccountNumber(String accountNumber);

	boolean existsByAccountNumberAndUserId(String accountNumber, String userId);
	void deleteByUserId(String userId);

}