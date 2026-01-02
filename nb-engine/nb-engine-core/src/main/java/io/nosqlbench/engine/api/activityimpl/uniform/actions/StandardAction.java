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

package io.nosqlbench.engine.api.activityimpl.uniform.actions;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.flow.FlowContextAwareOp;
import io.nosqlbench.adapters.api.activityimpl.flow.OpFlowContext;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.*;
import io.nosqlbench.adapters.api.evalctx.CycleFunction;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.core.StrideAction;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultSegmentBuffer;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleSegment;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorDetail;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.NBErrorHandler;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.nb.api.errors.ResultVerificationError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 This is the unified version of an Action. All driver adapters use this, as opposed
 to previous NB versions where it was implemented for each driver.
 <p>
 This allows the API to be consolidated so that the internal machinery of NB
 works in a very consistent and uniform way for all users and drivers.
 */
public class StandardAction implements StrideAction, ActivityDefObserver {
    private final static Logger logger = LogManager.getLogger("ACTION");
    private final Timer executeTimer;
    private final Histogram triesHistogram;
    private final Timer resultSuccessTimer;
    private final Timer resultTimer;
    private final Timer bindTimer;
    private final NBErrorHandler errorHandler;
    private final OpSequence<OpDispenser<? extends CycleOp<?>>> opsequence;
    private final int maxTries;
    private final Timer verifierTimer;
    private final Activity activity;
    /// Stride-scoped shell for flow contexts and per-cycle scratch.
    private final StrideContext strideContext;

    public StandardAction(Activity activity, int slot) {
        this.activity = activity;
        this.opsequence = activity.getOpSequence();
        this.maxTries = activity.getMaxTries();
        bindTimer = activity.getInstrumentation().getOrCreateBindTimer();
        executeTimer = activity.getInstrumentation().getOrCreateExecuteTimer();
        triesHistogram = activity.getInstrumentation().getOrCreateTriesHistogram();
        resultTimer = activity.getInstrumentation().getOrCreateResultTimer();
        resultSuccessTimer = activity.getInstrumentation().getOrCreateResultSuccessTimer();
        errorHandler = activity.getErrorHandler();
        verifierTimer = activity.getInstrumentation().getOrCreateVerifierTimer();
        long stride = activity.getParams().getOptionalLong("stride").orElse(1L);
        this.strideContext = new StrideContext(stride, 16, 4);
        // Set a space name resolver based on dispenser metadata if needed; fallback is per-cycle error context
    }

    @Override
    public int runCycle(long cycle) {

        OpDispenser<? extends CycleOp<?>> dispenser = null;
        CycleOp op = null;

        OpFlowContext flowContext;
        try (Timer.Context ct = bindTimer.time()) {
            flowContext = strideContext.beginCycle(cycle); // clear flow-scoped context for this cycle slot
            strideContext.setSpaceNameResolver(null);
            strideContext.setSpaceIndexResolver(null);
            dispenser = opsequence.apply(cycle);
            if (dispenser instanceof BaseOpDispenser<?, ?> baseDisp) {
                strideContext.setSpaceNameResolver(baseDisp.getSpaceNameFunction());
                strideContext.setSpaceIndexResolver(baseDisp.getSpaceIndexFunction());
            }
            var indexResolver = strideContext.getSpaceIndexResolver();
            if (indexResolver != null) {
                strideContext.setCurrentSpaceIndex(indexResolver.applyAsInt(cycle));
            }
            // Resolve and stash space name once per cycle for diagnostics/inject paths
            var nameResolver = strideContext.getSpaceNameResolver();
            if (nameResolver != null) {
                strideContext.setCurrentSpaceName(nameResolver.apply(cycle));
            } else {
                try {
                    strideContext.setCurrentSpaceName(dispenser.getErrorContextForCycle(cycle).getOrDefault("space_name", "0"));
                } catch (Exception ignored) {
                    strideContext.setCurrentSpaceName("0");
                }
            }
            op = dispenser.getOp(cycle);
            if (op instanceof FlowContextAwareOp aware) {
                aware.setFlowContext(flowContext, strideContext.getCurrentSpaceIndex());
            }
        } catch (Exception e) {
            RuntimeException bindingError =  new RuntimeException(
                "while binding request in cycle " + cycle + " for op template named '" + (
                    dispenser != null ? dispenser.getOpName() : "NULL") + "': " + e.getMessage(), e
            );
            bindingError = dispenser.modifyExceptionMessage(bindingError, cycle);
            throw bindingError;
        }

        int code = 0;
        Object result = null;
        while (op != null) {

            int tries = 0;
            while (tries++ < maxTries) {
                Throwable error = null;
                long startedAt = System.nanoTime();

                dispenser.onStart(cycle);

                try {
                    try (Timer.Context ct = executeTimer.time()) {
                        result = op.apply(cycle);
                    }

                    try (Timer.Context ignored = verifierTimer.time()) {
                        CycleFunction<Boolean> verifier = dispenser.getVerifier();
                        if (verifier != null) {
                            try {
                                verifier.setVariable("result", result);
                                verifier.setVariable("cycle", cycle);
                                Boolean isGood = verifier.apply(cycle);
                                if (!isGood) {
                                    throw new ResultVerificationError(
                                        "result verification failed",
                                        maxTries - tries,
                                        verifier.getExpressionDetails()
                                    );
                                }
                            } catch (Exception e) {
                                throw new ResultVerificationError(
                                    e,
                                    maxTries - tries,
                                    verifier.getExpressionDetails()
                                );
                            }
                        }
                    }

                } catch (Exception e) {
                    error = dispenser.modifyExceptionMessage(e,cycle);
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
                if (op instanceof FlowContextAwareOp aware) {
                    aware.setFlowContext(strideContext.getCurrentFlowContext(), strideContext.getCurrentSpaceIndex());
                }
            } else {
                op = null;
            }
        }

        return code;
    }

    @Override
    public int runStride(CycleSegment segment) {
        // reset stride-local buffers before starting
        resetStrideBuffers();
        int code = 0;
        while (!segment.isExhausted()) {
            long cyclenum = segment.nextCycle();
            if (cyclenum < 0) {
                break;
            }
            code = runCycle(cyclenum);
        }
        return code;
    }

    /// Reset reusable stride-local buffers; intended for callers that run strides directly.
    public void resetStrideBuffers() {
        strideContext.resetResultBuffer();
    }

    /// Access the stride-local result buffer for appending cycle results.
    public CycleResultSegmentBuffer resultBuffer() {
        return strideContext.resultBuffer();
    }

    /// Access the per-cycle flow context (current cycle slot).
    OpFlowContext currentFlowContext() {
        return strideContext.getCurrentFlowContext();
    }

    /// Access the per-cycle space name (diagnostic).
    String currentSpaceName() {
        return strideContext.getCurrentSpaceName();
    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {
        // Activity handles rate limiter and error handler refresh internally.
    }

}
