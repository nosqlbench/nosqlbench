/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */
package io.nosqlbench.activitytype.diag;

import io.nosqlbench.engine.api.activityapi.core.BaseAsyncAction;
import io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets.CompletedOp;
import io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets.StartedOp;
import io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets.TrackedOp;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.op_output.StrideOutputConsumer;
import io.nosqlbench.engine.api.activityapi.ratelimits.RateLimiter;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.ParameterMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.LockSupport;
import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;
import java.util.stream.Collectors;

public class AsyncDiagAction extends BaseAsyncAction<DiagOpData, DiagActivity> implements Thread.UncaughtExceptionHandler, StrideOutputConsumer<DiagOpData> {

    private final static Logger logger = LoggerFactory.getLogger(AsyncDiagAction.class);

    private long lastUpdate;
    private long quantizedInterval;
    private long reportModulo;
    private int phasesPerCycle;
    private int completedPhase;
    private long erroroncycle = Long.MIN_VALUE;
    private long throwoncycle = Long.MIN_VALUE;
    private boolean logcycle;
    private RateLimiter diagRateLimiter = null;

    private LongToIntFunction resultFunc;
    private LongUnaryOperator delayFunc;


//    private PriorityBlockingQueue<StartedOp<DiagOpData>> asyncOps;
    private LinkedBlockingDeque<StartedOp<DiagOpData>> opQueue;
    private OpFinisher finisher;
    private boolean enableOutputProcessing;


    public AsyncDiagAction(DiagActivity activity, int slot) {
        super(activity, slot);
        onActivityDefUpdate(activity.getActivityDef());
    }

    @Override
    public void requestStop() {
        super.requestStop();

    }

    /**
     * assign the last append reference time and the interval which, when added to it, represent when this
     * diagnostic thread should take its turn to log cycle info. Also, append the modulo parameter.
     */
    private void updateReportTime() {
        ParameterMap params = this.activity.getActivityDef().getParams();
        reportModulo = params.getOptionalLong("modulo").orElse(10000000L);
        lastUpdate = System.currentTimeMillis() - calculateOffset(slot, params);
        quantizedInterval = calculateInterval(params, activity.getActivityDef().getThreads());
        logger.trace("updating report time for slot:" + slot + ", def:" + params + " to " + quantizedInterval
                + ", and modulo " + reportModulo);
    }

    /**
     * Calculate a reference point in the past which would have been this thread's time to append,
     * for use as a discrete reference point upon which the quantizedIntervals can be stacked to find the
     * ideal schedule.
     *
     * @param timeslot - This thread's offset within the scheduled rotation, determined simply by thread enumeration
     * @param params   - the def for this activity instance
     * @return last time this thread would have updated
     */
    private long calculateOffset(long timeslot, ParameterMap params) {
        long updateInterval = params.getOptionalLong("interval").orElse(1000L);
        long offset = calculateInterval(params, activity.getActivityDef().getThreads()) - (updateInterval * timeslot);
        return offset;
    }

    /**
     * Calculate how frequently a thread needs to append in order to achieve an aggregate append interval for
     * a given number of cooperating threads.
     *
     * @param params - the def for this activity instance
     * @return long ms interval for this thread (the same for all threads, but calculated independently for each)
     */
    private long calculateInterval(ParameterMap params, int threads) {
        long updateInterval = params.getOptionalLong("interval").orElse(1000L);
        if (updateInterval == 0) { // Effectively disable this if it is set to 0 as an override.
            return Long.MAX_VALUE;
        }

        return updateInterval * threads;
    }


    @Override
    public void init() {

//        this.asyncOps = new PriorityBlockingQueue<>(1, Comparator.comparingLong(o -> o.getData().getSimulatedDelayNanos()));
        this.opQueue = new LinkedBlockingDeque<StartedOp<DiagOpData>>();
        this.finisher = new OpFinisher(activity.getAlias()+"_finisher_" + slot, opQueue, this);
    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);

        ParameterMap params = activityDef.getParams();
        updateReportTime();

        this.delayFunc = activity.getDelayFunc();
        this.resultFunc = activity.getResultFunc();

        this.erroroncycle = params.getOptionalLong("erroroncycle").orElse(Long.MIN_VALUE);
        this.throwoncycle = params.getOptionalLong("throwoncycle").orElse(Long.MIN_VALUE);
        this.logcycle = params.getOptionalBoolean("logcycle").orElse(false);

