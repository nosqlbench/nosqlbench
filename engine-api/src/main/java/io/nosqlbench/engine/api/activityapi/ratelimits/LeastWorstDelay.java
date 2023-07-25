/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.api.activityapi.ratelimits;

import io.nosqlbench.engine.api.activityapi.sysperf.SysPerf;
import io.nosqlbench.engine.api.activityapi.sysperf.SysPerfData;

import java.util.concurrent.locks.LockSupport;

public class LeastWorstDelay {

    public final static SysPerfData perfdata = SysPerf.get().getPerfData(false);

    //private final static long sleepThreshold = (long) perfdata
    //.getAvgNanos_Thread_Sleep();
    //private final static long parkThreshold = (long) perfdata
    //.getAvgNanos_LockSupport_ParkNanos();

    private final static long sleepThreshold = 1_000_000;
    private final static long parkThreshold = 20;

    /**
     * We wish for the JVM to inline this.
     *
     * This method tries to block a thread for a period of time, with a balance of
     * accuracy and calling overhead. It does this by estimating the best way to
     * block according to the time to block for and some knowledge of how much
     * overhead and accuracy each method of blocking has. It's not perfect, but it
     * is marginally better than a CPU burning busy wait or a throughput killing sleep
     * right in the middle of every single thread.
     *
     * A better implementation would use sparse sampling of effective accuracy
     * and feedback into the offsets, to deal with variability in CPU availability.
     *
     * @param nanos nanoseconds to delay for
     */
    public static void delayAsIfFor(long nanos) {
        if (nanos > 0) {
            if (nanos > sleepThreshold) {
                nanos -= sleepThreshold;
                try {
                    Thread.sleep((nanos / 1000000), (int) (nanos % 1000000));
                } catch (InterruptedException ignored) {
                }
            } else if (nanos > parkThreshold) {
                nanos -= parkThreshold;
                LockSupport.parkNanos(nanos);
            }
        }
    }

    public static void debugDelayAsIfFor(long nanos) {
        if (nanos > 0) {
            if (nanos > sleepThreshold) {
                try {
                    System.out.printf("sleeping for %.9fS%n", ((double) nanos / 1E9));
                    Thread.sleep((nanos / 1000000), (int) (nanos % 1000000));
                } catch (InterruptedException ignored) {
                }
            } else if (nanos > parkThreshold) {
                System.out.printf("parking for %.9fS%n", ((double) nanos / 1E9));
                LockSupport.parkNanos(nanos);
            }
        }
    }

    /**
     * This method has a quirky name, because it does something a bit quirky.
     *
     * Inject delay, but do not await a condition that the delay is accurate
     * according to the real time clock. Return the presumed real time clock
     * value after the delay.
     *
     * This method is meant to provide lightweight delay when accuracy is not
     * as important as efficiency, and where the jitter in the result will not
     * result in an error that accumulates. Users must be careful to avoid using
     * this method in other scenarios.
     *
     * @param targetNanoTime The system nanos that the delay should attempt to return at.
     *                       perfect accuracy, which doesn't happen
     */
    public void delayAsIfUntil(long targetNanoTime) {
        long nanos = Math.max(targetNanoTime - System.nanoTime(), 0L);
        if (nanos > 0) {
            if (nanos > sleepThreshold) {
                nanos -= sleepThreshold;
                try {
                    Thread.sleep((nanos / 1000000), (int) (nanos % 1000000));
                } catch (InterruptedException ignored) {
                }
            } else if (nanos > parkThreshold) {
                nanos -= parkThreshold;
                LockSupport.parkNanos(nanos);
            } // else there is nothing shorter than this besides spinning, and we're not doing that
        }
    }


}
