package com.tradefaster.client;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class TradeFasterScheduler {
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "TradeFaster-Scheduler");
        t.setDaemon(true);
        return t;
    });

    private TradeFasterScheduler() {
    }

    public static void schedule(Runnable task, long delayMs) {
        EXECUTOR.schedule(task, delayMs, TimeUnit.MILLISECONDS);
    }
}
