package com.example.internetbanking.exception;

public class UserNotFoundException extends RuntimeException {
	public UserNotFoundException(String Message) {
		super(Message);
	}
}

