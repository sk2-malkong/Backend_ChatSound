package com.example.final_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class FinalBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinalBackendApplication.class, args);
    }

}
