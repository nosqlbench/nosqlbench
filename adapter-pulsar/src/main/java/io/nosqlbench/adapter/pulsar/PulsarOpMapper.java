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

package io.nosqlbench.adapter.pulsar;

import io.nosqlbench.adapter.pulsar.dispensers.*;
import io.nosqlbench.adapter.pulsar.ops.PulsarOp;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;

public class PulsarOpMapper implements OpMapper<PulsarOp> {

    private final static Logger logger = LogManager.getLogger(PulsarOpMapper.class);

    private final NBConfiguration cfg;
    private final DriverSpaceCache<? extends PulsarSpace> cache;
    private final DriverAdapter adapter;

    public PulsarOpMapper(DriverAdapter adapter, NBConfiguration cfg, DriverSpaceCache<? extends PulsarSpace> cache) {
        this.cfg = cfg;
        this.cache = cache;
        this.adapter = adapter;
    }

    @Override
    public OpDispenser<? extends PulsarOp> apply(ParsedOp op) {
        String space = op.getStaticConfigOr("space", "default");

        PulsarClient pulsarClient = cache.get(space).getPulsarClient();
        PulsarAdmin pulsarAdmin = cache.get(space).getPulsarAdmin();
        Schema<?> pulsarSchema = cache.get(space).getPulsarSchema();



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
                    new AdminTenantOpDispenser(adapter, op, opType.targetFunction, pulsarAdmin);
                case AdminNamespace ->
                    new AdminNamespaceOpDispenser(adapter, op, opType.targetFunction, pulsarAdmin);
                case AdminTopic ->
                    new AdminTopicOpDispenser(adapter, op, opType.targetFunction, pulsarAdmin);
                case MessageProduce ->
                    new MessageProducerOpDispenser(adapter, op, opType.targetFunction, pulsarClient, pulsarSchema);
                case MessageConsume ->
                    new MessageConsumerOpDispenser(adapter, op, opType.targetFunction, pulsarClient, pulsarSchema);
                case MessageRead ->
                    new MessageReaderOpDispenser(adapter, op, opType.targetFunction, pulsarClient, pulsarSchema);
            };
        }
    }

}
