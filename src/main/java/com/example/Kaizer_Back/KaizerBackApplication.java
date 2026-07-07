package com.example.Kaizer_Back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KaizerBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(KaizerBackApplication.class, args);
	}

}
