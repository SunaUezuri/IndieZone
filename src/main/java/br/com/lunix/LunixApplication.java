package br.com.lunix;

import io.mongock.runner.springboot.EnableMongock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableMongock
@EnableScheduling
public class LunixApplication {

	public static void main(String[] args) {
		SpringApplication.run(LunixApplication.class, args);
	}

}
