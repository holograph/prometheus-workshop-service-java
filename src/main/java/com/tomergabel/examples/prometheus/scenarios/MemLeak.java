package com.tomergabel.examples.prometheus.scenarios;

import java.util.ArrayList;

public class MemLeak extends Scenario {
    public final static int DEFAULT_CHUNK_SIZE = 1024 * 1024;
    public final static int DEFAULT_MAX_CHUNKS = 600;
    public final static int DEFAULT_INTERVAL_SECONDS = 1;

    private final int chunkSize;
    private final int maxChunks;
    private final long intervalSeconds;
    private final ArrayList<byte[]> chunks;

    public MemLeak() {
        this.chunkSize = DEFAULT_CHUNK_SIZE;
        this.maxChunks = DEFAULT_MAX_CHUNKS;
        this.intervalSeconds = DEFAULT_INTERVAL_SECONDS;
        this.chunks = new ArrayList<>();
        this.stopped = false;

        this.setDaemon(true);
    }

    @Override
    public String getDisplayName() {
        return "scenario1";
    }

    public void run() {
        try {
            while (!this.stopped) {
                if (this.chunks.size() < maxChunks) {
                    byte[] chunk = new byte[this.chunkSize];
                    this.chunks.add(chunk);

                }
                //noinspection BusyWait
                Thread.sleep(this.intervalSeconds * 1000);
            }
        } catch (InterruptedException e) {
            this.chunks.clear();
        }
    }
}
