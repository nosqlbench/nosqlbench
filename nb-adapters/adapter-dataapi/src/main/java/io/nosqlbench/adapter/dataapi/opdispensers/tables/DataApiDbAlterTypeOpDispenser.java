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

import com.datastax.astra.client.tables.commands.AlterTypeOperation;
import com.datastax.astra.client.tables.commands.options.AlterTypeOptions;
import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.opdispensers.DataApiOpDispenser;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.tables.DataApiDbAlterTypeOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

/**
 * Dispenser for the {@code db_alter_type} op type.
 *
 * <p>Required YAML fields:
 * <ul>
 *   <li>the op value ({@code db_alter_type: "name"}) — name of the UDT to alter</li>
 *   <li>{@code alter_op} — map with one of (note: cannot use "operation" as that is a reserved NB keyword):
 *     <ul>
 *       <li>{@code add_fields} — map of fieldName -&gt; type string to add</li>
 *       <li>{@code rename_fields} — map of oldName -&gt; newName</li>
 *     </ul>
 *   </li>
 * </ul>
 */
public class DataApiDbAlterTypeOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiDbAlterTypeOpDispenser.class);
    private final LongFunction<DataApiDbAlterTypeOp> opFunction;

    public DataApiDbAlterTypeOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiDbAlterTypeOp> createOpFunction(ParsedOp op) {
        return (l) -> {
            String name = targetFunction.apply(l);
            AlterTypeOperation<?, ?> operation = getAlterTypeOperationFromOp(op, l);
            AlterTypeOptions options = getAlterTypeOptionsFromOp(op, l);
            return new DataApiDbAlterTypeOp(
                spaceFunction.apply(l).getDatabase(),
                name,
                operation,
                options
            );
        };
    }

    @Override
    public DataApiBaseOp getOp(long cycle) {
        return opFunction.apply(cycle);
    }
}
