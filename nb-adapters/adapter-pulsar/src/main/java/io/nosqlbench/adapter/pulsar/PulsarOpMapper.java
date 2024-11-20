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

package io.nosqlbench.adapter.pulsar;

import io.nosqlbench.adapter.pulsar.dispensers.*;
import io.nosqlbench.adapter.pulsar.ops.PulsarOp;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class PulsarOpMapper implements OpMapper<PulsarOp,PulsarSpace> {

    private final static Logger logger = LogManager.getLogger(PulsarOpMapper.class);

    private final NBConfiguration cfg;
    private final PulsarDriverAdapter adapter;

    public PulsarOpMapper(PulsarDriverAdapter adapter, NBConfiguration cfg) {
        this.cfg = cfg;
       this.adapter = adapter;
    }

    @Override
    public OpDispenser<PulsarOp> apply(NBComponent adapterC, ParsedOp op, LongFunction<PulsarSpace> spaceInitF) {
        int spaceName = op.getStaticConfigOr("space", 0);
//        PulsarSpace pulsarSpace = spaceCache.get(spaceName);
        PulsarSpace pulsarSpace = adapter.getSpaceFunc(op).apply(spaceName);

        /*
         * If the user provides a body element, then they want to provide the JSON or
         * a data structure that can be converted into JSON, bypassing any further
         * specialized type-checking or op-type specific features
         */
        if (op.isDefined("body")) {
            throw new RuntimeException("This mode is reserved for later. Do not use the 'body' op field.");
        }
        else {
            TypeAndTarget<PulsarOpType, String> opType = op.getTypeAndTarget(PulsarOpType.class, String.class);

            return switch (opType.enumId) {
                case AdminTenant ->
                    new AdminTenantOpDispenser(adapter, op, opType.targetFunction, pulsarSpace);
                case AdminNamespace ->
                    new AdminNamespaceOpDispenser(adapter, op, opType.targetFunction, pulsarSpace);
                case AdminTopic ->
                    new AdminTopicOpDispenser(adapter, op, opType.targetFunction, pulsarSpace);
                case MessageProduce ->
                    new MessageProducerOpDispenser(adapter, op, opType.targetFunction, pulsarSpace);
                case MessageConsume ->
                    new MessageConsumerOpDispenser(adapter, op, opType.targetFunction, pulsarSpace);
                //////////////////////////
                // NOTE: not sure how useful to have Pulsar message reader API in the NB performance testing
                //       currently, the reader API in NB Pulsar driver is no-op (see TDOD in MessageReaderOp)
                //////////////////////////
                case MessageRead ->
                    new MessageReaderOpDispenser(adapter, op, opType.targetFunction, pulsarSpace);
            };
        }
    }

}
