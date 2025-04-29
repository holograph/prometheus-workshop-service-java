package com.tomergabel.examples.prometheus;

public class SingleScenarioStatusResponse extends DemoResponse {
    final String scenario;

    public SingleScenarioStatusResponse(String scenario, String status) {
        super(status);
        this.scenario = scenario;
    }

    public String getScenario() {
        return scenario;
    }
}
