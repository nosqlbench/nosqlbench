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

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.*;
import io.nosqlbench.adapters.api.evalctx.CycleFunction;
import io.nosqlbench.engine.api.activityapi.core.*;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultSegmentBuffer;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultsSegment;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleSegment;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorDetail;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.NBErrorHandler;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityapi.core.ops.fluent.OpTracker;
import io.nosqlbench.engine.api.activityapi.input.Input;
import io.nosqlbench.engine.api.activityapi.output.Output;
import io.nosqlbench.engine.api.activityapi.simrate.RateLimiter;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.nb.api.errors.ResultVerificationError;
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
 * <p>
 * This motor implementation splits the handling of sync and async actions with a hard
 * fork in the middle to limit potential breakage of the prior sync implementation
 * with new async logic.
 */
public class CoreMotor<D> implements ActivityDefObserver, Motor<D>, SyncAction {

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
    private final Activity activity;
    private Output output;

    private int stride = 1;

    private OpTracker<D> opTracker;
    private final Timer executeTimer;
    private final Histogram triesHistogram;
    private final Timer resultSuccessTimer;
    private final Timer resultTimer;
    private final Timer bindTimer;
    private final NBErrorHandler errorHandler;
    private final OpSequence<OpDispenser<? extends Op>> opsequence;
    private final int maxTries;
    private final Timer verifierTimer;


    /**
     * Create an ActivityMotor.
     *
     * @param activity
     *     The activity that this motor will be associated with.
     * @param slotId
     *     The enumeration of the motor, as assigned by its executor.
     * @param input
     *     A LongSupplier which provides the cycle number inputs.
     */
    public CoreMotor(
        SimpleActivity activity,
        long slotId,
        Input input) {
        this.activity = activity;
        this.slotId = slotId;
        setInput(input);
        onActivityDefUpdate(activity.getActivityDef());
        this.opsequence = activity.getOpSequence();
        this.maxTries = activity.getMaxTries();
        bindTimer = activity.getInstrumentation().getOrCreateBindTimer();
        executeTimer = activity.getInstrumentation().getOrCreateExecuteTimer();
        triesHistogram = activity.getInstrumentation().getOrCreateTriesHistogram();
        resultTimer = activity.getInstrumentation().getOrCreateResultTimer();
        resultSuccessTimer = activity.getInstrumentation().getOrCreateResultSuccessTimer();
        errorHandler = activity.getErrorHandler();
        verifierTimer = activity.getInstrumentation().getOrCreateVerifierTimer();

    }


    /**
     * Create an ActivityMotor.
     *
     * @param activity
     *     The activity that this motor is based on.
     * @param slotId
     *     The enumeration of the motor, as assigned by its executor.
     * @param input
     *     A LongSupplier which provides the cycle number inputs.
     * @param output
     *     An optional opTracker.
     */
    public CoreMotor(
        SimpleActivity activity,
        long slotId,
        Input input,
        Output output
    ) {
        this(activity, slotId, input);
        setResultOutput(output);
    }

