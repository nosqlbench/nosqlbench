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
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricCounter;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricHistogram;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricTimer;

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

    public ComponentActivityInstrumentation(final Activity activity) {
        this.activity = activity;
        def = activity.getActivityDef();
        params = this.def.getParams();
        hdrdigits = activity.getHdrDigits();
        initMetrics();
    }

    private void initMetrics() {
        readInputTimer=activity.create().timer(
            "read_input",
            this.hdrdigits,
            MetricCategory.Internals,
            "measures overhead of acquiring a cycle range for an activity thread"
        );
        stridesServiceTimer=activity.create().timer(
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
            "cycles"+ComponentActivityInstrumentation.SERVICE_TIME,
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
        this.pendingOpsCounter=activity.create().counter(
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
    }


    @Override
    public  Timer getOrCreateInputTimer() {
        return readInputTimer;
    }


    @Override
    public  Timer getOrCreateStridesServiceTimer() {
        return stridesServiceTimer;
    }

    @Override
    public  Timer getStridesResponseTimerOrNull() {
        return stridesResponseTimer;
    }


    @Override
    public  Timer getOrCreateCyclesServiceTimer() {
        return cyclesServiceTimer;
    }

    @Override
    public  Timer getCyclesResponseTimerOrNull() {
        return cyclesResponseTimer;
    }

    @Override
    public  Counter getOrCreatePendingOpCounter() {
        return pendingOpsCounter;
    }

    @Override
    public  Timer getOrCreateBindTimer() {
        return bindTimer;
    }

    @Override
    public  Timer getOrCreateExecuteTimer() {
        return executeTimer;
    }

    @Override
    public  Timer getOrCreateResultTimer() {
        return resultTimer;
    }

    @Override
    public  Timer getOrCreateResultSuccessTimer() {
        return resultSuccessTimer;
    }

    @Override
    public  Histogram getOrCreateTriesHistogram() {
        return triesHistogram;
    }

    @Override
    public Timer getOrCreateVerifierTimer() {
        return verifierTimer;
    }
}
