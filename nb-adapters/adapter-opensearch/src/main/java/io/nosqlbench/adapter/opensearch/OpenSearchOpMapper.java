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

package io.nosqlbench.adapter.opensearch;

import io.nosqlbench.adapter.opensearch.dispensers.*;
import io.nosqlbench.adapter.opensearch.ops.OpenSearchBaseOp;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import io.nosqlbench.nb.api.components.core.NBComponent;

import java.util.function.LongFunction;

public class OpenSearchOpMapper implements OpMapper<OpenSearchBaseOp, OpenSearchSpace> {
    private final OpenSearchAdapter adapter;

    public OpenSearchOpMapper(OpenSearchAdapter openSearchAdapter) {
        this.adapter = openSearchAdapter;
    }

    @Override
    public OpDispenser<? extends OpenSearchBaseOp> apply(NBComponent adapterC, ParsedOp op, LongFunction<OpenSearchSpace> spaceF) {
        TypeAndTarget<OpenSearchOpTypes, String> typeAndTarget =
            op.getTypeAndTarget(OpenSearchOpTypes.class, String.class, "verb", "index");
        return switch (typeAndTarget.enumId) {
            case create_index -> new OpenSearchCreateIndexOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case delete_index -> new OpenSearchDeleteIndexOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case index -> new OpenSearchIndexOpDispenser(adapter,op, typeAndTarget.targetFunction);
            case update -> new OpenSearchUpdateOpDispenser(adapter,op, typeAndTarget.targetFunction);
            case delete -> new OpenSearchDeleteOpDispenser(adapter,op, typeAndTarget.targetFunction);
            case search -> new OpenSearchSearchOpDispenser(adapter,op, typeAndTarget.targetFunction);
            case knn_search -> new OpenSearchKnnSearchOpDispenser(adapter,op, typeAndTarget.targetFunction);
            case bulk -> new OpenSearchBulkOpDispenser(adapter, op, typeAndTarget.targetFunction);
            default -> throw new RuntimeException("Unrecognized op type '" + typeAndTarget.enumId.name() + "' while " +
                "mapping parsed op " + op);
        };
    }
}
