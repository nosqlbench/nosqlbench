/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.converters.cql.exporters.transformers;

import io.nosqlbench.converters.cql.cqlast.CqlModel;
import io.nosqlbench.converters.cql.cqlast.CqlTable;

import java.util.function.Function;

public class RatioCalculator implements Function<CqlModel,CqlModel> {

    @Override
    public CqlModel apply(CqlModel model) {
        if (!model.hasStats()) {
            return model;
        }
        double totalReads = 0.0d;
        double totalWrites = 0.0d;
        double totalSpace = 0.0d;
        double totalOps=0.0d;

        for (CqlTable table : model.getAllTables()) {
            String local_read_count = table.getTableAttributes().get("Local read count");
            double reads = Double.parseDouble(local_read_count);
            totalReads+=reads;
            totalOps+=reads;

            String local_write_count = table.getTableAttributes().get("Local write count");
            double writes = Double.parseDouble(local_write_count);
            totalWrites += writes;
            totalOps+=writes;

            String space_used_total = table.getTableAttributes().get("Space used (total)");
            double space = Double.parseDouble(space_used_total);
            totalSpace+=space;
        }

        for (CqlTable table : model.getAllTables()) {
            double reads = Double.parseDouble(table.getTableAttributes().get("Local read count"));
            double writes = Double.parseDouble(table.getTableAttributes().get("Local write count"));

            table.getTableAttributes().put("weighted_reads", String.valueOf(reads / totalOps));
            table.getTableAttributes().put("weighted_writes", String.valueOf(writes / totalOps));

            table.getTableAttributes().put("weighted_space", String.valueOf(Double.parseDouble(table.getTableAttributes().get("Space used (total)")) / totalReads));
        }

        return model;
    }


}
