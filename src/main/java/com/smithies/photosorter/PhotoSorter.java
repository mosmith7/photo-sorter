package com.smithies.photosorter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.smithies.photosorter.config.ApplicationConfiguration;

@SpringBootApplication
@Configuration
@Import(ApplicationConfiguration.class)
public class PhotoSorter {

	public static void main(String[] args) {
		SpringApplication.run(PhotoSorter.class, args);
		System.out.println("Application is running");
		System.setProperty("spring.config.location", "classpath:application.yaml,application.yaml");
	}
}
