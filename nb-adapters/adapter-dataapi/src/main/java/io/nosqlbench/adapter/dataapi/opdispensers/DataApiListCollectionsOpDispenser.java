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

package io.nosqlbench.adapter.dataapi.opdispensers;

import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.DataApiListCollectionsOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class DataApiListCollectionsOpDispenser extends DataApiOpDispenser {
    private final LongFunction<DataApiListCollectionsOp> opFunction;
    public DataApiListCollectionsOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiListCollectionsOp> createOpFunction(ParsedOp op) {
        return l -> {
            DataApiListCollectionsOp dataApiListCollectionsOp =
                new DataApiListCollectionsOp(
                    spaceFunction.apply(l).getDatabase());
            return dataApiListCollectionsOp;
        };
    }

    @Override
    public DataApiBaseOp getOp(long value) {
        return opFunction.apply(value);
    }
}
