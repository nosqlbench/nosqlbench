package io.nosqlbench.engine.api.activityimpl.uniform;

/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import com.codahale.metrics.Timer;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricCounter;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricHistogram;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricTimer;

import java.util.concurrent.Future;
import java.util.function.LongFunction;

public class ActivityMetrics {

    public static final String WAIT_TIME = "_waittime";
    public static final String RESPONSE_TIME = "_responsetime";
    public static final String SERVICE_TIME = "_servicetime";

    public final Activity<?, ?> activity;
    public final int hdrdigits;
    public NBMetricCounter pendingOpsCounter;
    public NBMetricTimer bindTimer;
    public NBMetricTimer executeTimer;
    public NBMetricTimer cycleServiceTimer;
    public NBMetricTimer resultTimer;
    public NBMetricTimer resultSuccessTimer;
    public NBMetricTimer inputTimer;
    public NBMetricTimer stridesServiceTimer;
    public NBMetricTimer stridesResponseTimer;
    public NBMetricTimer cycleResponseTimer;
    public NBMetricHistogram triesHistogram;

    public <S, R extends LongFunction> ActivityMetrics(Activity<?, ?> activity) {
        this.activity = activity;
        this.hdrdigits = activity.getComponentProp("hdr_digits").map(Integer::parseInt).orElse(3);
        initMetrics();
    }

    private void initMetrics() {

        this.pendingOpsCounter = activity.create().counter(
            "pending_ops", MetricCategory.Core,
            "Indicate the number of operations which have been started, but which have not been completed." +
                " This starts ");


        /// The bind timer keeps track of how long it takes for NoSQLBench to create an instance
        /// of an executable operation, given the cycle. This is usually done by using an
        /// {@link OpSequence} in conjunction with
        /// an {@link OpDispenser}. This is named for "binding
        /// a cycle to an operation".
        this.bindTimer = activity.create().timer(
            "bind", hdrdigits, MetricCategory.Core,
            "Time the step within a cycle which binds generated data to an op template to synthesize an executable operation.");

        /// The execute timer keeps track of how long it takes to submit an operation to be executed
        /// to an underlying native driver. For asynchronous APIs, such as those which return a
        /// {@link Future}, this is simply the amount of time it takes to acquire the future.
        ///     /// When possible, APIs should be used via their async methods, even if you are implementing
        /// a {@link SyncAction}. This allows the execute timer to measure the hand-off to the underlying API,
        /// and the result timer to measure the blocking calls to aquire the result.
        this.executeTimer = activity.create().timer(
            "execute", hdrdigits, MetricCategory.Core,
            "Time how long it takes to submit a request and receive a result, including reading the result in the client.");

        /// The cycles service timer measures how long it takes to complete a cycle of work.
        this.cycleServiceTimer = activity.create().timer(
            "cycles" + SERVICE_TIME, hdrdigits, MetricCategory.Core,
            "service timer for a cycle, including all of bind, execute, result and result_success;" +
                " service timers measure the time between submitting a request and receiving the response");


        /// The result timer keeps track of how long it takes a native driver to service a request once submitted.
        /// This timer, in contrast to the result-success timer ({@link #getOrCreateResultSuccessTimer()}),
        /// is used to track all operations. That is, no matter
        /// whether the operation succeeds or not, it should be tracked with this timer. The scope of this timer should
        /// cover each attempt at an operation through a native driver. Retries are not to be combined in this measurement.
        this.resultTimer = activity.create().timer(
            "result", hdrdigits, MetricCategory.Core,
            "Time how long it takes to submit a request, receive a result, including binding, reading results, " +
                "and optionally verifying them, including all operations whether successful or not, for each attempted request.");

        /// The result-success timer keeps track of operations which had no exception. The measurements for this timer should
        /// be exactly the same values as used for the result timer ({@link #getOrCreateResultTimer()}, except that
        /// attempts to complete an operation which yield an exception should be excluded from the results. These two metrics
        /// together provide a very high level sanity check against the error-specific metrics which can be reported by
        /// the error handler logic.
        this.resultSuccessTimer = activity.create().timer(
            "result_success", hdrdigits, MetricCategory.Core,
            "The execution time of successful operations, which includes submitting the operation, waiting for a response, and reading the result");

        /// The input timer measures how long it takes to get the cycle value to be used for
        /// an operation.
        this.inputTimer = activity.create().timer(
            "read_input", activity.getComponentProp("hdr_digits").map(Integer::parseInt).orElse(3),
            MetricCategory.Internals,
            "measures overhead of acquiring a cycle range for an activity thread");

        /// The strides service timer measures how long it takes to complete a stride of work.
        this.stridesServiceTimer = activity.create().timer(
            "strides", activity.getComponentProp("hdr_digits").map(Integer::parseInt).orElse(3),
            MetricCategory.Core,
            "service timer for a stride, which is the same as the op sequence length by default");

        if (null != activity.getStrideLimiter()) {
            ///  The strides response timer measures the total response time from the scheduled
            ///  time a stride should start to when it completed. Stride scheduling is only defined
            ///  when it is implied by a stride rate limiter, so this method should return null if
            ///  there is no strides rate limiter.
            this.stridesResponseTimer = activity.create().timer(
                "strides" + RESPONSE_TIME, hdrdigits, MetricCategory.Core,
                "response timer for a stride, which is the same as the op sequence length by default;" +
                    " response timers include scheduling delays which occur when an activity falls behind its target rate");
        } else {
            stridesResponseTimer = null;
        }


        /**
         * The cycles response timer measures the total response time from the scheduled
         * time an operation should start to when it is completed. Cycle scheduling is only defined
         * when it is implied by a cycle rate limiter, so this method should return null if
         * there is no cycles rate limiter.
         * @return a new or existing {@link Timer} if appropriate, else null
         */
        if (null != activity.getCycleLimiter()) {
            this.cycleResponseTimer = activity.create().timer(
                "cycles" + RESPONSE_TIME, hdrdigits, MetricCategory.Core,
                "response timer for a cycle, including all of bind, execute, result and result_success;" +
                    " response timers include scheduling delays which occur when an activity falls behind its target rate");
        } else {
            cycleResponseTimer = null;
        }


        /// The tries histogram tracks how many tries it takes to complete an operation successfully, or not. This histogram
        /// does not encode whether operations were successful or not. Ideally, if every attempt to complete an operation succeeds
        /// on its first try, the data in this histogram should all be 1. In practice, systems which are running near their
        /// capacity will see a few retried operations, and systems that are substantially over-driven will see many retried
        /// operations. As the retries value increases the further down the percentile scale you go, you can detect system loading
        /// patterns which are in excess of the real-time capability of the target system.
        /// This metric should be measured around every retry loop for a native operation.
        this.triesHistogram = activity.create().histogram(
            "tries", hdrdigits, MetricCategory.Core,
            "A histogram of all tries for an activity. Perfect results mean all quantiles return 1." +
                " Slight saturation is indicated by p99 or p95 returning higher values." +
                " Lower quantiles returning more than 1, or higher values at high quantiles indicate incremental overload.");
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ActivityMetrics{");
        sb.append(this.activity.description());
        return sb.toString();
    }
}
