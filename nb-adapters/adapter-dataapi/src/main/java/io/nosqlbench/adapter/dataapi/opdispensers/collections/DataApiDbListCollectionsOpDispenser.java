/*
 * Copyright (c) 2020-2024 nosqlbench
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

package io.nosqlbench.adapter.dataapi.opdispensers.collections;

import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.collections.DataApiDbListCollectionsOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.function.LongFunction;
import io.nosqlbench.adapter.dataapi.opdispensers.DataApiOpDispenser;

public class DataApiDbListCollectionsOpDispenser extends DataApiOpDispenser {
    private final LongFunction<DataApiDbListCollectionsOp> opFunction;
    public DataApiDbListCollectionsOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiDbListCollectionsOp> createOpFunction(ParsedOp op) {
        return l -> {
            DataApiDbListCollectionsOp dataApiListCollectionsOp =
                new DataApiDbListCollectionsOp(
                    spaceFunction.apply(l).getDatabase());
            return dataApiListCollectionsOp;
        };
    }

    @Override
    public DataApiBaseOp getOp(long cycle) {
        return opFunction.apply(cycle);
    }
}
