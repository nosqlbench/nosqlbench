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

package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.api.errors.BasicError;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.common.util.FutureUtil;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PulsarBatchProducerEndOp extends SyncPulsarOp {
    @Override
    public void run() {
        List<CompletableFuture<MessageId>> container = PulsarBatchProducerStartOp.threadLocalBatchMsgContainer.get();
        Producer<?> producer = PulsarBatchProducerStartOp.threadLocalProducer.get();

        if ((container != null) && (!container.isEmpty())) {
            try {
                // producer.flushAsync().get();
                FutureUtil.waitForAll(container).get();
            } catch (Exception e) {
                throw new RuntimeException("Batch Producer:: failed to send (some of) the batched messages!");
            }

            container.clear();
            PulsarBatchProducerStartOp.threadLocalBatchMsgContainer.set(null);
        }
        else {
            throw new BasicError("You tried to end an empty batch message container. This means you" +
                " did initiate the batch container properly, or there is an error in your" +
                " pulsar op sequencing and ratios.");
        }
    }
}
