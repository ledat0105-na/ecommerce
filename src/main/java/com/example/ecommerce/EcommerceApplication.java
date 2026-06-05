package com.example.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication(scanBasePackages = {
		"com.example.ecommerce",
		"controller",
		"service", 
		"repository",
		"entity",
		"config",
		"database"
})
@EntityScan(basePackages = {"entity", "com.example.ecommerce.entity"})
@EnableJpaRepositories(basePackages = {"repository", "com.example.ecommerce.repository"})
public class EcommerceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommerceApplication.class, args);
	}

}
