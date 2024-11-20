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

import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.Filter;
import com.datastax.astra.client.model.FindOptions;
import com.datastax.astra.client.model.Projection;
import com.datastax.astra.client.model.Sort;
import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.DataApiFindOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.function.LongFunction;

public class DataApiFindOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiFindOpDispenser.class);
    private final LongFunction<DataApiFindOp> opFunction;
    public DataApiFindOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiFindOp> createOpFunction(ParsedOp op) {
        return (l) -> {
            Database db = spaceFunction.apply(l).getDatabase();
            Filter filter = getFilterFromOp(op, l);
            FindOptions options = getFindOptions(op, l);
            return new DataApiFindOp(
                db,
                db.getCollection(targetFunction.apply(l)),
                filter,
                options
            );
        };
    }

    private FindOptions getFindOptions(ParsedOp op, long l) {
        FindOptions options = new FindOptions();
        Sort sort = getSortFromOp(op, l);
        if (op.isDefined("vector")) {
            float[] vector = getVectorValues(op, l);
            if (sort != null) {
                options = vector != null ? options.sort(vector, sort) : options.sort(sort);
            } else if (vector != null) {
                options = options.sort(vector);
            }
        }
        Projection[] projection = getProjectionFromOp(op, l);
        if (projection != null) {
            options = options.projection(projection);
        }
        Optional<LongFunction<Integer>> limitFunction = op.getAsOptionalFunction("limit", Integer.class);
        if (limitFunction.isPresent()) {
            options = options.limit(limitFunction.get().apply(l));
        }
        Optional<LongFunction<Integer>> skipFunction = op.getAsOptionalFunction("skip", Integer.class);
        if (skipFunction.isPresent()) {
            options = options.skip(skipFunction.get().apply(l));
        }
        Optional<LongFunction<Boolean>> includeSimilarityFunction = op.getAsOptionalFunction("includeSimilarity", Boolean.class);
        if (includeSimilarityFunction.isPresent()) {
            options.setIncludeSimilarity(includeSimilarityFunction.get().apply(l));
        }
        Optional<LongFunction<String>> pageStateFunction = op.getAsOptionalFunction("pageState", String.class);
        if (pageStateFunction.isPresent()) {
            options.setPageState(pageStateFunction.get().apply(l));
        }
        return options;
    }

    @Override
    public DataApiBaseOp getOp(long cycle) {
        return opFunction.apply(cycle);
    }
}
