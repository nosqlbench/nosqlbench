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

package io.nosqlbench.adapter.pulsar.dispensers;

import io.nosqlbench.adapter.pulsar.PulsarSpace;
import io.nosqlbench.adapter.pulsar.ops.MessageProducerOp;
import io.nosqlbench.adapter.pulsar.util.PulsarAdapterUtil;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.Producer;

import java.util.function.LongFunction;

public class MessageProducerOpDispenser extends PulsarClientOpDispenser {

    private final static Logger logger = LogManager.getLogger("MessageProducerOpDispenser");

    public static final String MSG_KEY_OP_PARAM = "msg_key";
    public static final String MSG_PROP_OP_PARAM = "msg_prop";
    public static final String MSG_VALUE_OP_PARAM = "msg_value";

    private final LongFunction<String> cycleProducerNameFunc;
    private final LongFunction<Producer<?>> producerFunc;
    private final LongFunction<String> msgKeyFunc;
    private final LongFunction<String> msgPropFunc;
    private final LongFunction<String> msgValueFunc;

    public MessageProducerOpDispenser(DriverAdapter adapter,
                                      ParsedOp op,
                                      LongFunction<String> tgtNameFunc,
                                      PulsarSpace pulsarSpace) {
        super(adapter, op, tgtNameFunc, pulsarSpace);

        this.cycleProducerNameFunc =
            lookupOptionalStrOpValueFunc(PulsarAdapterUtil.PRODUCER_CONF_STD_KEY.producerName.label);
        this.producerFunc = (l) -> getProducer(tgtNameFunc.apply(l), cycleProducerNameFunc.apply(l));
        this.msgKeyFunc = lookupOptionalStrOpValueFunc(MSG_KEY_OP_PARAM);
        this.msgPropFunc = lookupOptionalStrOpValueFunc(MSG_PROP_OP_PARAM);
        this.msgValueFunc = lookupMandtoryStrOpValueFunc(MSG_VALUE_OP_PARAM);
    }

    @Override
    public MessageProducerOp apply(long cycle) {
        return new MessageProducerOp(
            pulsarAdapterMetrics,
            pulsarClient,
            pulsarSchema,
            asyncApiFunc.apply(cycle),
            useTransactFunc.apply(cycle),
            seqTrackingFunc.apply(cycle),
            transactSupplierFunc.apply(cycle),
            msgSeqErrSimuTypeSetFunc.apply(cycle),
            producerFunc.apply(cycle),
            msgKeyFunc.apply(cycle),
            msgPropFunc.apply(cycle),
            msgValueFunc.apply(cycle)
        );
    }
}
