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

package io.nosqlbench.engine.api.activityapi.core;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.nb.api.engine.activityimpl.ParameterMap;
import io.nosqlbench.nb.api.engine.metrics.instruments.*;

public class ComponentActivityInstrumentation implements ActivityInstrumentation {

    private static final String WAIT_TIME = "_waittime";
    private static final String SERVICE_TIME = "_servicetime";
    private static final String RESPONSE_TIME = "_responsetime";

    private final Activity activity;
    private final ActivityDef def;
    private final ParameterMap params;
    private final int hdrdigits;
    private NBMetricTimer readInputTimer;
    private NBMetricTimer stridesServiceTimer;
    private NBMetricTimer stridesResponseTimer;
    private NBMetricTimer cyclesServiceTimer;
    private NBMetricTimer cyclesResponseTimer;
    private NBMetricCounter pendingOpsCounter;
    private NBMetricTimer bindTimer;
    private NBMetricTimer executeTimer;
    private NBMetricTimer resultTimer;
    private NBMetricTimer resultSuccessTimer;
    private NBMetricHistogram triesHistogram;
    private NBMetricTimer verifierTimer;

    private NBMetricGauge errorRate1m;
    private NBMetricGauge errorRate5m;
    private NBMetricGauge errorRate15m;
    private NBMetricGauge errorRateTotal;
    private NBMetricGauge errorsTotal;

    public ComponentActivityInstrumentation(final Activity activity) {
        this.activity = activity;
        def = activity.getActivityDef();
        params = this.def.getParams();
        hdrdigits = activity.getHdrDigits();
        initMetrics();
    }

    private void initMetrics() {
        readInputTimer = activity.create().timer(
            "read_input",
            this.hdrdigits,
            MetricCategory.Internals,
            "measures overhead of acquiring a cycle range for an activity thread"
        );
        stridesServiceTimer = activity.create().timer(
            "strides",
            this.hdrdigits,
            MetricCategory.Core,
            "service timer for a stride, which is the same as the op sequence length by default"
        );
        if (null != activity.getStrideLimiter()) {
            this.stridesResponseTimer = activity.create().timer(
                "strides" + ComponentActivityInstrumentation.RESPONSE_TIME,
                hdrdigits,
                MetricCategory.Core,
                "response timer for a stride, which is the same as the op sequence length by default;" +
                    " response timers include scheduling delays which occur when an activity falls behind its target rate"
            );
        }
        this.cyclesServiceTimer = activity.create().timer(
            "cycles" + ComponentActivityInstrumentation.SERVICE_TIME,
            hdrdigits,
            MetricCategory.Core,
            "service timer for a cycle, including all of bind, execute, result and result_success;" +
                " service timers measure the time between submitting a request and receiving the response"
        );
        if (null != activity.getCycleLimiter()) {
            this.cyclesResponseTimer = activity.create().timer(
                "cycles" + ComponentActivityInstrumentation.RESPONSE_TIME,
                hdrdigits,
                MetricCategory.Core,
                "response timer for a cycle, including all of bind, execute, result and result_success;" +
                    " response timers include scheduling delays which occur when an activity falls behind its target rate"
            );
        }
        this.pendingOpsCounter = activity.create().counter(
            "pending_ops",
            MetricCategory.Core,
            "Indicate the number of operations which have been started, but which have not been completed." +
                " This starts "
        );

        this.bindTimer = activity.create().timer(
            "bind",
            hdrdigits,
            MetricCategory.Core,
            "Time the step within a cycle which binds generated data to an op template to synthesize an executable operation."
        );

        this.executeTimer = activity.create().timer(
            "execute",
            hdrdigits,
            MetricCategory.Core,
            "Time how long it takes to submit a request and receive a result, including reading the result in the client."
        );
        this.resultTimer = activity.create().timer(
            "result",
            hdrdigits,
            MetricCategory.Core,
            "Time how long it takes to submit a request, receive a result, including binding, reading results, " +
                "and optionally verifying them, including all operations whether successful or not, for each attempted request."
        );
        this.resultSuccessTimer = activity.create().timer(
            "result_success",
            hdrdigits,
            MetricCategory.Core,
            "The execution time of successful operations, which includes submitting the operation, waiting for a response, and reading the result"
        );
        this.triesHistogram = activity.create().histogram(
            "tries",
            hdrdigits,
            MetricCategory.Core,
            "A histogram of all tries for an activity. Perfect results mean all quantiles return 1." +
                " Slight saturation is indicated by p99 or p95 returning higher values." +
                " Lower quantiles returning more than 1, or higher values at high quantiles indicate incremental overload."
        );
        this.verifierTimer = activity.create().timer(
            "verifier",
            hdrdigits,
            MetricCategory.Verification,
            "Time the execution of verifier code, if any"
        );
        this.errorRate1m = activity.create().gauge("error_rate_1m",
            () -> {
                double result_1m_rate = this.resultTimer.getOneMinuteRate();
                double result_success_1m_rate = this.resultSuccessTimer.getOneMinuteRate();
                if (result_1m_rate==0.0d || Double.isNaN(result_1m_rate)) {
                    return Double.NaN;
                }
                return (result_1m_rate-result_success_1m_rate)/result_1m_rate;
            },
            MetricCategory.Core,
            "The relative one minute error rate estimated from the one minute successful and non-successful op rates"
        );
        this.errorRate5m = activity.create().gauge("error_rate_5m",
            () -> {
                double result_5m_rate = this.resultTimer.getFiveMinuteRate();
                double result_success_5m_rate = this.resultSuccessTimer.getFiveMinuteRate();
                if (result_5m_rate == 0.0d || Double.isNaN(result_5m_rate)) {
                    return Double.NaN;
                }
                return (result_5m_rate - result_success_5m_rate) / result_5m_rate;
            },
            MetricCategory.Core,
            "The relative five minute error rate estimated from the five minute successful and non-successful op rates"
        );
        this.errorRate15m = activity.create().gauge("error_rate_15m",
            () -> {
                double result_15m_rate = this.resultTimer.getFifteenMinuteRate();
                double result_success_15m_rate = this.resultSuccessTimer.getFifteenMinuteRate();
                if (result_15m_rate == 0.0d || Double.isNaN(result_15m_rate)) {
                    return Double.NaN;
                }
                return (result_15m_rate - result_success_15m_rate) / result_15m_rate;
            },
            MetricCategory.Core,
            "The relative fifteen minute error rate estimated from the fifteen minute successful and non-successful op rates"
        );
        this.errorRateTotal = activity.create().gauge("error_rate_total",
            () -> {
                double result_total = this.resultTimer.getCount();
                double result_success_total = this.resultSuccessTimer.getCount();
                if (result_total == 0.0d) {
                    return Double.NaN;
                }
                return (result_total - result_success_total) / result_total;
            },
            MetricCategory.Core,
            "The cumulative error ratio calculated from the cumulative successful and non-successful op totals"
        );
        this.errorsTotal = activity.create().gauge("errors_total",
            () -> {
                double result_total = this.resultTimer.getCount();
                double result_success_total = this.resultSuccessTimer.getCount();
                return (result_total - result_success_total);
            },
            MetricCategory.Core,
            "The total number of errors calculated from the cumulative successful and non-successful op totals"
        );
    }

