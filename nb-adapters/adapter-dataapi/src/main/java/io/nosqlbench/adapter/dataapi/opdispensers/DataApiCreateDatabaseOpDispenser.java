/*
 * Copyright (c) 2024 nosqlbench
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

import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.DataApiCreateDatabaseOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class DataApiCreateDatabaseOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiCreateDatabaseOpDispenser.class);
    private final LongFunction<DataApiCreateDatabaseOp> opFunction;
    public DataApiCreateDatabaseOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiCreateDatabaseOp> createOpFunction(ParsedOp op) {
        return (l) -> new DataApiCreateDatabaseOp(
            spaceFunction.apply(l).getDatabase(),
            spaceFunction.apply(l).getAdmin(),
            targetFunction.apply(l),
            getCloudProvider(op, l),
            getRegion(op, l),
            getWaitUntilActive(op, l)
        );
    }

    /*
     * These default to the same values used in the API if only the name is provided.
     */
    private CloudProviderType getCloudProvider(ParsedOp op, long l) {
        return CloudProviderType.valueOf(op.getAsFunctionOr("cloudProvider", "GCP").apply(l));
    }

    private String getRegion(ParsedOp op, long l) {
        return op.getAsFunctionOr("region", "us-east1").apply(l);
    }

    private boolean getWaitUntilActive(ParsedOp op, long l) {
        return op.getAsFunctionOr("waitUntilActive", "true").apply(l).equals("true");
    }

    @Override
    public DataApiBaseOp getOp(long value) {
        return opFunction.apply(value);
    }
}
