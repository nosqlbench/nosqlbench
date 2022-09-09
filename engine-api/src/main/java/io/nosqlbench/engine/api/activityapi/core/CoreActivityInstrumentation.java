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

package io.nosqlbench.engine.api.activityapi.core;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.api.engine.activityimpl.ParameterMap;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;

public class CoreActivityInstrumentation implements ActivityInstrumentation {

    private static final String STRICTMETRICNAMES = "strictmetricnames";

    private static final String WAIT_TIME = ".waittime";
    private static final String SERVICE_TIME = ".servicetime";
    private static final String RESPONSE_TIME = ".responsetime";

    private final Activity activity;
    private final ActivityDef def;
    private final ParameterMap params;
    private final String svcTimeSuffix;
    private final boolean strictNaming;

    public CoreActivityInstrumentation(Activity activity) {
        this.activity = activity;
        this.def = activity.getActivityDef();
        this.params = def.getParams();
        this.strictNaming = params.getOptionalBoolean(STRICTMETRICNAMES).orElse(true);
        svcTimeSuffix = strictNaming ? SERVICE_TIME : "";
    }


    @Override
    public synchronized Timer getOrCreateInputTimer() {
        String metricName = "read_input";
        return ActivityMetrics.timer(def, metricName, activity.getHdrDigits());
    }


    @Override
    public synchronized Timer getOrCreateStridesServiceTimer() {
        return ActivityMetrics.timer(def, "strides" + SERVICE_TIME,  activity.getHdrDigits());
    }

    @Override
    public synchronized Timer getStridesResponseTimerOrNull() {
        if (activity.getStrideLimiter()==null) {
            return null;
        }
        return ActivityMetrics.timer(def, "strides" + RESPONSE_TIME,  activity.getHdrDigits());
    }


    @Override
    public synchronized Timer getOrCreateCyclesServiceTimer() {
        return ActivityMetrics.timer(def, "cycles" + svcTimeSuffix,  activity.getHdrDigits());
    }

    @Override
    public synchronized Timer getCyclesResponseTimerOrNull() {
        if (activity.getCycleLimiter()==null) {
            return null;
        }
        String metricName = "cycles" + RESPONSE_TIME;
        return ActivityMetrics.timer(def, metricName, activity.getHdrDigits());
    }

    @Override
    public synchronized Counter getOrCreatePendingOpCounter() {
        String metricName = "pending_ops";
        return ActivityMetrics.counter(def, metricName);
    }

    @Override
    public synchronized Counter getOrCreateOpTrackerBlockedCounter() {
        String metricName = "optracker_blocked";
        return ActivityMetrics.counter(def, metricName);
    }

    @Override
    public synchronized Timer getOrCreateBindTimer() {
        return ActivityMetrics.timer(def, "bind",  activity.getHdrDigits());
    }

    @Override
    public synchronized Timer getOrCreateExecuteTimer() {
        return ActivityMetrics.timer(def,"execute",  activity.getHdrDigits());
    }

    @Override
    public synchronized Timer getOrCreateResultTimer() {
        return ActivityMetrics.timer(def,"result",  activity.getHdrDigits());
    }

    @Override
    public synchronized Timer getOrCreateResultSuccessTimer() {
        return ActivityMetrics.timer(def,"result-success",  activity.getHdrDigits());
    }

    @Override
    public synchronized Histogram getOrCreateTriesHistogram() {
        return ActivityMetrics.histogram(def,"tries",  activity.getHdrDigits());
    }
}
