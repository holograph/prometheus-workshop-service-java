package com.tomergabel.examples.prometheus;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.View;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableAutoConfiguration
public class WorkshopApplication {
    @Bean AutoConfigurationCustomizerProvider autoConfigurationCustomizerProvider() {
        return config -> config
            .addMeterProviderCustomizer((meterProvider, props) -> meterProvider
                .registerMetricReader(
                    PrometheusHttpServer.builder()
                        .setPort(props.getInt("prometheus.http.port", 8081))
                        .build())
                .registerView(
                    InstrumentSelector.builder()
                        .setMeterName("showcase")
                        .build(),
                    View.builder()
                        .setAggregation(Aggregation.explicitBucketHistogram(List.of(0.0, 500.0, 1000.0, 10000.0)))
                        .build()
                )
            );
    }

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
