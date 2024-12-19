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
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.uniform.StandardActivity;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.nb.api.engine.activityimpl.ParameterMap;
import io.nosqlbench.nb.api.engine.metrics.instruments.*;

import java.util.concurrent.Future;

public class ComponentActivityInstrumentation  {

    private static final String WAIT_TIME = "_waittime";
    private static final String SERVICE_TIME = "_servicetime";
    private static final String RESPONSE_TIME = "_responsetime";

    private final StandardActivity activity;
    private final ActivityDef def;
    private final ParameterMap params;
    private final int hdrdigits;
    private NBMetricTimer readInputTimer;
    private NBMetricTimer stridesServiceTimer;
    private NBMetricTimer stridesResponseTimer;
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

    public ComponentActivityInstrumentation(final StandardActivity activity) {
        this.activity = activity;
        def = activity.getActivityDef();
        params = this.def.getParams();
        hdrdigits = activity.getHdrDigits();
        initMetrics();
    }

    private void initMetrics() {


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

    /// The pending ops counter keeps track of how many ops are submitted or in-flight, but
    /// which haven't been completed yet.
    public Counter getOrCreatePendingOpCounter() {
        return pendingOpsCounter;
    }

    public Timer getOrCreateBindTimer() {
        return bindTimer;
    }



}
