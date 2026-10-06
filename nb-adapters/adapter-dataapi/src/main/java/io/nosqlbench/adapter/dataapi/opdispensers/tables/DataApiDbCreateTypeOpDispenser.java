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

import com.datastax.astra.client.tables.commands.options.CreateTypeOptions;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeDefinition;
import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.opdispensers.DataApiOpDispenser;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.tables.DataApiDbCreateTypeOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

/**
 * Dispenser for the {@code db_create_type} op type.
 *
 * <p>Required YAML fields:
 * <ul>
 *   <li>{@code type_name} — name of the UDT to create</li>
 *   <li>{@code type_definition.fields} — map of fieldName -&gt; column type string</li>
 * </ul>
 * Optional: {@code if_not_exists} (boolean).
 */
public class DataApiDbCreateTypeOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiDbCreateTypeOpDispenser.class);
    private final LongFunction<DataApiDbCreateTypeOp> opFunction;

    public DataApiDbCreateTypeOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiDbCreateTypeOp> createOpFunction(ParsedOp op) {
        return (l) -> {
            String typeName = op.getAsRequiredFunction("type_name", String.class).apply(l);
            TableUserDefinedTypeDefinition definition = getUdtDefinitionFromOp(op, l);
            CreateTypeOptions options = getCreateTypeOptionsFromOp(op, l);
            return new DataApiDbCreateTypeOp(
                spaceFunction.apply(l).getDatabase(),
                typeName,
                definition,
                options
            );
        };
    }

    @Override
    public DataApiBaseOp getOp(long cycle) {
        return opFunction.apply(cycle);
    }
}
