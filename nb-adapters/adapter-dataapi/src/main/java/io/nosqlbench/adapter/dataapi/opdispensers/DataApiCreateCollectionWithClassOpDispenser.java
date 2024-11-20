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
import io.nosqlbench.adapter.dataapi.ops.DataApiCreateCollectionWithClassOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class DataApiCreateCollectionWithClassOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiCreateCollectionWithClassOpDispenser.class);
    private final LongFunction<DataApiCreateCollectionWithClassOp> opFunction;

    public DataApiCreateCollectionWithClassOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiCreateCollectionWithClassOp> createOpFunction(ParsedOp op) {
        return (l) -> new DataApiCreateCollectionWithClassOp(
            spaceFunction.apply(l).getDatabase(),
            targetFunction.apply(l),
            this.getCollectionOptionsFromOp(op, l),
            getCreateClass(op, l)
        );
    }

    private Class<?> getCreateClass(ParsedOp op, long l) {
        String className = op.getAsFunctionOr("createClass", "com.datastax.astra.client.model.Document").apply(l);
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DataApiBaseOp getOp(long cycle) {
        return opFunction.apply(cycle);
    }


}
