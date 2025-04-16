package com.tomergabel.examples.prometheus;

import com.tomergabel.examples.prometheus.scenarios.MemLeak;
import com.tomergabel.examples.prometheus.scenarios.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class ScenarioHealthResponse extends DemoResponse {
    final Map<String, String> scenarios;

    public ScenarioHealthResponse(Map<String, String> scenarios) {
        this.scenarios = scenarios;
    }

    public Map<String, String> getScenarios() {
        return scenarios;
    }
}

class SingleScenarioStatusResponse extends DemoResponse {
    final String scenario;
    final String status;

    public SingleScenarioStatusResponse(String scenario, String status) {
        this.scenario = scenario;
        this.status = status;
    }

    public String getScenario() {
        return scenario;
    }

    @Override
    public String getStatus() {
        return status;
    }
}

class ScenarioActionRequest {
    private final String action;

    public ScenarioActionRequest(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }
}

@RestController("/scenario")
public class ScenarioController {

    private final Map<String, Supplier<Scenario>> builders = Map.of(
            "scenario1", MemLeak::new
    );

    private final Map<String, Scenario> running = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String statusOf(String alias) {
        return running.containsKey(alias) && running.get(alias).isAlive() ? "running" : "stopped";
    }

    @GetMapping("/health")
    ScenarioHealthResponse health() {
        return new ScenarioHealthResponse(
                builders.keySet().stream().collect(Collectors.toMap(k -> k, this::statusOf)));
    }

    @GetMapping("/{alias}")
    SingleScenarioStatusResponse scenarioStatus(@PathVariable String alias) {
        if (!builders.containsKey(alias))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return new SingleScenarioStatusResponse(alias, statusOf(alias));
    }

    @PostMapping("/{alias}")
    SingleScenarioStatusResponse action(@PathVariable String alias, @RequestBody ScenarioActionRequest req) throws InterruptedException {
        if (!builders.containsKey(alias))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        Scenario active = running.get(alias);

        switch (req.getAction()) {
            case "stop":
                if (active == null)
                    throw new ResponseStatusException(HttpStatus.NOT_MODIFIED);
                active.stop(2000L);
                logger.info("Stopping scenario {}", alias);
                return new SingleScenarioStatusResponse(alias, "stopped");
            case "start":
                if (active != null && active.isAlive())
                    throw new ResponseStatusException(HttpStatus.NOT_MODIFIED);
                logger.info("Starting scenario {}", alias);
                var scenario = builders.get(alias).get();
                scenario.start();
                running.put(alias, scenario);
                return new SingleScenarioStatusResponse(alias, "running");
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid action '" + req.getAction() + "'");
        }
    }


/*
request_count_from = 0
request_count = 0

# TODO relocate to ratelimit scenario
@router.get("/do_something", include_in_schema=False)
def sample_endpoint() -> dict:
    # "Rate limit"
    global request_count, request_count_from
    if request_count_from <= time.time() - 60:
        request_count_from = time.time()
        request_count = 1
    elif request_count >= 10:
        raise HTTPException(status_code=429)
    else:
        request_count += 1

    seconds = float(random.randrange(100, 1000)) / 1000.0
    time.sleep(seconds)
    return {"status": "ok"}
*/
}
