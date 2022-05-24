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
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
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
public abstract class BaseOpDispenser<T> implements OpDispenser<T> {

    private final String name;
    private boolean instrument;
    private Histogram resultSizeHistogram;
    private Timer successTimer;
    private Timer errorTimer;
    private String[] timerStarts = new String[0];
    private String[] timerStops = new String[0];


    // TODO: Consider changing this to "ready op template" or similar
    public BaseOpDispenser(OpTemplate optpl) {
        this.name = optpl.getName();
    }

    public BaseOpDispenser(ParsedOp op) {
        this.name = op.getName();
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

//    public BaseOpDispenser(CommandTemplate cmdtpl) {
//        this.name = cmdtpl.getName();
//    }

    /**
     * {@inheritDoc}
     *
     * @param value The cycle number which serves as the seed for any
     *              generated op fields to be bound into an operation.
     * @return
     */
    @Override
    public abstract T apply(long value);

    private void configureInstrumentation(ParsedOp pop) {
        this.instrument = pop.takeStaticConfigOr("instrument", false);
        if (instrument) {
            this.successTimer = ActivityMetrics.timer(pop.getStaticConfigOr("alias", "UNKNOWN") + "-" + pop.getName() + "--success");
            this.errorTimer = ActivityMetrics.timer(pop.getStaticConfigOr("alias", "UNKNOWN") + "-" + pop.getName() + "--error");
            this.resultSizeHistogram = ActivityMetrics.histogram(pop.getStaticConfigOr("alias", "UNKNOWN") + "-" + pop.getName() + "--resultset-size");
        }

        timerStarts = pop.takeOptionalStaticValue("start-timers", String.class)
            .map(s -> s.split(", *"))
            .orElse(null);

        if (timerStarts!=null) {
            for (String timerStart : timerStarts) {
                ThreadLocalNamedTimers.addTimer(pop,timerStart);
            }
        }

        timerStops = pop.takeOptionalStaticValue("stop-timers", String.class)
            .map(s -> s.split(", *"))
            .orElse(null);
    }

    @Override
    public void onStart(long cycleValue) {
        if (timerStarts!=null) {
            ThreadLocalNamedTimers.TL_INSTANCE.get().start(timerStops);
        }
    }

    @Override
    public void onSuccess(long cycleValue, long nanoTime, long resultsize) {
        if (instrument) {
            successTimer.update(nanoTime, TimeUnit.NANOSECONDS);
            resultSizeHistogram.update(resultsize);
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
