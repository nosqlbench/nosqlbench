/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.activitytype.diag;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.security.InvalidParameterException;
import java.util.concurrent.atomic.AtomicLong;

public class SequenceBlocker {
    private final static Logger logger = LogManager.getLogger(SequenceBlocker.class);
    private final AtomicLong sequence;
    private final AtomicLong waiting=new AtomicLong(0L);
    private final boolean errorsAreFatal;
//    private PriorityBlockingQueue<TakeANumber> queue = new PriorityBlockingQueue<>();
    private Exception fatalError;

    public SequenceBlocker(long start, boolean errorsAreFatal) {
        this.sequence = new AtomicLong(start);
        this.errorsAreFatal = errorsAreFatal;
    }

    public synchronized void awaitAndRun(long startAt, long endPlus, Runnable task) {
        waiting.incrementAndGet();

        if (fatalError != null) {
            throw new RuntimeException("There was previously a fatal error, not allowing new tasks. Error=" + fatalError.getMessage());
        }

//        queue.add(new TakeANumber(startAt, sequencePlusCount, task));
        while (sequence.get() != startAt) {
            try {
                wait(1_000);
            } catch (InterruptedException ignored) {
            }
        }

        try {
            task.run();
        } catch (Exception e) {
            logger.error(() -> "Runnable errored in SequenceBlocker: " + e.getMessage());
            if (errorsAreFatal) {
                this.fatalError = e;
            }
            throw e;
        } finally {
            waiting.decrementAndGet();
            if (!sequence.compareAndSet(startAt,endPlus)) {
                throw new InvalidParameterException("Serious logic error in synchronizer. This should never fail.");
            }
        }
        notifyAll();
    }

    public synchronized void awaitCompletion() {
        while (waiting.get()>0)
            try {
                wait(60_000);
            } catch (InterruptedException ignored) {
            }
    }

    private final static class TakeANumber implements Comparable<TakeANumber> {
        private final long start;
        private final long endPlus;
        private final Runnable task;

        public TakeANumber(long start, long endPlus, Runnable task) {
            this.start = start;
            this.endPlus = endPlus;
            this.task = task;
        }

        @Override
        public int compareTo(TakeANumber o) {
            return Long.compare(start, o.start);
        }

        public long getStart() {
            return start;
        }

        public long getEndPlus() {
            return endPlus;
        }

        public String toString() {
            return "[" + getStart() + "-" + getEndPlus() + ")";
        }
    }
}
