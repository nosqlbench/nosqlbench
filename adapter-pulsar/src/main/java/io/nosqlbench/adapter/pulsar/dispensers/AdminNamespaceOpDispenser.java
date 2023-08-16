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
import io.nosqlbench.adapter.pulsar.ops.AdminNamespaceOp;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class AdminNamespaceOpDispenser extends PulsarAdminOpDispenser {

    private final static Logger logger = LogManager.getLogger("AdminNamespaceOpDispenser");

    public AdminNamespaceOpDispenser(DriverAdapter adapter,
                                     ParsedOp op,
                                     LongFunction<String> tgtNameFunc,
                                     PulsarSpace pulsarSpace) {
        super(adapter, op, tgtNameFunc, pulsarSpace);
    }

    @Override
    public AdminNamespaceOp apply(long cycle) {
        return new AdminNamespaceOp(
            pulsarAdapterMetrics,
            pulsarAdmin,
            asyncApiFunc.apply(cycle),
            adminDelOpFunc.apply(cycle),
            tgtNameFunc.apply(cycle));
    }
}
