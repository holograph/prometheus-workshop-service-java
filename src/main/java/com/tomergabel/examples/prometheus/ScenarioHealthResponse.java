package com.tomergabel.examples.prometheus;

import java.util.Map;

public class ScenarioHealthResponse extends DemoResponse {
    final Map<String, String> scenarios;

    public ScenarioHealthResponse(Map<String, String> scenarios) {
        this.scenarios = scenarios;
    }

    public Map<String, String> getScenarios() {
        return scenarios;
    }
}
