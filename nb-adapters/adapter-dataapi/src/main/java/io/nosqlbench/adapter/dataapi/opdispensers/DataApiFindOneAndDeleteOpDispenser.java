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
import com.datastax.astra.client.model.FindOneAndDeleteOptions;
import com.datastax.astra.client.model.Projection;
import com.datastax.astra.client.model.Sort;
import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.DataApiFindOneAndDeleteOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class DataApiFindOneAndDeleteOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiFindOneAndDeleteOpDispenser.class);
    private final LongFunction<DataApiFindOneAndDeleteOp> opFunction;

    public DataApiFindOneAndDeleteOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiFindOneAndDeleteOp> createOpFunction(ParsedOp op) {
        return (l) -> {
            Database db = spaceFunction.apply(l).getDatabase();
            Filter filter = getFilterFromOp(op, l);
            FindOneAndDeleteOptions options = getFindOneAndDeleteOptions(op, l);

            return new DataApiFindOneAndDeleteOp(
                db,
                db.getCollection(targetFunction.apply(l)),
                filter,
                options
            );
        };
    }

    private FindOneAndDeleteOptions getFindOneAndDeleteOptions(ParsedOp op, long l) {
        FindOneAndDeleteOptions options = new FindOneAndDeleteOptions();
        Sort sort = getSortFromOp(op, l);
        if (sort != null) {
            options = options.sort(sort);
        }
        Projection[] projection = getProjectionFromOp(op, l);
        if (projection != null) {
            options = options.projection(projection);
        }
        return options;
    }

    @Override
    public DataApiBaseOp getOp(long value) {
        return opFunction.apply(value);
    }
}
