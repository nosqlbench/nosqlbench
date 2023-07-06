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


import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import com.linkedin.venice.client.store.AvroGenericStoreClient;
import io.nosqlbench.adapter.venice.VeniceSpace;
import io.nosqlbench.adapter.venice.util.VeniceAdapterMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;


public class ReadSingleKeyOp extends VeniceOp {

    private final static Logger logger = LogManager.getLogger("ReadSingleKeyOp");

    private final AvroGenericStoreClient<Object, Object> client;
    private final Object key;
    private final Timer executeTimer;
    private Counter foundCounter;
    private Counter notFoundCounter;

    public ReadSingleKeyOp(VeniceAdapterMetrics veniceAdapterMetrics,
                           VeniceSpace veniceSpace,
                           Object key) {
        super(veniceAdapterMetrics, veniceSpace);
        this.client = veniceSpace.getClient();
        this.key = key;
        this.executeTimer = veniceAdapterMetrics.getExecuteTimer();
        this.foundCounter = veniceAdapterMetrics.getFoundCounter();
        this.notFoundCounter = veniceAdapterMetrics.getNotFoundCounter();
    }

    @Override
    public Object apply(long value) {
        Object callValue;
        try (Timer.Context time = executeTimer.time();) {
            CompletableFuture<Object> handle = client.get(key);
            callValue = handle.join();
            if (logger.isDebugEnabled()) {
                logger.debug("ReadSingleKeyOp key={} value={} latency {}", key, callValue);
            }
        }
        if (callValue != null) {
            foundCounter.inc();
        } else {
            notFoundCounter.inc();
        }
        return null;
    }
}
