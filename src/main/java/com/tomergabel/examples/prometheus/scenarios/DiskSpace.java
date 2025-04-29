package com.tomergabel.examples.prometheus.scenarios;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DiskSpace extends Scenario {
    public final static int DEFAULT_CHUNK_SIZE = 4 * 1024 * 1024;
    public final static int DEFAULT_MAX_CHUNKS = 1024;
    public final static int DEFAULT_INTERVAL_SECONDS = 1;
    private static final Logger log = LoggerFactory.getLogger(DiskSpace.class);

    private final int chunkSize;
    private final int maxChunks;
    private final long intervalSeconds;
    private final List<File> files;
    private final Random random;

    public DiskSpace() {
        this.chunkSize = DEFAULT_CHUNK_SIZE;
        this.maxChunks = DEFAULT_MAX_CHUNKS;
        this.intervalSeconds = DEFAULT_INTERVAL_SECONDS;
        this.stopped = false;
        this.files = new ArrayList<>(this.maxChunks);
        this.random = new Random();

        this.setDaemon(true);
    }

    @Override
    public String getDisplayName() {
        return "scenario2";
    }

    public void run() {
        try {
            while (!this.stopped) {
                if (this.files.size() < this.maxChunks) {
                    File f = File.createTempFile("scenario2", "tmp");
                    this.files.add(f);
                    try (var os = new FileOutputStream(f)) {
                        byte[] chunk = new byte[this.chunkSize];
                        this.random.nextBytes(chunk);
                        os.write(chunk);
                    }
                }
                //noinspection BusyWait
                Thread.sleep(this.intervalSeconds * 1000);
            }
        } catch (IOException e) {
            log.error("Failed to write temporary file, scenario crashed", e);
        } catch (InterruptedException ignored) {
        } finally {
            //noinspection ResultOfMethodCallIgnored
            this.files.forEach(File::delete);
        }
    }
}
