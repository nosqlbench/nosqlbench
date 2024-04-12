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

package io.nosqlbench.adapter.s4j.ops;

import com.codahale.metrics.Histogram;
import io.nosqlbench.adapter.s4j.S4JSpace;
import io.nosqlbench.adapter.s4j.util.S4JAdapterMetrics;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;

import javax.jms.Destination;
import javax.jms.JMSContext;

public abstract class S4JOp implements CycleOp<Object> {
    protected S4JAdapterMetrics s4jAdapterMetrics;
    protected final S4JSpace s4jSpace;
    protected final JMSContext jmsContext;
    protected final Destination destination;
    protected final boolean asyncApi;
    protected   final boolean commitTransact;
    protected final long s4jOpStartTimeMills;
    protected final long maxS4jOpDurationInSec;
    protected final Histogram messageSizeHistogram;


    public S4JOp(
        S4JAdapterMetrics s4jAdapterMetrics,
        S4JSpace s4jSpace,
        JMSContext jmsContext,
        Destination destination,
        boolean asyncApi,
        boolean commitTransact)
    {
        this.s4jAdapterMetrics = s4jAdapterMetrics;
        this.s4jSpace = s4jSpace;
        this.jmsContext = jmsContext;
        this.destination = destination;
        this.asyncApi = asyncApi;
        this.commitTransact = commitTransact;
        this.s4jOpStartTimeMills = s4jSpace.getS4JActivityStartTimeMills();
        this.maxS4jOpDurationInSec = s4jSpace.getMaxS4JOpTimeInSec();
        this.messageSizeHistogram = s4jAdapterMetrics.getMessagesizeHistogram();
    }
}
