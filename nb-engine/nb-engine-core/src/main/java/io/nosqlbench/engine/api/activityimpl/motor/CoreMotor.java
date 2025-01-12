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

import com.codahale.metrics.Timer;
import io.nosqlbench.engine.api.activityapi.core.*;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultSegmentBuffer;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultsSegment;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleSegment;
import io.nosqlbench.engine.api.activityimpl.MotorState;
import io.nosqlbench.engine.api.activityapi.core.ops.fluent.OpTracker;
import io.nosqlbench.engine.api.activityapi.input.Input;
import io.nosqlbench.engine.api.activityapi.output.Output;
import io.nosqlbench.engine.api.activityapi.simrate.RateLimiter;
import io.nosqlbench.engine.api.activityimpl.uniform.Activity;
import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.config.standard.*;
import io.nosqlbench.nb.api.labels.NBLabels;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static io.nosqlbench.engine.api.activityapi.core.RunState.*;

/**
 ActivityMotor is a Runnable which runs in one of an activity's many threads.
 It is the iteration harness for individual cycles of an activity. Each ActivityMotor
 instance is responsible for taking input from a LongSupplier and applying
 the provided LongConsumer to it on each cycle. These two parameters are called
 input and action, respectively.

 This motor implementation splits the handling of sync and async actions with a hard
 fork in the middle to limit potential breakage of the prior sync implementation
 with new async logic. */
public class CoreMotor<D> extends NBBaseComponent implements Motor<D>, Stoppable, NBReconfigurable {

    private static final Logger logger = LogManager.getLogger(CoreMotor.class);

    private final long slotId;
    private final Activity activity;

    private Timer inputTimer;

    private RateLimiter strideRateLimiter;
    private Timer stridesServiceTimer;
    private Timer stridesResponseTimer;

    private RateLimiter cycleRateLimiter;
    private Timer cycleServiceTimer;
    private Timer cycleResponseTimer;

    private Input input;
    private SyncAction action;
    //    private final StandardActivity activity;
    private Output output;

    private final MotorState motorState;
    //    private final AtomicReference<RunState> slotState;
    private int stride = 1;

    private OpTracker<D> opTracker;
    private NBConfiguration config;


    /**
     Create an ActivityMotor.

     //     * @param activity The activity that this motor will be associated with.
     @param slotId
     The enumeration of the motor, as assigned by its executor.
     @param input
     A LongSupplier which provides the cycle number inputs.
     */
    public CoreMotor(
        Activity activity,
        long slotId,
        Input input,
        SyncAction action,
        Output output
    )
    {
        super(activity, NBLabels.forKV("motor", slotId));
        this.activity = activity;
        this.slotId = slotId;
        setInput(input);
        setResultOutput(output);
        motorState = new MotorState(slotId, activity.getRunStateTally());
        applyConfig(activity.getConfig());
        this.action = action;

        int hdrdigits = activity.getComponentProp("hdr_digits").map(Integer::parseInt).orElse(3);


    }

