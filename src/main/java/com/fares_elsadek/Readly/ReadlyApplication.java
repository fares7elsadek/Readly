package com.fares_elsadek.Readly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableJpaAuditing
@EnableRetry
@ConfigurationPropertiesScan
public class ReadlyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReadlyApplication.class, args);
	}

}
