package com.viettel.it.thread;

import org.apache.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by hanhnv68 on 9/7/2017.
 */
public class FixedExecutionRunnable implements Runnable {

    private static final Logger logger = Logger.getLogger(FixedExecutionRunnable.class);

    private final AtomicInteger runCount = new AtomicInteger();
    private final Runnable delegate;
    private volatile ScheduledFuture<?> self;
    private final int maxRunCount;
    private final ScheduledExecutorService executor;

    public FixedExecutionRunnable(Runnable delegate, int maxRunCount, ScheduledExecutorService executor) {
        this.delegate = delegate;
        this.maxRunCount = maxRunCount;
        this.executor = executor;
    }

    @Override
    public void run() {
        delegate.run();
        if(runCount.incrementAndGet() == maxRunCount) {
//            boolean interrupted = false;
            try {
                while(self == null) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
//                        interrupted = true;
                        try {
                            Thread.currentThread().interrupt();
                        } catch (Exception ex) {
                            logger.error(ex.getMessage(), ex);
                        }
                    }
                }
                self.cancel(false);
//                try {
//                    if(interrupted) {
//
//                    }
//                } catch (Exception e) {
//                    logger.error(e.getMessage(), e);
//                }
            } finally {
                logger.info("finally stop fix scheduler");
                executor.shutdown();
            }
        }
    }

    public void runNTimes(long period, long initialDelay, TimeUnit unit) {
        self = executor.scheduleAtFixedRate(this, initialDelay, period, unit);
    }


}
