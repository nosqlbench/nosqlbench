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

import com.codahale.metrics.Timer;
import io.nosqlbench.engine.api.activityapi.core.progress.ProgressCapable;
import io.nosqlbench.engine.api.activityapi.core.progress.StateCapable;
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.IntPredicateDispenser;
import io.nosqlbench.engine.api.activityapi.errorhandling.ErrorMetrics;
import io.nosqlbench.engine.api.activityapi.output.OutputDispenser;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.activityimpl.motor.RunStateTally;
import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.api.engine.activityimpl.ParameterMap;
import io.nosqlbench.engine.api.activityapi.input.InputDispenser;
import io.nosqlbench.engine.api.activityapi.ratelimits.RateLimiter;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.function.Supplier;

/**
 * Provides the components needed to build and run an activity a runtime.
 * The easiest way to build a useful Activity is to extend {@link SimpleActivity}.
 */
public interface Activity extends Comparable<Activity>, ActivityDefObserver, ProgressCapable, StateCapable, NBLabeledElement {

    /**
     * Provide the activity with the controls needed to stop itself.
     *
     * @param activityController The dedicated control interface for this activity
     */
    void setActivityController(ActivityController activityController);

    ActivityController getActivityController();

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

    MotorDispenser getMotorDispenserDelegate();

    void setMotorDispenserDelegate(MotorDispenser motorDispenser);

    InputDispenser getInputDispenserDelegate();

    void setInputDispenserDelegate(InputDispenser inputDispenser);

    ActionDispenser getActionDispenserDelegate();

    void setActionDispenserDelegate(ActionDispenser actionDispenser);

    IntPredicateDispenser getResultFilterDispenserDelegate();

    void setResultFilterDispenserDelegate(IntPredicateDispenser resultFilterDispenser);

    OutputDispenser getMarkerDispenserDelegate();

    void setOutputDispenserDelegate(OutputDispenser outputDispenser);

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
     * Set the cycle rate limiter for this activity. This method should only
     * be used in a non-concurrent context. Otherwise, the supplier version
     * {@link #getCycleRateLimiter(Supplier)} should be used.
     * @param rateLimiter The cycle {@link RateLimiter} for this activity
     */
    void setCycleLimiter(RateLimiter rateLimiter);

    /**
     * Get or create the cycle rate limiter in a safe way. Implementations
     * should ensure that this method is synchronized or that each requester
     * gets the same cycle rate limiter for the activity.
     * @param supplier A {@link RateLimiter} {@link Supplier}
     * @return An extant or newly created cycle {@link RateLimiter}
     */
    RateLimiter getCycleRateLimiter(Supplier<? extends RateLimiter> supplier);

    /**
     * Get the current stride rate limiter for this activity.
     * The stride rate limiter is used to throttle the rate at which
     * new strides are dispatched across all threads in an activity.
     * @return The stride {@link RateLimiter}
     */
    RateLimiter getStrideLimiter();

    /**
     * Set the stride rate limiter for this activity. This method should only
     * be used in a non-concurrent context. Otherwise, the supplier version
     * {@link #getStrideRateLimiter(Supplier)}} should be used.
     * @param rateLimiter The stride {@link RateLimiter} for this activity.
     */
    void setStrideLimiter(RateLimiter rateLimiter);

    /**
     * Get or create the stride {@link RateLimiter} in a concurrent-safe
     * way. Implementations should ensure that this method is synchronized or
     * that each requester gets the same stride rate limiter for the activity.
     * @param supplier A {@link RateLimiter} {@link Supplier}
     * @return An extant or newly created stride {@link RateLimiter}
     */
    RateLimiter getStrideRateLimiter(Supplier<? extends RateLimiter> supplier);

    Timer getResultTimer();

    /**
     * Get or create the instrumentation needed for this activity. This provides
     * a single place to find and manage, and document instrumentation that is
     * uniform across all activities.
     *
     * @return A new or existing instrumentation object for this activity.
     */
    ActivityInstrumentation getInstrumentation();

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
}
