/*
 *   Copyright 2016 jshook
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package io.nosqlbench.activitytype.diag;

import com.codahale.metrics.Timer;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.core.MultiPhaseAction;
import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityapi.ratelimits.RateLimiter;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiagAction implements SyncAction, ActivityDefObserver, MultiPhaseAction {

    private final static Logger logger = LoggerFactory.getLogger(DiagAction.class);
    private final ActivityDef activityDef;
    private final DiagActivity diagActivity;

    private int slot;
    private long lastUpdate;
    private long quantizedInterval;
    private long reportModulo;
    private int phasesPerCycle;
    private int completedPhase;
    private int resultmodulo = Integer.MIN_VALUE;
    private long erroroncycle = Long.MIN_VALUE;
    private long throwoncycle = Long.MIN_VALUE;
    private boolean logcycle;
    private int staticvalue = Integer.MIN_VALUE;
    private RateLimiter diagRateLimiter = null;
    private Timer resultTimer;

    public DiagAction(int slot, ActivityDef activityDef, DiagActivity diagActivity) {
        this.activityDef = activityDef;
        this.slot = slot;
        this.diagActivity = diagActivity;

        onActivityDefUpdate(activityDef);
    }

    /**
     * idempotently assign the last append reference time and the interval which, when added to it, represent when this
     * diagnostic thread should takeUpTo its turn to log cycle info. Also, append the modulo parameter.
     */
    private void updateReportTime() {
        reportModulo = activityDef.getParams().getOptionalLong("modulo").orElse(10000000L);
        lastUpdate = System.currentTimeMillis() - calculateOffset(slot, activityDef);
        quantizedInterval = calculateInterval(activityDef);
        logger.trace("updating report time for slot:" + slot + ", def:" + activityDef + " to " + quantizedInterval
                + ", and modulo " + reportModulo);
    }

    private void updatePhases() {
        phasesPerCycle = activityDef.getParams().getOptionalInteger("phases").orElse(1);
    }

    /**
     * Calculate a reference point in the past which would have been this thread's time to append,
     * for use as a discrete reference point upon which the quantizedIntervals can be stacked to find the
     * ideal schedule.
     *
     * @param timeslot    - This thread's offset within the scheduled rotation, determined simply by thread enumeration
     * @param activityDef - the def for this activity instance
     * @return last time this thread would have updated
     */
    private long calculateOffset(long timeslot, ActivityDef activityDef) {
        long updateInterval = activityDef.getParams().getOptionalLong("interval").orElse(1000L);
        long offset = calculateInterval(activityDef) - (updateInterval * timeslot);
        return offset;
    }

    /**
     * Calculate how frequently a thread needs to append in order to achieve an aggregate append interval for
     * a given number of cooperating threads.
     *
     * @param activityDef - the def for this activity instance
     * @return long ms interval for this thread (the same for all threads, but calculated independently for each)
     */
    private long calculateInterval(ActivityDef activityDef) {
        long updateInterval = activityDef.getParams().getOptionalLong("interval").orElse(1000L);
        if (updateInterval == 0) { // Effectively disable this if it is set to 0 as an override.
            return Long.MAX_VALUE;
        }

        int threads = activityDef.getThreads();
        return updateInterval * threads;
    }


    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {
        updateReportTime();
        updatePhases();
        this.resultmodulo = activityDef.getParams().getOptionalInteger("resultmodulo").orElse(Integer.MIN_VALUE);
        this.erroroncycle = activityDef.getParams().getOptionalLong("erroroncycle").orElse(Long.MIN_VALUE);
        this.throwoncycle = activityDef.getParams().getOptionalLong("throwoncycle").orElse(Long.MIN_VALUE);
        this.logcycle = activityDef.getParams().getOptionalBoolean("logcycle").orElse(false);
        this.staticvalue = activityDef.getParams().getOptionalInteger("staticvalue").orElse(-1);
        this.diagRateLimiter = diagActivity.getDiagRateLimiter();
        this.resultTimer = this.diagActivity.getResultTimer();
    }

    @Override
    public boolean incomplete() {
        return (completedPhase < phasesPerCycle);
    }

    @Override
    public int runPhase(long value) {
        return runCycle(value);
    }

    @Override
    public int runCycle(long value) {

        if (logcycle) {
            logger.trace("cycle " + value);
        }

        try (Timer.Context timerctx = resultTimer.time()) {
            if (diagRateLimiter != null) {
                long waittime = diagRateLimiter.maybeWaitForOp();
            }

            long now = System.currentTimeMillis();
            if (completedPhase >= phasesPerCycle) {
                completedPhase = 0;
            }

            if ((now - lastUpdate) > quantizedInterval) {
                long delay = ((now - lastUpdate) - quantizedInterval);
                logger.info("diag action interval, input=" + value + ", phase=" + completedPhase + ", report delay=" + delay + "ms");
                lastUpdate += quantizedInterval;
                diagActivity.delayHistogram.update(delay);
            }

            if ((value % reportModulo) == 0) {
                logger.info("diag action   modulo, input=" + value + ", phase=" + completedPhase);
            }

            completedPhase++;

            int result = 0;

            if (resultmodulo >= 0) {
                if ((value % resultmodulo) == 0) {
                    result = 1;
                } else {
                    result = 0;
                }
            } else if (staticvalue >= 0) {
                return staticvalue;
            } else {
                result = (byte) (value % 128);
            }

            if (erroroncycle == value) {
                this.diagActivity.getActivityController().stopActivityWithReasonAsync("Diag was requested to stop on cycle " + erroroncycle);
            }

            if (throwoncycle == value) {
                throw new DiagDummyError("Diag was asked to throw an error on cycle " + throwoncycle);
            }

            return result;
        }

    }

}
