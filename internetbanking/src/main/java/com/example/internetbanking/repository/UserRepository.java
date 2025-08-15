package com.example.internetbanking.repository;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.internetbanking.entity.User;
public interface UserRepository extends JpaRepository<User,String> {
	
	 User findByUserId(String userId);

	   User findByEmail(String email);

	   boolean existsByEmail(String email);

	   List<User> findAllByIsVerifiedFalseAndCodeExpiryBefore(Date now);


	
}
