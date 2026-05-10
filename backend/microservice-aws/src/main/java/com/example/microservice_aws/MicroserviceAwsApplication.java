package com.example.microservice_aws;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class MicroserviceAwsApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Guayaquil"));
		SpringApplication.run(MicroserviceAwsApplication.class, args);
	}

	@PostConstruct
	public void printLocalTime() {
		System.out.println("Hora Local JVM: " + java.time.ZonedDateTime.now());
		System.out.println("Instant (UTC): " + java.time.Instant.now());
	}
}
