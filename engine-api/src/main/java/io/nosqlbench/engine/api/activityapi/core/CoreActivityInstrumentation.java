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
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.api.engine.activityimpl.ParameterMap;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;

public class CoreActivityInstrumentation implements ActivityInstrumentation {

    private static final String STRICTMETRICNAMES = "strictmetricnames";

    private static final String WAIT_TIME = "_waittime";
    private static final String SERVICE_TIME = "_servicetime";
    private static final String RESPONSE_TIME = "_responsetime";

    private final Activity activity;
    private final ActivityDef def;
    private final ParameterMap params;
    private final String svcTimeSuffix;
    private final boolean strictNaming;

    public CoreActivityInstrumentation(final Activity activity) {
        this.activity = activity;
        def = activity.getActivityDef();
        params = this.def.getParams();
        strictNaming = this.params.getOptionalBoolean(CoreActivityInstrumentation.STRICTMETRICNAMES).orElse(true);
        this.svcTimeSuffix = this.strictNaming ? CoreActivityInstrumentation.SERVICE_TIME : "";
    }


    @Override
    public synchronized Timer getOrCreateInputTimer() {
        final String metricName = "read_input";
        return ActivityMetrics.timer(this.activity, metricName, this.activity.getHdrDigits());
    }


    @Override
    public synchronized Timer getOrCreateStridesServiceTimer() {
        return ActivityMetrics.timer(this.activity, "strides" + CoreActivityInstrumentation.SERVICE_TIME, this.activity.getHdrDigits());
    }

    @Override
    public synchronized Timer getStridesResponseTimerOrNull() {
        if (null == activity.getStrideLimiter()) return null;
        return ActivityMetrics.timer(this.activity, "strides" + CoreActivityInstrumentation.RESPONSE_TIME, this.activity.getHdrDigits());
    }


    @Override
    public synchronized Timer getOrCreateCyclesServiceTimer() {
        return ActivityMetrics.timer(this.activity, "cycles" + this.svcTimeSuffix, this.activity.getHdrDigits());
    }

    @Override
    public synchronized Timer getCyclesResponseTimerOrNull() {
        if (null == activity.getCycleLimiter()) return null;
        final String metricName = "cycles" + CoreActivityInstrumentation.RESPONSE_TIME;
        return ActivityMetrics.timer(this.activity, metricName, this.activity.getHdrDigits());
    }

    @Override
    public synchronized Counter getOrCreatePendingOpCounter() {
        final String metricName = "pending_ops";
        return ActivityMetrics.counter(this.activity, metricName);
    }

    @Override
    public synchronized Counter getOrCreateOpTrackerBlockedCounter() {
        final String metricName = "optracker_blocked";
        return ActivityMetrics.counter(this.activity, metricName);
    }

    @Override
    public synchronized Timer getOrCreateBindTimer() {
        return ActivityMetrics.timer(this.activity, "bind", this.activity.getHdrDigits());
    }

    @Override
    public synchronized Timer getOrCreateExecuteTimer() {
        return ActivityMetrics.timer(this.activity,"execute", this.activity.getHdrDigits());
    }

    @Override
    public synchronized Timer getOrCreateResultTimer() {
        return ActivityMetrics.timer(this.activity,"result", this.activity.getHdrDigits());
    }

    @Override
    public synchronized Timer getOrCreateResultSuccessTimer() {
        return ActivityMetrics.timer(this.activity,"result_success", this.activity.getHdrDigits());
    }

    @Override
    public synchronized Histogram getOrCreateTriesHistogram() {
        return ActivityMetrics.histogram(this.activity,"tries", this.activity.getHdrDigits());
    }

    @Override
    public Timer getOrCreateVerifierTimer() {
        return ActivityMetrics.timer(this.activity,"verifier", this.activity.getHdrDigits());

    }
}
