package com.dducwsjvbe.backendjava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class BackendjavaApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendjavaApplication.class, args);
	}

}