    /**
     * Set the input for this ActivityMotor.
     *
     * @param input
     *     The LongSupplier that provides the cycle number.
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


    @Override
    public long getSlotId() {
        return this.slotId;
    }


    @Override
    public Void call() {

        try {
            inputTimer = activity.getInstrumentation().getOrCreateInputTimer();
            strideServiceTimer = activity.getInstrumentation().getOrCreateStridesServiceTimer();
            stridesResponseTimer = activity.getInstrumentation().getStridesResponseTimerOrNull();

            strideRateLimiter = activity.getStrideLimiter();
            cycleRateLimiter = activity.getCycleLimiter();


            if (input instanceof Startable) {
                ((Startable) input).start();
            }

            if (strideRateLimiter != null) {
                // block for strides rate limiter
                strideRateLimiter.block();
            }

            long strideDelay = 0L;
            long cycleDelay = 0L;

            SyncAction sync = this;
            cycleServiceTimer = activity.getInstrumentation().getOrCreateCyclesServiceTimer();
            strideServiceTimer = activity.getInstrumentation().getOrCreateStridesServiceTimer();

            if (activity.getActivityDef().getParams().containsKey("async")) {
                throw new RuntimeException("The async parameter was given for this activity, but it does not seem to know how to do async.");
            }


            CycleSegment cycleSegment = null;
            CycleResultSegmentBuffer segBuffer = new CycleResultSegmentBuffer(stride);


            try (Timer.Context inputTime = inputTimer.time()) {
                cycleSegment = input.getInputSegment(stride);
            }

            if (cycleSegment == null) {
                throw new RuntimeException("invalid state with cycle segment = null");
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
                            logger.trace(() -> "input exhausted (input " + input + ") via negative read, stopping motor thread " + slotId);
                            continue;
                        }
                    }

                    int result = -1;

                    if (cycleRateLimiter != null) {
                        // Block for cycle rate limiter
                        cycleDelay = cycleRateLimiter.block();
                    }

                    long cycleStart = System.nanoTime();
                    try {
                        logger.trace(() -> "cycle " + cyclenum);
                        result = sync.runCycle(cyclenum);
                    } catch (Exception e) {
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
                    logger.error(() -> "Error while feeding result segment " + outputBuffer + " to output '" + output + "', error:" + t);
                    throw t;
                }
            }


        } catch (Throwable t) {
            logger.error(() -> "Error in core motor loop:" + t, t);
            throw t;
        }
        return null;
    }

    @Override
    public String toString() {
        return this.activity.getAlias() + ": slot:" + this.slotId;
    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {

        for (Object component : (new Object[]{input, opTracker, this, output})) {
            if (component instanceof ActivityDefObserver) {
                ((ActivityDefObserver) component).onActivityDefUpdate(activityDef);
            }
        }

        this.stride = activityDef.getParams().getOptionalInteger("stride").orElse(1);
        strideRateLimiter = activity.getStrideLimiter();
        cycleRateLimiter = activity.getCycleLimiter();

    }

    public void setResultOutput(Output resultOutput) {
        this.output = resultOutput;
    }

    public int runCycle(long cycle) {

        OpDispenser<? extends Op> dispenser = null;
        Op op = null;

        try (Timer.Context ct = bindTimer.time()) {
            dispenser = opsequence.apply(cycle);
            op = dispenser.getOp(cycle);
        } catch (Exception e) {
            throw new RuntimeException("while binding request in cycle " + cycle + " for op template named '" + (dispenser != null ? dispenser.getOpName() : "NULL") +
                "': " + e.getMessage(), e);
        }

        int code = 0;
        Object result = null;
        while (op != null) {

            int tries = 0;
            while (tries++ < maxTries) {
                Throwable error = null;
                long startedAt = System.nanoTime();

                dispenser.onStart(cycle);

                try (Timer.Context ct = executeTimer.time()) {
                    if (op instanceof RunnableOp runnableOp) {
                        runnableOp.run();
                    } else if (op instanceof CycleOp<?> cycleOp) {
                        result = cycleOp.apply(cycle);
                    } else if (op instanceof ChainingOp chainingOp) {
                        result = chainingOp.apply(result);
                    } else {
                        throw new RuntimeException("The op implementation did not implement any active logic. Implement " +
                            "one of [RunnableOp, CycleOp, or ChainingOp]");
                    }
                    // TODO: break out validation timer from execute
                    try (Timer.Context ignored = verifierTimer.time()) {
                        CycleFunction<Boolean> verifier = dispenser.getVerifier();
                        try {
                            verifier.setVariable("result", result);
                            verifier.setVariable("cycle", cycle);
                            Boolean isGood = verifier.apply(cycle);
                            if (!isGood) {
                                throw new ResultVerificationError("result verification failed", maxTries - tries, verifier.getExpressionDetails());
                            }
                        } catch (Exception e) {
                            throw new ResultVerificationError(e, maxTries - tries, verifier.getExpressionDetails());
                        }
                    }
                } catch (Exception e) {
                    error = e;
                } finally {
                    long nanos = System.nanoTime() - startedAt;
                    resultTimer.update(nanos, TimeUnit.NANOSECONDS);
                    if (error == null) {
                        resultSuccessTimer.update(nanos, TimeUnit.NANOSECONDS);
                        dispenser.onSuccess(cycle, nanos);
                        break;
                    } else {
                        ErrorDetail detail = errorHandler.handleError(error, cycle, nanos);
                        dispenser.onError(cycle, nanos, error);
                        code = detail.resultCode;
                        if (!detail.isRetryable()) {
                            break;
                        }
                    }
                }
            }
            triesHistogram.update(tries);

            if (op instanceof OpGenerator) {
                logger.trace(() -> "GEN OP for cycle(" + cycle + ")");
                op = ((OpGenerator) op).getNextOp();
            } else {
                op = null;
            }
        }

        return code;
    }

}
