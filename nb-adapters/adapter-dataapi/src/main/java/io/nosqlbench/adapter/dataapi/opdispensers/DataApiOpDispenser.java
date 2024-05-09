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

package io.nosqlbench.adapter.dataapi.opdispensers;

import com.datastax.astra.client.model.*;
import io.nosqlbench.adapter.dataapi.DataApiSpace;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

public abstract class DataApiOpDispenser extends BaseOpDispenser<DataApiBaseOp, DataApiSpace> {
    protected final LongFunction<String> targetFunction;
    protected final LongFunction<DataApiSpace> spaceFunction;

    protected DataApiOpDispenser(DriverAdapter<? extends DataApiBaseOp, DataApiSpace> adapter, ParsedOp op,
                                 LongFunction<String> targetFunction) {
        super(adapter, op);
        this.targetFunction = targetFunction;
        this.spaceFunction = adapter.getSpaceFunc(op);
    }

    protected Sort getSortFromOp(ParsedOp op, long l) {
        Sort sort = null;
        Optional<LongFunction<Map>> sortFunction = op.getAsOptionalFunction("sort", Map.class);
        if (sortFunction.isPresent()) {
            Map<String,Object> sortFields = sortFunction.get().apply(l);
            String sortOrder = sortFields.get("type").toString();
            String sortField = sortFields.get("field").toString();
            switch(sortOrder) {
                case "asc" -> sort = Sorts.ascending(sortField);
                case "desc" -> sort = Sorts.descending(sortField);
            }
        }
        return sort;
    }

    protected Filter getFilterFromOp(ParsedOp op, long l) {
        Filter filter = null;
        Optional<LongFunction<Map>> filterFunction = op.getAsOptionalFunction("filters", Map.class);
        if (filterFunction.isPresent()) {
            Map<String,Object> filters = filterFunction.get().apply(l);
            List<Filter> filterList = new ArrayList<>();
            String conjunction = "and";
            for (Map.Entry<String, Object> entry : filters.entrySet()) {
                if (entry.getKey().equalsIgnoreCase("filter")) {
                    Map<String,Object> filterFields = (Map<String, Object>) entry.getValue();
                    switch (filterFields.get("operation").toString()) {
                        case "lt" ->
                            filterList.add(Filters.lt(filters.get("field").toString(), (long) filters.get("value")));
                        case "gt" ->
                            filterList.add(Filters.gt(filters.get("field").toString(), (long) filters.get("value")));
                        case "eq" -> filterList.add(Filters.eq(filters.get("field").toString(), filters.get("value")));
                        default -> logger.error("Operation not supported");
                    }
                } else if (entry.getKey().equalsIgnoreCase("conjunction")) {
                    conjunction = (String) entry.getValue();
                } else {
                    logger.error("Filter " + entry.getKey() + " not supported");
                }
            }
            if (conjunction.equalsIgnoreCase("and")) {
                filter = Filters.and(filterList);
            } else if (conjunction.equalsIgnoreCase("or")) {
                filter = Filters.or(filterList);
            } else {
                logger.error("Conjunction " + conjunction + " not supported");
            }
        }
        return filter;
    }

    protected Update getUpdates(ParsedOp op, long l) {
        Update update = new Update();
        Optional<LongFunction<Map>> updatesFunction = op.getAsOptionalFunction("updates", Map.class);
        if (updatesFunction.isPresent()) {
            Map<String, Object> updates = updatesFunction.get().apply(l);
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                if (entry.getKey().equalsIgnoreCase("update")) {
                    Map<String, Object> updateFields = (Map<String, Object>) entry.getValue();
                    switch (updateFields.get("operation").toString()) {
                        case "set" ->
                            update = Updates.set(updateFields.get("field").toString(), updateFields.get("value"));
                        case "inc" ->
                            update = Updates.inc(updateFields.get("field").toString(), (double) updateFields.get("value"));
                        case "unset" -> update = Updates.unset(updateFields.get("field").toString());
                        case "addToSet" ->
                            update = Updates.addToSet(updateFields.get("field").toString(), updateFields.get("value"));
                        case "min" ->
                            update = Updates.min(updateFields.get("field").toString(), (double) updateFields.get("value"));
                        case "rename" ->
                            update = Updates.rename(updateFields.get("field").toString(), updateFields.get("value").toString());
                        default -> logger.error("Operation " + updateFields.get("operation") + " not supported");
                    }
                } else {
                    logger.error("Filter " + entry.getKey() + " not supported");
                }
            }
        }
        return update;
    }

}
