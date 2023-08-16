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
package io.nosqlbench.engine.api.activityimpl.motor;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import io.nosqlbench.engine.api.activityapi.core.*;
import io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets.TrackedOp;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.op_output.StrideOutputConsumer;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultSegmentBuffer;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultsSegment;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleSegment;
import io.nosqlbench.engine.api.activityimpl.MotorState;
import io.nosqlbench.engine.api.activityapi.core.ops.fluent.OpTracker;
import io.nosqlbench.engine.api.activityapi.core.ops.fluent.OpTrackerImpl;
import io.nosqlbench.engine.api.activityapi.input.Input;
import io.nosqlbench.engine.api.activityapi.output.Output;
import io.nosqlbench.engine.api.activityapi.ratelimits.RateLimiter;
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static io.nosqlbench.engine.api.activityapi.core.RunState.*;

/**
 * ActivityMotor is a Runnable which runs in one of an activity's many threads.
 * It is the iteration harness for individual cycles of an activity. Each ActivityMotor
 * instance is responsible for taking input from a LongSupplier and applying
 * the provided LongConsumer to it on each cycle. These two parameters are called
 * input and action, respectively.
 *
 * This motor implementation splits the handling of sync and async actions with a hard
 * fork in the middle to limit potential breakage of the prior sync implementation
 * with new async logic.
 */
public class CoreMotor<D> implements ActivityDefObserver, Motor<D>, Stoppable {

    private static final Logger logger = LogManager.getLogger(CoreMotor.class);

    private final long slotId;

    private Timer inputTimer;

    private RateLimiter strideRateLimiter;
    private Timer strideServiceTimer;
    private Timer stridesResponseTimer;

    private RateLimiter cycleRateLimiter;
    private Timer cycleServiceTimer;
    private Timer cycleResponseTimer;

    private Input input;
    private Action action;
    private final Activity activity;
    private Output output;

    private final MotorState motorState;
    //    private final AtomicReference<RunState> slotState;
    private int stride = 1;

    private OpTracker<D> opTracker;
    private Counter optrackerBlockCounter;


    /**
     * Create an ActivityMotor.
     *
     * @param activity The activity that this motor will be associated with.
     * @param slotId   The enumeration of the motor, as assigned by its executor.
     * @param input    A LongSupplier which provides the cycle number inputs.
     */
    public CoreMotor(
        Activity activity,
        long slotId,
        Input input) {
        this.activity = activity;
        this.slotId = slotId;
        setInput(input);
        motorState = new MotorState(slotId, activity.getRunStateTally());
        onActivityDefUpdate(activity.getActivityDef());
    }


    /**
     * Create an ActivityMotor.
     *
     * @param activity The activity that this motor is based on.
     * @param slotId   The enumeration of the motor, as assigned by its executor.
     * @param input    A LongSupplier which provides the cycle number inputs.
     * @param action   An LongConsumer which is applied to the input for each cycle.
     */
    public CoreMotor(
        Activity activity,
        long slotId,
        Input input,
        Action action
    ) {
        this(activity, slotId, input);
        setAction(action);
    }

    /**
     * Create an ActivityMotor.
     *
     * @param activity The activity that this motor is based on.
     * @param slotId   The enumeration of the motor, as assigned by its executor.
     * @param input    A LongSupplier which provides the cycle number inputs.
     * @param action   An LongConsumer which is applied to the input for each cycle.
     * @param output   An optional opTracker.
     */
    public CoreMotor(
        Activity activity,
        long slotId,
        Input input,
        Action action,
        Output output
    ) {
        this(activity, slotId, input);
        setAction(action);
        setResultOutput(output);
    }

    /**
     * Set the input for this ActivityMotor.
     *
     * @param input The LongSupplier that provides the cycle number.
     * @return this ActivityMotor, for chaining
     */
    @Override
    public Motor<D> setInput(Input input) {
        this.input = input;
        return this;
    }

    @Override
    public Input getInput() {
        return input;
    }


    /**
     * Set the action for this ActivityMotor.
     *
     * @param action The LongConsumer that will be applied to the next cycle number.
     * @return this ActivityMotor, for chaining
     */
    @Override
    public Motor<D> setAction(Action action) {
        this.action = action;
        return this;
    }

    @Override
    public Action getAction() {
        return action;
    }

    @Override
    public long getSlotId() {
        return this.slotId;
    }

    @Override
    public MotorState getState() {
        return motorState;
    }

    @Override
    public void removeState() {
        motorState.removeState();
    }

