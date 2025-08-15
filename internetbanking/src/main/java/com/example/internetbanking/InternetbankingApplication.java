package com.example.internetbanking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InternetbankingApplication {

	public static void main(String[] args) {
		SpringApplication.run(InternetbankingApplication.class, args);
	}

}
