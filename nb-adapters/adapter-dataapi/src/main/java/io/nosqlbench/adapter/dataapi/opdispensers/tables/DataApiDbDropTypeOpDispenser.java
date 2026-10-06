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

package io.nosqlbench.adapter.dataapi.opdispensers.tables;

import com.datastax.astra.client.tables.commands.options.DropTypeOptions;
import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.opdispensers.DataApiOpDispenser;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.tables.DataApiDbDropTypeOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

/**
 * Dispenser for the {@code db_drop_type} op type.
 *
 * <p>Required YAML fields:
 * <ul>
 *   <li>{@code type_name} — name of the UDT to drop</li>
 * </ul>
 */
public class DataApiDbDropTypeOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiDbDropTypeOpDispenser.class);
    private final LongFunction<DataApiDbDropTypeOp> opFunction;

    public DataApiDbDropTypeOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiDbDropTypeOp> createOpFunction(ParsedOp op) {
        return (l) -> {
            String typeName = op.getAsRequiredFunction("type_name", String.class).apply(l);
            DropTypeOptions options = getDropTypeOptionsFromOp(op, l);
            return new DataApiDbDropTypeOp(
                spaceFunction.apply(l).getDatabase(),
                typeName,
                options
            );
        };
    }

    @Override
    public DataApiBaseOp getOp(long cycle) {
        return opFunction.apply(cycle);
    }
}
