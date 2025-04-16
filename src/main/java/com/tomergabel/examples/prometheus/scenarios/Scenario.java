package com.tomergabel.examples.prometheus.scenarios;

public interface Scenario {
    String getDisplayName();
    void stop(long timeoutMS) throws InterruptedException;
    boolean isAlive();
}
