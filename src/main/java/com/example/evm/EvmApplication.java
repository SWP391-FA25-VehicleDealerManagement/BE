package com.example.evm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EvmApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvmApplication.class, args);
    }
}
