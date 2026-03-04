package com.closer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CloserApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloserApplication.class, args);
    }
}
