package com.tomergabel.examples.prometheus;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tomergabel.examples.prometheus.scenarios.*;
import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

enum ScenarioAction {
    @JsonProperty("stop")
    STOP,
    @JsonProperty("start")
    START
}

record HealthResponse(Map<String, String> scenarios) {}
record ScenarioStatusResponse(String scenario, String status) {}
record ScenarioActionRequest(ScenarioAction action) {}

@RestController
@RequestMapping("/scenario")
public class ScenarioController {

    private final Map<String, Supplier<Scenario>> builders = Map.of(
            "scenario1", MemLeak::new,
            "scenario2", DiskSpace::new,
            "scenario3", RateLimit::new
    );

    private final Map<String, Scenario> running = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String statusOf(String alias) {
        return running.containsKey(alias) && running.get(alias).isAlive() ? "running" : "stopped";
    }

    @GetMapping("/health")
    HealthResponse health() {
        return new HealthResponse(
                builders.keySet().stream().collect(Collectors.toMap(k -> k, this::statusOf)));
    }

    @GetMapping("/{alias}")
    ScenarioStatusResponse scenarioStatus(@PathVariable String alias) {
        if (!builders.containsKey(alias))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return new ScenarioStatusResponse(alias, statusOf(alias));
    }

    @PostMapping("/{alias}")
    ScenarioStatusResponse action(@PathVariable String alias, @RequestBody ScenarioActionRequest req) throws InterruptedException {
        if (!builders.containsKey(alias))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        Scenario active = running.get(alias);

        switch (req.action()) {
            case STOP:
                if (active == null)
                    throw new ResponseStatusException(HttpStatus.NOT_MODIFIED);
                active.stop(2000L);
                logger.info("Stopping scenario {}", alias);
                return new ScenarioStatusResponse(alias, "stopped");
            case START:
                if (active != null && active.isAlive())
                    throw new ResponseStatusException(HttpStatus.NOT_MODIFIED);
                logger.info("Starting scenario {}", alias);
                var scenario = builders.get(alias).get();
                scenario.start();
                running.put(alias, scenario);
                return new ScenarioStatusResponse(alias, "running");
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid action '" + req.action() + "'");
        }
    }

    private int requestCount = 0;
    private Instant requestCountFrom = Instant.ofEpochMilli(0);

    @GetMapping("/do_something")
    @Hidden
    synchronized void sampleEndpoint() {
        var elapsed = Duration.between(this.requestCountFrom, Instant.now());
        if (elapsed.compareTo(RateLimit.DEFAULT_RATE_LIMIT_WINDOW) > 0) {
            this.requestCountFrom = Instant.now();
            this.requestCount = 1;
        } else if (this.requestCount >= 10) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
        } else
            this.requestCount++;
    }
}
