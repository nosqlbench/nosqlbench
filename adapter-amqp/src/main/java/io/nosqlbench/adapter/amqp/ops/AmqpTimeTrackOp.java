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

package io.nosqlbench.adapter.amqp.ops;

import com.rabbitmq.client.Channel;
import io.nosqlbench.adapter.amqp.AmqpSpace;
import io.nosqlbench.adapter.amqp.util.AmqpAdapterMetrics;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.CycleOp;

public abstract class AmqpTimeTrackOp implements CycleOp<Object> {
    private final AmqpAdapterMetrics amqpAdapterMetrics;
    protected final AmqpSpace amqpSpace;
    protected final Channel channel;
    protected final String exchangeName;

    // Maximum time length to execute Kafka operations (e.g. message send or consume)
    // - when NB execution passes this threshold, it is simply NoOp
    // - 0 means no maximum time constraint. AmqpTimeTrackOp is always executed until NB execution cycle finishes
    protected final long maxOpTimeInSec;

    protected final long activityStartTime;

    protected Object cycleObj;

    public AmqpTimeTrackOp(AmqpAdapterMetrics amqpAdapterMetrics,
                           AmqpSpace amqpSpace,
                           Channel channel,
                           String exchangeName)
    {
        this.amqpAdapterMetrics = amqpAdapterMetrics;
        this.amqpSpace = amqpSpace;
        this.channel = channel;
        this.exchangeName = exchangeName;
        this.activityStartTime = amqpSpace.getActivityStartTimeMills();
        this.maxOpTimeInSec = amqpSpace.getMaxOpTimeInSec();
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
