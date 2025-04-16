package com.tomergabel.examples.prometheus.scenarios;

public abstract class Scenario extends Thread {
    public abstract String getDisplayName();

    protected volatile boolean stopped = false;

    public void stop(long timeoutMS) throws InterruptedException {
        this.stopped = true;
        this.interrupt();
        this.join(timeoutMS);
    }
}