    /**
     Set the input for this ActivityMotor.
     @param input
     The LongSupplier that provides the cycle number.
     @return this ActivityMotor, for chaining
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

            strideRateLimiter = activity.getStrideLimiter();
            cycleRateLimiter = activity.getCycleLimiter();

            if (motorState.get() == Finished) {
                logger.warn(() -> "Input was already exhausted for slot " +
                    slotId +
                    ", remaining in finished state.");
            }

            action.init();

            if (input instanceof Startable) {
                ((Startable) input).start();
            }

            if (strideRateLimiter != null) {
                // block for strides rate limiter
                strideRateLimiter.block();
            }

            long strideDelay = 0L;
            long cycleDelay = 0L;

            motorState.enterState(Running);
            while (motorState.get() == Running) {

                CycleSegment cycleSegment = null;
                CycleResultSegmentBuffer segBuffer = new CycleResultSegmentBuffer(stride);

                try (Timer.Context inputTime = activity.metrics.inputTimer.time()) {
                    cycleSegment = input.getInputSegment(stride);
                }

                if (cycleSegment == null) {
                    logger.trace(() -> "input exhausted (input " +
                        input +
                        ") via null segment, stopping motor thread " +
                        slotId);
                    motorState.enterState(Finished);
                    continue;
                }


                if (strideRateLimiter != null) {
                    // block for strides rate limiter
                    strideDelay = strideRateLimiter.block();
                }

                long strideStart = System.nanoTime();
                try {

                    while (!cycleSegment.isExhausted()) {
                        long cyclenum = cycleSegment.nextCycle();
                        if (cyclenum < 0) {
                            if (cycleSegment.isExhausted()) {
                                logger.trace(() -> "input exhausted (input " +
                                    input +
                                    ") via negative read, stopping motor thread " +
                                    slotId);
                                motorState.enterState(Finished);
                                continue;
                            }
                        }

                        if (motorState.get() != Running) {
                            logger.trace(() -> "motor stopped after input (input " +
                                cyclenum +
                                "), stopping motor thread " +
                                slotId);
                            continue;
                        }
                        int result = -1;

                        if (cycleRateLimiter != null) {
                            // Block for cycle rate limiter
                            cycleDelay = cycleRateLimiter.block();
                        }

                        long cycleStart = System.nanoTime();
                        try {
                            logger.trace(() -> "cycle " + cyclenum);
                            result = action.runCycle(cyclenum);
                        } catch (Exception e) {
                            motorState.enterState(Errored);
                            throw e;
                        } finally {
                            long cycleEnd = System.nanoTime();
                            activity.metrics.cycleServiceTimer.update(
                                (cycleEnd - cycleStart) + cycleDelay, TimeUnit.NANOSECONDS);
                        }
                        segBuffer.append(cyclenum, result);
                    }

                } finally {
                    long strideEnd = System.nanoTime();
                    activity.metrics.stridesServiceTimer.update(
                        (strideEnd - strideStart) + strideDelay,
                        TimeUnit.NANOSECONDS);
                }

                if (output != null) {
                    CycleResultsSegment outputBuffer = segBuffer.toReader();
                    try {
                        output.onCycleResultSegment(outputBuffer);
                    } catch (Exception t) {
                        logger.error(() -> "Error while feeding result segment " +
                            outputBuffer +
                            " to output '" +
                            output +
                            "', error:" +
                            t);
                        throw t;
                    }
                }
            }

            if (motorState.get() == Stopping) {
                motorState.enterState(Stopped);
                logger.trace(() -> Thread.currentThread().getName() +
                    " shutting down as " +
                    motorState.get());
            } else if (motorState.get() == Finished) {
                logger.trace(() -> Thread.currentThread().getName() +
                    " shutting down as " +
                    motorState.get());
            } else {
                logger.warn(
                    () -> "Unexpected motor state for CoreMotor shutdown: " + motorState.get());
            }

        } catch (Throwable t) {
            logger.error(() -> "Error in core motor loop:" + t, t);
            motorState.enterState(Errored);
            throw t;
        }
    }

    @Override
    public String toString() {
        return this.activity.getAlias() + ": slot:" + this.slotId + "; state:" + motorState.get();
    }

    @Override
    public void applyConfig(NBConfiguration cfg) {
        NBConfigurable.applyMatching(cfg, new Object[]{input, opTracker, action, output});
        this.config = getConfigModel().matchConfig(cfg);
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
            logger.warn(() -> "attempted to stop motor " +
                this.getSlotId() +
                ": from non Running state:" +
                currentState);
        }
    }

    public void setResultOutput(Output resultOutput) {
        this.output = resultOutput;
    }


    @Override
    public NBConfigModel getConfigModel() {
        return ConfigModel.of(CoreMotor.class).add(Param.required("stride", Integer.class))
            .asReadOnly();
    }

    @Override
    public void applyReconfig(NBConfiguration recfg) {
        applyConfig(recfg);
    }

    @Override
    public NBConfigModel getReconfigModel() {
        return getConfigModel();
    }
}
