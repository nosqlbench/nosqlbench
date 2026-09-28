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
import com.datastax.astra.client.collections.commands.options.CollectionFindAndRerankOptions;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.rerank.RerankServiceOptions;
import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.DataApiCollectionFindAndRerankOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;
import java.util.Map;
import java.util.Optional;

public class DataApiCollectionFindAndRerankOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiCollectionFindAndRerankOpDispenser.class);
    private final LongFunction<DataApiCollectionFindAndRerankOp> opFunction;

    public DataApiCollectionFindAndRerankOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiCollectionFindAndRerankOp> createOpFunction(ParsedOp op) {
        return (l) -> {
            Database db = spaceFunction.apply(l).getDatabase();
            Filter filter = getFilterFromOp(op, l);
            CollectionFindAndRerankOptions options = getCollectionFindAndRerankOptions(op, l);

            return new DataApiCollectionFindAndRerankOp(
                db,
                db.getCollection(targetFunction.apply(l)),
                filter,
                options
            );
        };
    }

    private CollectionFindAndRerankOptions getCollectionFindAndRerankOptions(ParsedOp op, long l) {
        CollectionFindAndRerankOptions options = new CollectionFindAndRerankOptions();
        Sort sort = getFindAndRerankSortFromOp(op, l);
        options = options.sort(sort);
        Projection[] projection = getProjectionFromOp(op, l);
        if (projection != null) {
            options = options.projection(projection);
        }
        Optional<Integer> limit = getLimitFromOp(op, l);
        if (limit.isPresent()) {
            options = options.limit(limit.get());
        }
        Optional<Boolean> includeSortVector = getIncludeSortVectorFromOp(op, l);
        if (includeSortVector.isPresent()) {
            options = options.includeSortVector(includeSortVector.get());
        }
        Optional<Boolean> includeScores = getIncludeScoresFromOp(op, l);
        if (includeScores.isPresent()) {
            options = options.includeScores(includeScores.get());
        }
        Optional<String> rerankQuery = getRerankQueryFromOp(op, l);
        if (rerankQuery.isPresent()) {
            options = options.rerankQuery(rerankQuery.get());
        }
        Optional<String> rerankOn = getRerankOnFromOp(op, l);
        if (rerankOn.isPresent()) {
            options = options.rerankOn(rerankOn.get());
        }
        Optional<Object> hybridLimits = getHybridLimitsFromOp(op, l);
        if (hybridLimits.isPresent()) {
            Object hl = hybridLimits.get();
            if (hl instanceof Integer i) {
                options = options.hybridLimits(i);
            } else {
                @SuppressWarnings("unchecked")
                Map<String, Integer> hlMap = (Map<String, Integer>) hl;
                options = options.hybridLimits(hlMap);
            }
        }
        Optional<RerankServiceOptions> rerankService = getRerankServiceFromOp(op, l);
        if (rerankService.isPresent()) {
            options = options.rerankService(rerankService.get());
        }
        // TEMP
        RerankServiceOptions rop = new RerankServiceOptions();
        rop.provider("providerZ");
        rop.modelName("modelNameZ");
        options = options.rerankService(rop);
        // END TEMP

        return options;
    }

    @Override
    public DataApiBaseOp getOp(long cycle) {
        return opFunction.apply(cycle);
    }
}
