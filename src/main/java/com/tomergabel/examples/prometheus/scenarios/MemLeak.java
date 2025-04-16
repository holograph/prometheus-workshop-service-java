package com.tomergabel.examples.prometheus.scenarios;

import java.util.ArrayList;

public class MemLeak extends Thread implements Scenario {
    public final static int DEFAULT_CHUNK_SIZE = 1024 * 1024;
    public final static int DEFAULT_MAX_CHUNKS = 600;
    public final static int DEFAULT_INTERVAL_SECONDS = 1;

    private final int chunkSize;
    private final int maxChunks;
    private final long intervalSeconds;
    private final ArrayList<byte[]> chunks;
    private volatile boolean stopped;

    public MemLeak() {
        this.chunkSize = DEFAULT_CHUNK_SIZE;
        this.maxChunks = DEFAULT_MAX_CHUNKS;
        this.intervalSeconds = DEFAULT_INTERVAL_SECONDS;
        this.chunks = new ArrayList<>();
        this.stopped = false;
    }

    @Override
    public String getDisplayName() {
        return "scenario1";
    }

    @Override
    public void stop(long timeoutMS) throws InterruptedException {
        this.stopped = true;
        this.interrupt();
        this.join(timeoutMS);
    }

    public void run() {
        try {
            while (!this.stopped) {
                if (this.chunks.size() < maxChunks) {
                    byte[] chunk = new byte[this.chunkSize];
                    this.chunks.add(chunk);

                }
                Thread.sleep(this.intervalSeconds * 1000);
            }
        } catch (InterruptedException e) {
            this.chunks.clear();
        }
    }
}
