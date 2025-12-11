/*
 * Copyright (c) 2024 nosqlbench
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

import com.codahale.metrics.Timer;
import io.nosqlbench.engine.api.activityapi.core.*;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultSegmentBuffer;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleSegment;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultsSegment;
import io.nosqlbench.engine.api.activityapi.input.Input;
import io.nosqlbench.engine.api.activityapi.output.Output;
import io.nosqlbench.engine.api.activityapi.simrate.RateLimiter;
import io.nosqlbench.engine.api.activityimpl.MotorState;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityDef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/// Virtual-thread-friendly stride runner that replaces CoreMotor for sync actions.
/// Pulls cycle segments from input and delegates execution to {@link StrideAction#runStride},
/// applying stride/cycle rate limiting and buffering results.
public class StrideMotor<D> implements Motor<D>, ActivityDefObserver {

    private static final Logger logger = LogManager.getLogger(StrideMotor.class);

    private final Activity activity;
    private final long slotId;
    private Input input;
    private final StrideAction action;
    private Output output;
    private final MotorState motorState;
    private RateLimiter strideRateLimiter;
    private RateLimiter cycleRateLimiter;
    private Timer inputTimer;
    private Timer strideServiceTimer;
    private Timer cycleServiceTimer;

    public StrideMotor(Activity activity, long slotId, Input input, StrideAction action, Output output) {
        this.activity = activity;
        this.slotId = slotId;
        this.input = input;
        this.action = action;
        this.output = output;
        this.motorState = new MotorState(slotId, activity.getRunStateTally());
        onActivityDefUpdate(activity.getActivityDef());
    }

    @Override
    public void run() {
        motorState.enterState(RunState.Starting);
        try {
            inputTimer = activity.getInstrumentation().getOrCreateInputTimer();
            strideServiceTimer = activity.getInstrumentation().getOrCreateStridesServiceTimer();
            cycleServiceTimer = activity.getInstrumentation().getOrCreateCyclesServiceTimer();

            if (motorState.get() == RunState.Finished) {
                logger.warn(() -> "Input was already exhausted for slot " + slotId + ", remaining in finished state.");
            }

            action.init();
            if (input instanceof Startable startable) {
                startable.start();
            }

            if (strideRateLimiter != null) {
                strideRateLimiter.block();
            }

            long strideDelay = 0L;

            motorState.enterState(RunState.Running);
            while (motorState.get() == RunState.Running) {
                CycleSegment cycleSegment;
                CycleResultSegmentBuffer segBuffer;
                if (action instanceof io.nosqlbench.engine.api.activityimpl.uniform.actions.StandardAction std) {
                    std.resetStrideBuffers();
                    segBuffer = std.resultBuffer();
                } else {
                    segBuffer = new CycleResultSegmentBuffer(activity.getActivityDef().getParams().getOptionalInteger("stride").orElse(1));
                }

                try (Timer.Context ignored = inputTimer.time()) {
                    cycleSegment = input.getInputSegment(activity.getParams().getOptionalInteger("stride").orElse(1));
                }

                if (cycleSegment == null) {
                    logger.trace(() -> "input exhausted via null segment, stopping motor thread " + slotId);
                    motorState.enterState(RunState.Finished);
                    continue;
                }

                if (strideRateLimiter != null) {
                    strideDelay = strideRateLimiter.block();
                }

                long strideStart = System.nanoTime();
                try {
                    long[] cycleDelayHolder = new long[1];
                    long[] cycleStartHolder = new long[1];
                    Runnable beforeCycle = () -> {
                        cycleStartHolder[0] = System.nanoTime();
                        if (cycleRateLimiter != null) {
                            cycleDelayHolder[0] = cycleRateLimiter.block();
                        } else {
                            cycleDelayHolder[0] = 0L;
                        }
                    };
                    java.util.function.BiConsumer<Long, Integer> afterCycle = (cyclenum, result) -> {
                        long cycleEnd = System.nanoTime();
                        cycleServiceTimer.update((cycleEnd - cycleStartHolder[0]) + cycleDelayHolder[0], TimeUnit.NANOSECONDS);
                        segBuffer.append(cyclenum, result);
                    };
                    action.runStride(cycleSegment, beforeCycle, afterCycle);
                } catch (Exception e) {
                    motorState.enterState(RunState.Errored);
                    throw e;
                } finally {
                    long strideEnd = System.nanoTime();
                    strideServiceTimer.update((strideEnd - strideStart) + strideDelay, TimeUnit.NANOSECONDS);
                }

                if (output != null) {
                    CycleResultsSegment outputBuffer = segBuffer.toReader();
                    try {
                        output.onCycleResultSegment(outputBuffer);
                    } catch (Exception t) {
                        logger.error(() -> "Error while feeding result segment " + outputBuffer + " to output '" + output + "', error:" + t);
                        throw t;
                    }
                }
            }

            if (motorState.get() == RunState.Stopping) {
                motorState.enterState(RunState.Stopped);
                logger.trace(() -> Thread.currentThread().getName() + " shutting down as " + motorState.get());
            } else if (motorState.get() == RunState.Finished) {
                logger.trace(() -> Thread.currentThread().getName() + " shutting down as " + motorState.get());
            } else {
                logger.warn(() -> "Unexpected motor state for StrideMotor shutdown: " + motorState.get());
            }

        } catch (Throwable t) {
            logger.error(() -> "Error in stride motor loop:" + t, t);
            motorState.enterState(RunState.Errored);
            throw t;
        }
    }

    @Override
    public Motor<D> setInput(Input input) {
        this.input = input;
        return this;
    }

    @Override
    public Input getInput() {
        return input;
    }

    @Override
    public Motor<D> setAction(Action action) {
        throw new BasicError("StrideMotor requires a StrideAction; set at construction time.");
    }

    @Override
    public Action getAction() {
        return this.action;
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
    public void requestStop() {
        RunState currentState = motorState.get();
        if (Objects.requireNonNull(currentState) == RunState.Running) {
            Stoppable.stop(input, action);
            motorState.enterState(RunState.Stopping);
        } else {
            logger.warn(() -> "attempted to stop motor " + this.getSlotId() + ": from non Running state:" + currentState);
        }
    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {
        ActivityDefObserver.apply(activityDef, input, null, action, output);
        this.strideRateLimiter = activity.getStrideLimiter();
        this.cycleRateLimiter = activity.getCycleLimiter();
    }
}
