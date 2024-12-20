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

import com.codahale.metrics.Counting;
import io.nosqlbench.engine.api.activityimpl.uniform.ActivityWiring;
import io.nosqlbench.engine.api.activityimpl.uniform.StandardActivity;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.nb.api.engine.activityimpl.ParameterMap;
import io.nosqlbench.engine.api.activityapi.core.progress.ProgressCapable;
import io.nosqlbench.engine.api.activityapi.core.progress.StateCapable;
import io.nosqlbench.engine.api.activityapi.errorhandling.ErrorMetrics;
import io.nosqlbench.engine.api.activityapi.simrate.RateLimiter;
import io.nosqlbench.engine.api.activityimpl.motor.RunStateTally;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricTimer;

import java.io.InputStream;
import java.io.PrintWriter;

/**
 * Provides the components needed to build and run an activity a runtime.
 * The easiest way to build a useful Activity is to extend {@link StandardActivity}.
 */
public interface Activity extends Comparable<Activity>, ActivityDefObserver, ProgressCapable, StateCapable, NBComponent {

    /**
     * Register an object which should be closed after this activity is shutdown.
     *
     * @param closeable An Autocloseable object
     */
    void registerAutoCloseable(AutoCloseable closeable);

    ActivityDef getActivityDef();

    default String getAlias() {
        return this.getActivityDef().getAlias();
    }

    default ParameterMap getParams() {
        return this.getActivityDef().getParams();
    }

    default void initActivity() {
    }

    /**
     * Close all autocloseables that have been registered with this Activity.
     */
    void closeAutoCloseables();

    @Override
    RunState getRunState();

    void setRunState(RunState runState);

    long getStartedAtMillis();

    default void shutdownActivity() {
    }

    default String getCycleSummary() {
        return this.getActivityDef().getCycleSummary();
    }

    /**
     * Get the current cycle rate limiter for this activity.
     * The cycle rate limiter is used to throttle the rate at which
     * cycles are dispatched across all threads in the activity
     * @return the cycle {@link RateLimiter}
     */
    RateLimiter getCycleLimiter();


    /**
     * Get the current stride rate limiter for this activity.
     * The stride rate limiter is used to throttle the rate at which
     * new strides are dispatched across all threads in an activity.
     * @return The stride {@link RateLimiter}
     */
    RateLimiter getStrideLimiter();

    PrintWriter getConsoleOut();

    InputStream getConsoleIn();

    void setConsoleOut(PrintWriter writer);

    ErrorMetrics getExceptionMetrics();

//    /**
//     * When a driver needs to identify an error uniquely for the purposes of
//     * routing it to the correct error handler, or naming it in logs, or naming
//     * metrics, override this method in your activity.
//     * @return A function that can reliably and safely map an instance of Throwable to a stable name.
//     */
//    default Function<Throwable,String> getErrorNameMapper() {
//        return t -> t.getClass().getSimpleName();
//    }
//

    int getMaxTries();
    default int getHdrDigits() {
        return this.getParams().getOptionalInteger("hdr_digits").orElse(4);
    }
    RunStateTally getRunStateTally();
    ActivityWiring getWiring();

}
