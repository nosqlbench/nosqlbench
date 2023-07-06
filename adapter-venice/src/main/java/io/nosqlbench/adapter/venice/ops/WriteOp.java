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

package io.nosqlbench.adapter.venice.ops;


import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import com.linkedin.venice.producer.DurableWrite;
import com.linkedin.venice.producer.online.OnlineVeniceProducer;
import io.nosqlbench.adapter.venice.VeniceSpace;
import io.nosqlbench.adapter.venice.util.VeniceAdapterMetrics;
import org.apache.avro.generic.GenericRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;


public class WriteOp extends VeniceOp {

    private final static Logger logger = LogManager.getLogger("ReadSingleKeyOp");

    private final OnlineVeniceProducer<Object, Object> producer;
    private final Object key;
    private final Object value;
    private final Timer executeTimer;

    public WriteOp(VeniceAdapterMetrics veniceAdapterMetrics,
                   VeniceSpace veniceSpace,
                   Object key,
                   Object value) {
        super(veniceAdapterMetrics, veniceSpace);
        this.producer = veniceSpace.getProducer();
        this.key = key;
        this.value = value;
        this.executeTimer = veniceAdapterMetrics.getExecuteTimer();
    }

    @Override
    public Object apply(long value) {
        Object callValue;
        try (Timer.Context time = executeTimer.time();) {
            CompletableFuture<DurableWrite> handle = producer.asyncPut(this.key, this.value);
            callValue = handle.join();
            if (logger.isDebugEnabled()) {
                logger.debug("Write key={} value={} res {}", key, callValue, callValue);
            }
        }
        return null;
    }
}
