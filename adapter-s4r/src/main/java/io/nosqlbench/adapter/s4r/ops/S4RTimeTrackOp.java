/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.adapter.s4r.ops;

import com.rabbitmq.client.Channel;
import io.nosqlbench.adapter.s4r.S4RSpace;
import io.nosqlbench.adapter.s4r.util.S4RAdapterMetrics;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.CycleOp;

import java.io.IOException;

public abstract class S4RTimeTrackOp implements CycleOp<Object> {
    private final S4RAdapterMetrics s4rAdapterMetrics;
    protected final S4RSpace s4RSpace;
    protected final Channel channel;
    protected final String exchangeName;

    // Maximum time length to execute Kafka operations (e.g. message send or consume)
    // - when NB execution passes this threshold, it is simply NoOp
    // - 0 means no maximum time constraint. S4RTimeTrackOp is always executed until NB execution cycle finishes
    protected final long maxOpTimeInSec;

    protected final long activityStartTime;

    protected Object cycleObj;

    public S4RTimeTrackOp(S4RAdapterMetrics s4rAdapterMetrics,
                          S4RSpace s4RSpace,
                          Channel channel,
                          String exchangeName)
    {
        this.s4rAdapterMetrics = s4rAdapterMetrics;
        this.s4RSpace = s4RSpace;
        this.channel = channel;
        this.exchangeName = exchangeName;
        this.activityStartTime = s4RSpace.getActivityStartTimeMills();
        this.maxOpTimeInSec = s4RSpace.getMaxOpTimeInSec();
    }

    @Override
    public Object apply(long cycle) {
        long timeElapsedMills = System.currentTimeMillis() - activityStartTime;

        // If maximum operation duration is specified, only process messages
        // before the maximum duration threshold is reached. Otherwise, this is
        // just no-op.
        if ( (maxOpTimeInSec == 0) || (timeElapsedMills <= (maxOpTimeInSec*1000)) ) {
            cycleMsgProcess(cycle, cycleObj);
        }

        return  null;
    }

    abstract void cycleMsgProcess(long cycle, Object cycleObj);
}
