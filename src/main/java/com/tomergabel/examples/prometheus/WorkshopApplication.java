package com.tomergabel.examples.prometheus;

import io.opentelemetry.api.OpenTelemetry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class WorkshopApplication {

    @Bean ShowcaseController showcaseController(OpenTelemetry openTelemetry) {
        return new ShowcaseController(openTelemetry);
    }

    public static void main(String[] args) {
        SpringApplication.run(WorkshopApplication.class, args);
    }
}
