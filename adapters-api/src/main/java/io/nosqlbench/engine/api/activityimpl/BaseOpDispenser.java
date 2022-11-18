/*
 * Copyright (c) 2022 nosqlbench
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
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import io.nosqlbench.engine.api.metrics.ThreadLocalNamedTimers;
import io.nosqlbench.engine.api.templating.ParsedOp;

import java.util.concurrent.TimeUnit;

/**
 * {@inheritDoc}
 * See {@link OpDispenser} for details on how to use this type.
 *
 * Some details are tracked per op template, which aligns to the life-cycle of the op dispenser.
 * Thus, each op dispenser is where the stats for all related operations are kept.
 *
 * @param <T> The type of operation
 */
public abstract class BaseOpDispenser<T extends Op, S> implements OpDispenser<T> {

    private final String name;
    protected final DriverAdapter<T, S> adapter;
    protected boolean instrument;
    private Histogram resultSizeHistogram;
    private Timer successTimer;
    private Timer errorTimer;
    private String[] timerStarts = new String[0];
    private String[] timerStops = new String[0];

    public BaseOpDispenser(DriverAdapter<T,S> adapter,ParsedOp op) {
        this.name = op.getName();
        this.adapter = adapter;
        timerStarts = op.takeOptionalStaticValue("start-timers", String.class)
            .map(s -> s.split(", *"))
            .orElse(null);

        timerStops = op.takeOptionalStaticValue("stop-timers", String.class)
            .map(s -> s.split(", *"))
            .orElse(null);

        if (timerStarts!=null) {
            for (String timerStart : timerStarts) {
                ThreadLocalNamedTimers.addTimer(op,timerStart);
            }
        }
        configureInstrumentation(op);
    }

    public DriverAdapter<T,S> getAdapter() {
        return adapter;
    }

//    public BaseOpDispenser(CommandTemplate cmdtpl) {
//        this.name = cmdtpl.getName();
//    }

    /**
     * {@inheritDoc}
     *
     * @param cycle The cycle number which serves as the seed for any
     *              generated op fields to be bound into an operation.
     * @return
     */
    @Override
    public abstract T apply(long cycle);

    protected String getDefaultMetricsPrefix(ParsedOp pop) {
        return pop.getStaticConfigOr("alias", "UNKNOWN") + "-" + pop.getName() + "--";
    }

    private void configureInstrumentation(ParsedOp pop) {
        this.instrument = pop.takeStaticConfigOr("instrument", false);
        if (instrument) {
            this.successTimer = ActivityMetrics.timer(getDefaultMetricsPrefix(pop) + "success");
            this.errorTimer = ActivityMetrics.timer(getDefaultMetricsPrefix(pop) + "error");
            this.resultSizeHistogram = ActivityMetrics.histogram(getDefaultMetricsPrefix(pop) + "resultset-size");
        }
    }

    @Override
    public void onStart(long cycleValue) {
        if (timerStarts!=null) {
            ThreadLocalNamedTimers.TL_INSTANCE.get().start(timerStarts);
        }
    }

    @Override
    public void onSuccess(long cycleValue, long nanoTime, long resultsize) {
        if (instrument) {
            successTimer.update(nanoTime, TimeUnit.NANOSECONDS);
            if (resultsize>-1) {
                resultSizeHistogram.update(resultsize);
            }
        }
        if (timerStops!=null) {
            ThreadLocalNamedTimers.TL_INSTANCE.get().stop(timerStops);
        }

//        ThreadLocalNamedTimers.TL_INSTANCE.get().stop(stopTimers);
    }

    @Override
    public void onError(long cycleValue, long resultNanos, Throwable t) {
        if (instrument) {
            errorTimer.update(resultNanos, TimeUnit.NANOSECONDS);
        }
        if (timerStops!=null) {
            ThreadLocalNamedTimers.TL_INSTANCE.get().stop(timerStops);
        }
    }

}
