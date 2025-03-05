package com.simaslog.webHook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WebHookApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebHookApplication.class, args);
    }
}
