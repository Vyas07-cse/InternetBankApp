package com.example.internetbanking.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.internetbanking.repository.UserRepository;
import com.example.internetbanking.entity.User;
@Service
public class MyUserDetailsService implements UserDetailsService{
	
	@Autowired
	private UserRepository userRepo;

	@Override
	public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
		User user=userRepo.findByUserId(userId);
		if(user==null) {
			throw new UsernameNotFoundException("User Not Found");
		}
		
		return new UserPrincipal(user);
	}	

}
