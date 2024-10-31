/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.nb.api.components.core;

import io.nosqlbench.nb.api.labels.NBLabels;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * <P>Because of ctor super calling order requirements, the task thread can't always be started
 * automatically in super(...). If that is the case, then use this class directly and call
 * start() at the end of your subtype ctor.</P>
 *
 * <P>Otherwise, it is safe to use {@link PeriodicTaskComponent} directly.</P>
 */
public abstract class UnstartedPeriodicTaskComponent extends NBBaseComponent implements Runnable {

    private static final Logger logger = LogManager.getLogger(UnstartedPeriodicTaskComponent.class);
    protected final long intervalmillis;
    private final Lock lock = new ReentrantLock();
    private final Condition shutdownSignal = lock.newCondition();
    private final FirstReport firstReport;
    private final LastReport lastReport;
    private final String threadName;
    Thread thread;
    private boolean running = true;

    public enum FirstReport {
        Immediately,
        OnInterval
    }
    public enum LastReport {
        None,
        onClose,
        /**
         * OnInterrupt is a stronger version of OnClose, including scenarios where the process is interrupted with a signal
         */
        OnInterrupt
    }

    public UnstartedPeriodicTaskComponent(
        NBComponent node,
        NBLabels extraLabels,
        long millis,
        String threadName,
        FirstReport firstReport,
        LastReport lastReport
    ) {
        super(node, extraLabels);
        this.threadName = threadName;
        this.intervalmillis = millis;
        this.firstReport = firstReport;
        this.lastReport = lastReport;
        if(lastReport== LastReport.OnInterrupt) {
            Thread hook=new Thread(this::task,"shutdownhook-"+threadName);
            Runtime.getRuntime().addShutdownHook(hook);
        }
        // TODO: There is a potential race condition between init and invoke here, if millis is low enough and post-super() state is needed
    }

    public void start() {
        if (firstReport==FirstReport.Immediately) task();
        thread = Thread.ofVirtual().name(threadName).start(this);
    }
    /**
     * This task should only do what is needed once each period.
     * If it throws any exceptions, then these exceptions will cause the period task
     * to exit. Thus, if you need to allow failures in some cases while keeping
     * the caller (scheduler) active, all errors should be caught and handled
     * internally.
     */
    protected abstract void task();

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        long reportAt = now + intervalmillis;
        long waitfor = reportAt - now;

        while (running) {
            while (running && waitfor > 0) {
                boolean signalReceived = false;
                try {
                    lock.lock();
                    signalReceived = shutdownSignal.await(waitfor, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ignored) {
                } finally {
                    lock.unlock();
                }
                if (signalReceived) {
                    logger.debug("signal shutting down " + this);
                    return;
                }
                now = System.currentTimeMillis();
                waitfor = reportAt - now;
            }
//            logger.info("summarizing metrics to console");
            try {
                lock.lock();
                task();
            } catch (Exception e) {
                logger.error(e);
                throw new RuntimeException(e);
            } finally {
                reportAt = reportAt + (intervalmillis);
                now = System.currentTimeMillis();
                waitfor = reportAt - now;
                lock.unlock();
            }
        }
        logger.info("shutting down periodic runnable component: " + description());
    }

    public void teardown() {

        logger.debug("shutting down " + this);

        lock.lock();
        running = false;
        shutdownSignal.signalAll();
        lock.unlock();

//        if (lastReport==LastReport.onClose || lastReport==LastReport.OnInterrupt) {
//            logger.debug("final task() call for period component " + description());
//            task();
//        }

        logger.debug("signaled reporter thread to shut down " + description());

        try {
            thread.join();
        } catch (InterruptedException e) {
            logger.warn("interrupted while joining thread");
        }

        if (this.lastReport== LastReport.onClose) {
            logger.debug("running " + this + " one last time on close().");
            task();
        }

        super.teardown();

    }



}
