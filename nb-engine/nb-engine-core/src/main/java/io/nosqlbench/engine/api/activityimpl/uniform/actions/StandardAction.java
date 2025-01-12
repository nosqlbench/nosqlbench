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

import com.codahale.metrics.Timer;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.*;
import io.nosqlbench.adapters.api.evalctx.CycleFunction;
import io.nosqlbench.engine.api.activityimpl.uniform.Activity;
import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricHistogram;
import io.nosqlbench.nb.api.errors.ResultVerificationError;
import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorDetail;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.NBErrorHandler;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.nb.api.labels.NBLabels;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 This is the generified version of an Action. All driver adapters us this, as opposed
 to previous NB versions where it was implemented for each driver.
 <p>
 This allows the API to be consolidated so that the internal machinery of NB
 works in a very consistent and uniform way for all users and drivers.
 @param <A>
 The type of activity
 @param <R>
 The type of operation */
public class StandardAction<A extends Activity<R, ?>, R extends java.util.function.LongFunction> extends NBBaseComponent implements SyncAction {
    private final static Logger logger = LogManager.getLogger("ACTION");
    private final NBErrorHandler errorHandler;
    private final OpSequence<OpDispenser<? extends CycleOp<?>>> opsequence;
    private final int maxTries;
    private final Timer verifierTimer;
    private final A activity;
    public NBMetricHistogram triesHistogram;

    public StandardAction(A activity) {
        super(activity, NBLabels.forKV("action", StandardAction.class.getSimpleName()));
        this.activity = activity;
        this.opsequence = activity.getOpSequence();
        this.maxTries = activity.getMaxTries();

        int hdrdigits = activity.getComponentProp("hdr_digits")
            .map(Integer::parseInt).orElse(3);


        errorHandler = activity.getErrorHandler();

        this.verifierTimer = activity.create().timer(
            "verifier",
            hdrdigits,
            MetricCategory.Verification,
            "Time the execution of verifier code, if any"
        );
    }

    @Override
    public int runCycle(long cycle) {

        OpDispenser<? extends CycleOp<?>> dispenser = null;
        CycleOp op = null;

        try (Timer.Context ct = activity.metrics.bindTimer.time()) {
            dispenser = opsequence.apply(cycle);
            op = dispenser.getOp(cycle);
        } catch (Exception e) {
            throw new RuntimeException(
                "while binding request in cycle " + cycle + " for op template named '" + (dispenser != null ? dispenser.getOpName() : "NULL") + "': " + e.getMessage(),
                e
            );
        }

        int code = 0;
        Object result = null;
        while (op != null) {

            int tries = 0;
            while (tries++ < maxTries) {
                Throwable error = null;
                long startedAt = System.nanoTime();

                dispenser.onStart(cycle);

                try (Timer.Context ct = activity.metrics.executeTimer.time()) {
                    result = op.apply(cycle);
                    // TODO: break out validation timer from execute
                    try (Timer.Context ignored = verifierTimer.time()) {
                        CycleFunction<Boolean> verifier = dispenser.getVerifier();
                        try {
                            verifier.setVariable("result", result);
                            verifier.setVariable("cycle", cycle);
                            Boolean isGood = verifier.apply(cycle);
                            if (!isGood) {
                                throw new ResultVerificationError(
                                    "result verification failed", maxTries - tries,
                                    verifier.getExpressionDetails()
                                );
                            }
                        } catch (Exception e) {
                            throw new ResultVerificationError(
                                e, maxTries - tries, verifier.getExpressionDetails());
                        }
                    }
                } catch (Exception e) {
                    error = e;
                } finally {
                    long nanos = System.nanoTime() - startedAt;
                    activity.metrics.resultTimer.update(nanos, TimeUnit.NANOSECONDS);
                    if (error == null) {
                        activity.metrics.resultSuccessTimer.update(nanos, TimeUnit.NANOSECONDS);
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
            activity.metrics.triesHistogram.update(tries);

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
