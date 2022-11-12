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

package io.nosqlbench.adapter.pulsar.dispensers;

import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.templating.ParsedOp;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;

import java.util.function.LongFunction;

public abstract class PulsarClientOpDispenser extends PulsarBaseOpDispenser {

    protected final PulsarClient pulsarClient;
    protected final Schema<?> pulsarSchema;

    public PulsarClientOpDispenser(DriverAdapter adapter,
                                   ParsedOp op,
                                   LongFunction<String> tgtNameFunc,
                                   PulsarClient pulsarClient,
                                   Schema<?> pulsarSchema) {
        super(adapter, op, tgtNameFunc);
        this.pulsarClient = pulsarClient;
        this.pulsarSchema = pulsarSchema;
    }
}
