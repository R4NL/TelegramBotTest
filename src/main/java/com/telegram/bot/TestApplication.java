package com.telegram.bot;

import com.telegram.bot.servise.PreStart;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;

@SpringBootApplication

public class TestApplication {

    public static void main(String[] args) {
        PreStart.createDirectories();
        ApiContextInitializer.init();
        SpringApplication.run(TestApplication.class, args);

    }
}
