package com.rishanth.flux360;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@EnableScheduling
@SpringBootApplication
public class Flux360Application {

    public static void main(String[] args) {
        SpringApplication.run(Flux360Application.class, args);
    }
}





















