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
import io.nosqlbench.adapter.pulsar.ops.AdminTopicOp;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class AdminTopicOpDispenser extends PulsarAdminOpDispenser {

    private final static Logger logger = LogManager.getLogger("AdminTopicOpDispenser");

    private final LongFunction<Boolean> enablePartFunc;
    private final LongFunction<Integer> partNumFunc;

    public AdminTopicOpDispenser(DriverAdapter adapter,
                                 ParsedOp op,
                                 LongFunction<String> tgtNameFunc,
                                 PulsarSpace pulsarSpace) {
        super(adapter, op, tgtNameFunc, pulsarSpace);

        // Non-partitioned topic is default
        enablePartFunc = lookupStaticBoolConfigValueFunc("enable_partition", false);
        partNumFunc = lookupStaticIntOpValueFunc("partition_num", 1);
    }

    @Override
    public AdminTopicOp apply(long cycle) {

        return new AdminTopicOp(
            pulsarAdapterMetrics,
            pulsarAdmin,
            asyncApiFunc.apply(cycle),
            adminDelOpFunc.apply(cycle),
            tgtNameFunc.apply(cycle),
            enablePartFunc.apply(cycle),
            partNumFunc.apply(cycle)
        );
    }
}
