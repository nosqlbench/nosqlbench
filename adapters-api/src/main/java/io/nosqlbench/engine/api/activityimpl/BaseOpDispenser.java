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

package io.nosqlbench.engine.api.activityimpl;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.api.config.NBLabels;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import io.nosqlbench.api.errors.MVELCompilationError;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.engine.api.metrics.ThreadLocalNamedTimers;
import io.nosqlbench.engine.api.templating.ParsedOp;
import org.mvel2.MVEL;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 *
 * See {@link OpDispenser} for details on how to use this type.
 * <p>
 * Some details are tracked per op template, which aligns to the life-cycle of the op dispenser.
 * Thus, each op dispenser is where the stats for all related operations are kept.
 *
 * @param <T> The type of operation
 */
public abstract class BaseOpDispenser<T extends Op, S> implements OpDispenser<T>, NBLabeledElement {

    private final String opName;
    private Serializable expectedResultExpression;
    protected final DriverAdapter<T, S> adapter;
    private final NBLabels labels;
    private boolean instrument;
    private Histogram resultSizeHistogram;
    private Timer successTimer;
    private Timer errorTimer;
    private final String[] timerStarts;
    private final String[] timerStops;

    protected BaseOpDispenser(final DriverAdapter<T, S> adapter, final ParsedOp op) {
        opName = op.getName();
        this.adapter = adapter;
        labels = op.getLabels();

        this.timerStarts = op.takeOptionalStaticValue("start-timers", String.class)
                .map(s -> s.split(", *"))
                .orElse(null);

        this.timerStops = op.takeOptionalStaticValue("stop-timers", String.class)
                .map(s -> s.split(", *"))
                .orElse(null);

        if (null != timerStarts)
            for (final String timerStart : this.timerStarts) ThreadLocalNamedTimers.addTimer(op, timerStart);
        this.configureInstrumentation(op);
        this.configureResultExpectations(op);
    }

    public Serializable getExpectedResultExpression() {
        return expectedResultExpression;
    }

    private void configureResultExpectations(ParsedOp op) {
        op.takeOptionalStaticValue("expected-result", String.class)
            .map(this::compileExpectedResultExpression)
            .ifPresent(result -> this.expectedResultExpression = result);
    }

    private Serializable compileExpectedResultExpression(String expectedResultExpression) {
        try {
            return MVEL.compileExpression(expectedResultExpression);
        } catch (Exception e) {
            throw new MVELCompilationError(
                String.format("Failed to compile expected-result expression: \"%s\"", expectedResultExpression), e
            );
        }
    }

    String getOpName() {
        return this.opName;
    }

    public DriverAdapter<T, S> getAdapter() {
        return this.adapter;
    }

    private void configureInstrumentation(final ParsedOp pop) {
        instrument = pop.takeStaticConfigOr("instrument", false);
        if (this.instrument) {
            final int hdrDigits = pop.getStaticConfigOr("hdr_digits", 4).intValue();
            successTimer = ActivityMetrics.timer(pop, "success",hdrDigits);
            errorTimer = ActivityMetrics.timer(pop, "error", hdrDigits);
            resultSizeHistogram = ActivityMetrics.histogram(pop, "resultset-size", hdrDigits);
        }
    }

    @Override
    public void onStart(final long cycleValue) {
        if (null != timerStarts) ThreadLocalNamedTimers.TL_INSTANCE.get().start(this.timerStarts);
    }

    @Override
    public void onSuccess(final long cycleValue, final long nanoTime, final long resultSize) {
        if (this.instrument) {
            this.successTimer.update(nanoTime, TimeUnit.NANOSECONDS);
            if (-1 < resultSize) this.resultSizeHistogram.update(resultSize);
        }
        if (null != timerStops) ThreadLocalNamedTimers.TL_INSTANCE.get().stop(this.timerStops);
    }

    @Override
    public void onError(final long cycleValue, final long resultNanos, final Throwable t) {

        if (this.instrument) this.errorTimer.update(resultNanos, TimeUnit.NANOSECONDS);
        if (null != timerStops) ThreadLocalNamedTimers.TL_INSTANCE.get().stop(this.timerStops);
    }

    @Override
    public NBLabels getLabels() {
        return this.labels;
    }

}
