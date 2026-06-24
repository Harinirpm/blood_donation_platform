package com.bloodbridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BloodBridgeApplication {
    public static void main(String[] args) {
        SpringApplication.run(BloodBridgeApplication.class, args);
        System.out.println("Server Running....");
    }
}
