package com.cos.naverrealtime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NaverrealtimeApplication {

	public static void main(String[] args) {
		SpringApplication.run(NaverrealtimeApplication.class, args);
	}

}
