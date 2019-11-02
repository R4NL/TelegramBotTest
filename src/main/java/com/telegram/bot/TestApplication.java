package com.telegram.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;

@SpringBootApplication

public class TestApplication {

    public static void main(String[] args) {
        ApiContextInitializer.init();
        SpringApplication.run(TestApplication.class, args);

    }

}