    @Override
    public NBMetricGauge getOrCreateErrorsTotal() {
        return this.errorsTotal;
    }
    @Override
    public NBMetricGauge getOrCreateErrorRate1m() {
        return this.errorRate1m;
    }
    @Override
    public NBMetricGauge getOrCreateErrorRate5m() {
        return this.errorRate5m;
    }
    @Override
    public NBMetricGauge getOrCreateErrorRate15m() {
        return this.errorRate15m;
    }
    @Override
    public NBMetricGauge getOrCreateErrorRateTotal() {
        return this.errorRateTotal;
    }


    @Override
    public Timer getOrCreateInputTimer() {
        return readInputTimer;
    }


    @Override
    public Timer getOrCreateStridesServiceTimer() {
        return stridesServiceTimer;
    }

    @Override
    public Timer getStridesResponseTimerOrNull() {
        return stridesResponseTimer;
    }


    @Override
    public Timer getOrCreateCyclesServiceTimer() {
        return cyclesServiceTimer;
    }

    @Override
    public Timer getCyclesResponseTimerOrNull() {
        return cyclesResponseTimer;
    }

    @Override
    public Counter getOrCreatePendingOpCounter() {
        return pendingOpsCounter;
    }

    @Override
    public Timer getOrCreateBindTimer() {
        return bindTimer;
    }

    @Override
    public Timer getOrCreateExecuteTimer() {
        return executeTimer;
    }

    @Override
    public Timer getOrCreateResultTimer() {
        return resultTimer;
    }

    @Override
    public Timer getOrCreateResultSuccessTimer() {
        return resultSuccessTimer;
    }

    @Override
    public Histogram getOrCreateTriesHistogram() {
        return triesHistogram;
    }

    @Override
    public Timer getOrCreateVerifierTimer() {
        return verifierTimer;
    }
}
