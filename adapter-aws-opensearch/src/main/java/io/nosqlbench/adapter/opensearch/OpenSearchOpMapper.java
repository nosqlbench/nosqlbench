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

package io.nosqlbench.adapter.opensearch;

import io.nosqlbench.adapter.opensearch.dispensers.*;
import io.nosqlbench.adapter.opensearch.ops.UpdateOp;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import org.opensearch.client.opensearch.OpenSearchClient;

public class OpenSearchOpMapper implements OpMapper<Op> {
    private final OpenSearchAdapter adapter;

    public OpenSearchOpMapper(OpenSearchAdapter openSearchAdapter) {
        this.adapter = openSearchAdapter;
    }

    @Override
    public OpDispenser<? extends Op> apply(ParsedOp op) {
        TypeAndTarget<OpenSearchOpTypes, String> typeAndTarget =
            op.getTypeAndTarget(OpenSearchOpTypes.class, String.class, "verb", "index");
        return switch (typeAndTarget.enumId) {
            case create_index -> new CreateIndexOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case delete_index -> new DeleteIndexOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case index -> new IndexOpDispenser(adapter,op, typeAndTarget.targetFunction);
            case update -> new UpdateOpDispenser(adapter,op, typeAndTarget.targetFunction);
            case delete -> new DeleteOpDispenser(adapter,op, typeAndTarget.targetFunction);
            case knn_search -> new KnnSearchOpDispenser(adapter,op, typeAndTarget.targetFunction);
            default -> throw new RuntimeException("Unrecognized op type '" + typeAndTarget.enumId.name() + "' while " +
                "mapping parsed op " + op);
        };
    }
}
