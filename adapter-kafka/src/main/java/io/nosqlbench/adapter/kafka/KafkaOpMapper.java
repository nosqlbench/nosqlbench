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

package io.nosqlbench.adapter.kafka;

import io.nosqlbench.adapter.kafka.dispensers.MessageConsumerOpDispenser;
import io.nosqlbench.adapter.kafka.dispensers.MessageProducerOpDispenser;
import io.nosqlbench.adapter.kafka.ops.KafkaOp;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KafkaOpMapper implements OpMapper<KafkaOp> {

    private final static Logger logger = LogManager.getLogger(KafkaOpMapper.class);

    private final NBConfiguration cfg;
    private final DriverSpaceCache<? extends KafkaSpace> spaceCache;
    private final DriverAdapter adapter;

    public KafkaOpMapper(DriverAdapter adapter, NBConfiguration cfg, DriverSpaceCache<? extends KafkaSpace> spaceCache) {
        this.cfg = cfg;
        this.spaceCache = spaceCache;
        this.adapter = adapter;
    }

    @Override
    public OpDispenser<? extends KafkaOp> apply(ParsedOp op) {
        String spaceName = op.getStaticConfigOr("space", "default");
        KafkaSpace kafkaSpace = spaceCache.get(spaceName);

        /*
         * If the user provides a body element, then they want to provide the JSON or
         * a data structure that can be converted into JSON, bypassing any further
         * specialized type-checking or op-type specific features
         */
        if (op.isDefined("body")) {
            throw new RuntimeException("This mode is reserved for later. Do not use the 'body' op field.");
        }
        else {
            TypeAndTarget<KafkaOpType, String> opType = op.getTypeAndTarget(KafkaOpType.class, String.class);

            return switch (opType.enumId) {
                case MessageProduce ->
                    new MessageProducerOpDispenser(adapter, op, opType.targetFunction, kafkaSpace);
                case MessageConsume ->
                    new MessageConsumerOpDispenser(adapter, op, opType.targetFunction, kafkaSpace);
            };
        }
    }

}
