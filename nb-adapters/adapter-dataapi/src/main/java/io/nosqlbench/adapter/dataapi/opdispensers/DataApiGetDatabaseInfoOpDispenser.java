/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.adapter.dataapi.opdispensers;

import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.DataApiGetDatabaseInfoOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;
import java.util.function.LongFunction;

public class DataApiGetDatabaseInfoOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiGetDatabaseInfoOpDispenser.class);
    private final LongFunction<DataApiGetDatabaseInfoOp> opFunction;

    public DataApiGetDatabaseInfoOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiGetDatabaseInfoOp> createOpFunction(ParsedOp op) {
        return (l) -> new DataApiGetDatabaseInfoOp(
            spaceFunction.apply(l).getDatabase(),
            spaceFunction.apply(l).getAdmin(),
            UUID.fromString(targetFunction.apply(l))
        );
    }

    @Override
    public DataApiBaseOp getOp(long cycle) {
        return opFunction.apply(cycle);
    }
}
