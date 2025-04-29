package com.tomergabel.examples.prometheus.scenarios;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class RateLimit extends Scenario {
    private static final Logger log = LoggerFactory.getLogger(RateLimit.class);

    public final static int DEFAULT_INTERVAL_SECONDS = 1;
    public final static Duration DEFAULT_RATE_LIMIT_WINDOW = Duration.ofMinutes(1);

    @Override
    public String getDisplayName() {
        return "scenario3";
    }

    public void run() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/scenario/do_something"))
                .build();
        try {
            while (!this.stopped) {
                var response = client.send(req, HttpResponse.BodyHandlers.discarding());
                if (response.statusCode() != 200)
                    log.warn("Received non-200 status code " + response.statusCode());

                //noinspection BusyWait
                Thread.sleep(DEFAULT_INTERVAL_SECONDS * 1000);
            }
        } catch (InterruptedException ignoring) {
        } catch (IOException e) {
            log.error("Failed to send HTTP request, scenario crashing", e);
        }
    }
}
