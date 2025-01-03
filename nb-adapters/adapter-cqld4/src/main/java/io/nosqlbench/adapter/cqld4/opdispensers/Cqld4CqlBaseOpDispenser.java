package io.nosqlbench.adapter.cqld4.opdispensers;

/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import com.datastax.oss.driver.api.core.cql.ColumnDefinition;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.Row;
import io.nosqlbench.adapter.cqld4.Cqld4DriverAdapter;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4BaseOp;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.virtdata.core.templates.CapturePoint;
import io.nosqlbench.virtdata.core.templates.CapturePointException;
import io.nosqlbench.virtdata.core.templates.CapturePoints;
import io.nosqlbench.virtdata.core.templates.UniformVariableCapture;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class Cqld4CqlBaseOpDispenser<T extends Cqld4CqlOp> extends Cqld4BaseOpDispenser<T>
    implements UniformVariableCapture<List<Row>>
{

    public Cqld4CqlBaseOpDispenser(Cqld4DriverAdapter adapter, ParsedOp op) {
        super(adapter, op);
    }

    @Override
    public abstract T getOp(long cycle);

    @Override
    public Function<List<Row>, Map<String, ?>> captureF(CapturePoints<List<Row>> points) {
        Function<List<Row>, Map<String, ?>> f = (List<Row> result) -> {
            if (result.size() != 1) {
                throw new CapturePointException(
                    "result contained " + result.size() + " rows, required exactly 1");
            }
            Row row = result.get(0);
            ColumnDefinitions coldefs = row.getColumnDefinitions();
            Map<String, Object> values = new HashMap<>(coldefs.size());

            if (points.isGlob()) {
                for (ColumnDefinition coldef : coldefs) {
                    String colname = coldef.getName().toString();
                    values.put(colname, row.getObject(colname));
                }
            } else {
                for (CapturePoint<List<Row>> point : points) {
                    String sourceName = point.getSourceName();
                    Object value = row.getObject(point.getSourceName());
                    Object recast = point.getAsCast().cast(value);
                    values.put(point.getAsName(), recast);
                }
            }

            return values;
        };
        return f;
    }

}
