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

import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.commands.options.DropTableIndexOptions;
import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.DataApiDbDropTableIndexOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

/**
 * Dispenser for the {@code db_drop_table_index} op type.
 *
 * <p>Required YAML fields:
 * <ul>
 *   <li>the op value ({@code db_drop_table_index: "name"}) — name of the index to drop</li>
 * </ul>
 * Optional: {@code if_exists} (boolean).
 */
public class DataApiDbDropTableIndexOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiDbDropTableIndexOpDispenser.class);
    private final LongFunction<DataApiDbDropTableIndexOp> opFunction;

    public DataApiDbDropTableIndexOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiDbDropTableIndexOp> createOpFunction(ParsedOp op) {
        return (l) -> {
            Database db = spaceFunction.apply(l).getDatabase();
            String name = targetFunction.apply(l);
            DropTableIndexOptions options = getDropIndexOptionsFromOp(op, l);
            return new DataApiDbDropTableIndexOp(db, name, options);
        };
    }

    @Override
    public DataApiBaseOp getOp(long cycle) {
        return opFunction.apply(cycle);
    }
}
