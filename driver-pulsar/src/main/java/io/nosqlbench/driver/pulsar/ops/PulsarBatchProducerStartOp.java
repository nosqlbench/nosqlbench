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

import io.nosqlbench.nb.api.errors.BasicError;
import org.apache.commons.compress.utils.Lists;
import org.apache.pulsar.client.api.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PulsarBatchProducerStartOp extends SyncPulsarOp {

    // TODO: ensure sane container lifecycle management
    public final transient static ThreadLocal<List<CompletableFuture<MessageId>>> threadLocalBatchMsgContainer = new ThreadLocal<>();
    public final transient static ThreadLocal<Producer<?>> threadLocalProducer = new ThreadLocal<>();

    public PulsarBatchProducerStartOp(Producer<?> batchProducer) {
        threadLocalProducer.set(batchProducer);
    }

    @Override
    public void run() {
        List<CompletableFuture<MessageId>> container = threadLocalBatchMsgContainer.get();

        if (container == null) {
            container = Lists.newArrayList();
            threadLocalBatchMsgContainer.set(container);
        } else {
            throw new BasicError("You tried to create a batch message container where one was already" +
                " defined. This means you did not flush and unset the last container, or there is an error in your" +
                " pulsar op sequencing and ratios.");
        }
    }
}
