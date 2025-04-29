package com.tomergabel.examples.prometheus;

import io.opentelemetry.api.OpenTelemetry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableAutoConfiguration
public class WorkshopApplication {
    @Bean ShowcaseController showcaseController(OpenTelemetry openTelemetry) {
        return new ShowcaseController(openTelemetry);
    }

    @Bean ScenarioController scenarioController() {
        return new ScenarioController();
    }

    public static void main(String[] args) {
        SpringApplication.run(WorkshopApplication.class, args);
    }
}