        this.diagRateLimiter = activity.getDiagRateLimiter();

        this.enableOutputProcessing = params.getOptionalBoolean("enable_output_processing").orElse(false);
    }

    @Override
    public LongFunction<DiagOpData> getOpInitFunction() {
        return (l) -> new DiagOpData("a diag op");
    }


    @Override
    public void startOpCycle(TrackedOp<DiagOpData> opc) {
        opc.getOpData().log("starting at " + System.nanoTime());
        opc.getOpData().setSimulatedDelayNanos(delayFunc.applyAsLong(opc.getCycle()));
        StartedOp<DiagOpData> started = opc.start();
        opQueue.add(started);
    }

    private int backendExecuteOp(StartedOp<DiagOpData> opc) {

        long cycle = opc.getCycle();

        if (logcycle) {
            logger.trace("cycle " + cycle);
        }

        if (diagRateLimiter != null) {
            diagRateLimiter.maybeWaitForOp();
        }

        long now = System.currentTimeMillis();
        if (completedPhase >= phasesPerCycle) {
            completedPhase = 0;
        }

        if ((now - lastUpdate) > quantizedInterval) {
            long delay = ((now - lastUpdate) - quantizedInterval);
            logger.info("diag action interval, input=" + cycle + ", phase=" + completedPhase + ", report delay=" + delay + "ms");
            lastUpdate += quantizedInterval;
            activity.delayHistogram.update(delay);
        }

        if ((cycle % reportModulo) == 0) {
            logger.info("diag action   modulo, input=" + cycle + ", phase=" + completedPhase);
        }

        completedPhase++;

        int result = resultFunc.applyAsInt(cycle);

        if (erroroncycle == cycle) {
            activity.getActivityController().stopActivityWithReasonAsync("Diag was requested to stop on cycle " + erroroncycle);
        }

        if (throwoncycle == cycle) {
            throw new RuntimeException("Diag was asked to throw an error on cycle " + throwoncycle);
        }

        return result;

    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        logger.error("Error on finisher thread: " +t.getName() + ", error: " + e.getMessage());
        requestStop();
    }


    private static class OpFinisher implements Runnable {
        final BlockingQueue<StartedOp<DiagOpData>> queue;
        private final AsyncDiagAction action;
        AsyncDiagAction mainContext;
        private volatile boolean running = true;
        private Thread thread;
        private String name;

        public OpFinisher(String name, BlockingQueue<StartedOp<DiagOpData>> queue, AsyncDiagAction action) {
            this.queue = queue;
            this.action=action;
            this.name = name;

            thread = new Thread(this);
            thread.setName(name);
            thread.setUncaughtExceptionHandler(action);
            thread.start();
        }

        public void requestStop() {
            running=false;
        }

        @Override
        public void run() {
            logger.debug("stopping finisher thread for diagnostic action " + name);
            while (running) {
                StartedOp<DiagOpData> opc;
                try {
                    opc=queue.take();

                    DiagOpData op = opc.getOpData();
                    long now = System.nanoTime();
                    long simulatedCompletionTime = opc.getStartedAtNanos() + op.getSimulatedDelayNanos();

                    long nanodelay = Math.max(0L,simulatedCompletionTime-now);
                    if (nanodelay>=1000) { // It's not worth calling this for very small values
                        LockSupport.parkNanos(nanodelay);
                    }

                    int result = action.backendExecuteOp(opc);
                    if (result==0) {
                        opc.succeed(result);
                    } else {
                        opc.fail(result);
                    }
                } catch (InterruptedException ignored) {
                }

            }
            logger.debug("stopping finisher thread for diagnostic action " + name);

        }
    }

    @Override
    public void onStrideOutput(List<CompletedOp<DiagOpData>> completedOps) {
        if (enableOutputProcessing) {
            logger.info("processing stride output for " + completedOps.get(0).getCycle());
            long start = completedOps.get(0).getCycle();
            long endPlus = completedOps.get(completedOps.size()-1).getCycle()+1;
            String diagLog = completedOps.get(0).getOpData().getDiagLog().stream().collect(Collectors.joining("\n"));
            activity.getSequenceBlocker().awaitAndRun(start, endPlus, () -> logger.info(" => " + start + " -> " + endPlus + ": " + diagLog));
        }
    }


}