    @Override
    public void run() {
        motorState.enterState(Starting);

        try {
            inputTimer = activity.getInstrumentation().getOrCreateInputTimer();
            strideServiceTimer = activity.getInstrumentation().getOrCreateStridesServiceTimer();
            stridesResponseTimer = activity.getInstrumentation().getStridesResponseTimerOrNull();
            optrackerBlockCounter = activity.getInstrumentation().getOrCreateOpTrackerBlockedCounter();

            strideRateLimiter = activity.getStrideLimiter();
            cycleRateLimiter = activity.getCycleLimiter();


            if (motorState.get() == Finished) {
                logger.warn(() -> "Input was already exhausted for slot " + slotId + ", remaining in finished state.");
            }

            action.init();

            if (input instanceof Startable) {
                ((Startable) input).start();
            }

            if (strideRateLimiter != null) {
                // block for strides rate limiter
                strideRateLimiter.start();
            }

            long strideDelay = 0L;
            long cycleDelay = 0L;

            // Reviewer Note: This separate of code paths was used to avoid impacting the
            // previously logic for the SyncAction type. It may be consolidated later once
            // the async action is proven durable
            if (action instanceof AsyncAction) {

                @SuppressWarnings("unchecked")
                AsyncAction<D> async = (AsyncAction) action;

                opTracker = new OpTrackerImpl<>(activity, slotId);
                opTracker.setCycleOpFunction(async.getOpInitFunction());

                StrideOutputConsumer<D> strideconsumer = null;
                if (action instanceof StrideOutputConsumer) {
                    strideconsumer = (StrideOutputConsumer<D>) async;
                }

                motorState.enterState(Running);
                while (motorState.get() == Running) {

                    CycleSegment cycleSegment = null;

                    try (Timer.Context inputTime = inputTimer.time()) {
                        cycleSegment = input.getInputSegment(stride);
                    }

                    if (cycleSegment == null) {
                        logger.trace(() -> "input exhausted (input " + input + ") via null segment, stopping motor thread " + slotId);
                        motorState.enterState(Finished);
                        continue;
                    }

                    if (strideRateLimiter != null) {
                        // block for strides rate limiter
                        strideDelay = strideRateLimiter.maybeWaitForOp();
                    }

                    StrideTracker<D> strideTracker = new StrideTracker<>(
                        strideServiceTimer,
                        stridesResponseTimer,
                        strideDelay,
                        cycleSegment.peekNextCycle(),
                        stride,
                        output,
                        strideconsumer);
                    strideTracker.start();

                    long strideStart = System.nanoTime();

                    while (!cycleSegment.isExhausted() && motorState.get() == Running) {
                        long cyclenum = cycleSegment.nextCycle();
                        if (cyclenum < 0) {
                            if (cycleSegment.isExhausted()) {
                                logger.trace(() -> "input exhausted (input " + input + ") via negative read, stopping motor thread " + slotId);
                                motorState.enterState(Finished);
                                continue;
                            }
                        }

                        if (motorState.get() != Running) {
                            logger.trace(()->"motor stopped in cycle " + cyclenum + ", stopping motor thread " + slotId);
                            continue;
                        }

                        if (cycleRateLimiter != null) {
                            // Block for cycle rate limiter
                            cycleDelay = cycleRateLimiter.maybeWaitForOp();
                        }

                        try {
                            TrackedOp<D> op = opTracker.newOp(cyclenum, strideTracker);
                            op.setWaitTime(cycleDelay);

                            synchronized (opTracker) {
                                while (opTracker.isFull()) {
                                    try {
                                        logger.trace(() -> "Blocking for enqueue with (" + opTracker.getPendingOps() + "/" + opTracker.getMaxPendingOps() + ") queued ops");
                                        optrackerBlockCounter.inc();
                                        opTracker.wait(10000);
                                    } catch (InterruptedException ignored) {
                                    }
                                }
                            }

                            async.enqueue(op);

                        } catch (Exception t) {
                            logger.error(()->"Error while processing async cycle " + cyclenum + ", error:" + t);
                            throw t;
                        }
                    }


                }

                if (motorState.get() == Finished) {
                    boolean finished = opTracker.awaitCompletion(60000);
                    if (finished) {
                        logger.debug(() -> "slot " + this.slotId + " completed successfully");
                    } else {
                        logger.warn(() -> "slot " + this.slotId + " was stopped before completing successfully");
                    }
                }

                if (motorState.get() == Stopping) {
                    motorState.enterState(Stopped);
                }


            } else if (action instanceof SyncAction sync) {

                cycleServiceTimer = activity.getInstrumentation().getOrCreateCyclesServiceTimer();
                strideServiceTimer = activity.getInstrumentation().getOrCreateStridesServiceTimer();

                if (activity.getActivityDef().getParams().containsKey("async")) {
                    throw new RuntimeException("The async parameter was given for this activity, but it does not seem to know how to do async.");
                }

                motorState.enterState(Running);
                while (motorState.get() == Running) {

                    CycleSegment cycleSegment = null;
                    CycleResultSegmentBuffer segBuffer = new CycleResultSegmentBuffer(stride);

                    try (Timer.Context inputTime = inputTimer.time()) {
                        cycleSegment = input.getInputSegment(stride);
                    }

                    if (cycleSegment == null) {
                        logger.trace(() -> "input exhausted (input " + input + ") via null segment, stopping motor thread " + slotId);
                        motorState.enterState(Finished);
                        continue;
                    }


                    if (strideRateLimiter != null) {
                        // block for strides rate limiter
                        strideDelay = strideRateLimiter.maybeWaitForOp();
                    }

                    long strideStart = System.nanoTime();
                    try {

                        while (!cycleSegment.isExhausted()) {
                            long cyclenum = cycleSegment.nextCycle();
                            if (cyclenum < 0) {
                                if (cycleSegment.isExhausted()) {
                                    logger.trace(() -> "input exhausted (input " + input + ") via negative read, stopping motor thread " + slotId);
                                    motorState.enterState(Finished);
                                    continue;
                                }
                            }

                            if (motorState.get() != Running) {
                                logger.trace(() -> "motor stopped after input (input " + cyclenum + "), stopping motor thread " + slotId);
                                continue;
                            }
                            int result = -1;

                            if (cycleRateLimiter != null) {
                                // Block for cycle rate limiter
                                cycleDelay = cycleRateLimiter.maybeWaitForOp();
                            }

                            long cycleStart = System.nanoTime();
                            try {
                                logger.trace(()->"cycle " + cyclenum);
                                result = sync.runCycle(cyclenum);
                            } catch (Exception e) {
                                motorState.enterState(Errored);
                                throw e;
                            } finally {
                                long cycleEnd = System.nanoTime();
                                cycleServiceTimer.update((cycleEnd - cycleStart) + cycleDelay, TimeUnit.NANOSECONDS);
                            }
                            segBuffer.append(cyclenum, result);
                        }

                    } finally {
                        long strideEnd = System.nanoTime();
                        strideServiceTimer.update((strideEnd - strideStart) + strideDelay, TimeUnit.NANOSECONDS);
                    }

                    if (output != null) {
                        CycleResultsSegment outputBuffer = segBuffer.toReader();
                        try {
                            output.onCycleResultSegment(outputBuffer);
                        } catch (Exception t) {
                            logger.error(()->"Error while feeding result segment " + outputBuffer + " to output '" + output + "', error:" + t);
                            throw t;
                        }
                    }
                }

            } else {
                throw new RuntimeException("Valid Action implementations must implement either the SyncAction or the AsyncAction sub-interface");
            }

            if (motorState.get() == Stopping) {
                motorState.enterState(Stopped);
                logger.trace(() -> Thread.currentThread().getName() + " shutting down as " + motorState.get());
            } else if (motorState.get() == Finished) {
                logger.trace(() -> Thread.currentThread().getName() + " shutting down as " + motorState.get());
            } else {
                logger.warn(()->"Unexpected motor state for CoreMotor shutdown: " + motorState.get());
            }

        } catch (Throwable t) {
            logger.error(()->"Error in core motor loop:" + t, t);
            motorState.enterState(Errored);
            throw t;
        }
    }

    @Override
    public String toString() {
        return "slot:" + this.slotId + "; state:" + motorState.get();
    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {

        for (Object component : (new Object[]{input, opTracker, action, output})) {
            if (component instanceof ActivityDefObserver) {
                ((ActivityDefObserver) component).onActivityDefUpdate(activityDef);
            }
        }

        this.stride = activityDef.getParams().getOptionalInteger("stride").orElse(1);
        strideRateLimiter = activity.getStrideLimiter();
        cycleRateLimiter = activity.getCycleLimiter();

    }

    @Override
    public synchronized void requestStop() {
        RunState currentState = motorState.get();
        if (Objects.requireNonNull(currentState) == Running) {
            Stoppable.stop(input, action);
            motorState.enterState(Stopping);
        } else {
            logger.warn(() -> "attempted to stop motor " + this.getSlotId() + ": from non Running state:" + currentState);
        }
    }

    public void setResultOutput(Output resultOutput) {
        this.output = resultOutput;
    }

}
