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

import io.nosqlbench.driver.pulsar.PulsarActivity;
import io.nosqlbench.driver.pulsar.PulsarSpace;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.pulsar.client.api.Producer;

import java.util.function.LongFunction;

public class PulsarBatchProducerStartMapper extends PulsarOpMapper {

    private final LongFunction<Producer<?>> batchProducerFunc;

    public PulsarBatchProducerStartMapper(CommandTemplate cmdTpl,
                                          PulsarSpace clientSpace,
                                          PulsarActivity pulsarActivity,
                                          LongFunction<Boolean> asyncApiFunc,
                                          LongFunction<Producer<?>> batchProducerFunc) {
        super(cmdTpl, clientSpace, pulsarActivity, asyncApiFunc);
        this.batchProducerFunc = batchProducerFunc;
    }

    @Override
    public PulsarOp apply(long value) {
        Producer<?> batchProducer = batchProducerFunc.apply(value);
        return new PulsarBatchProducerStartOp(batchProducer);
    }
}
