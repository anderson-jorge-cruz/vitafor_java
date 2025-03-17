package com.simaslog.webHook;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Swagger OpenApi - SIMASLOG", version = "2", description = "WMS's SIMASLOG API"))
@EnableScheduling
public class WebHookApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebHookApplication.class, args);
    }
}
