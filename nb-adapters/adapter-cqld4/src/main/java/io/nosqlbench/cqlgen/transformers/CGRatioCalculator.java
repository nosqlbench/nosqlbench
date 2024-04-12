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

package io.nosqlbench.cqlgen.transformers;

import io.nosqlbench.cqlgen.api.CGModelTransformer;
import io.nosqlbench.cqlgen.core.CGTableStats;
import io.nosqlbench.cqlgen.model.CqlModel;
import io.nosqlbench.cqlgen.model.CqlTable;

public class CGRatioCalculator implements CGModelTransformer {

    private String name;

    @Override
    public CqlModel apply(CqlModel model) {
        if (!model.hasStats()) {
            // TODO: True this up
            return model;
        }

        double totalReads = 0.0d;
        double totalWrites = 0.0d;
        double totalSpace = 0.0d;
        double totalOps = 0.0d;

        for (CqlTable table : model.getTableDefs()) {
            CGTableStats tableAttributes = table.getTableAttributes();
            if (tableAttributes == null) {
                continue;
            }
            String local_read_count = tableAttributes.getAttribute("Local read count");
            double reads = Double.parseDouble(local_read_count);
            totalReads += reads;
            totalOps += reads;

            String local_write_count = table.getTableAttributes().getAttribute("Local write count");
            double writes = Double.parseDouble(local_write_count);
            totalWrites += writes;
            totalOps += writes;

            String space_used_total = table.getTableAttributes().getAttribute("Space used (total)");
            double space = Double.parseDouble(space_used_total);
            totalSpace += space;
        }

        for (CqlTable table : model.getTableDefs()) {
            double reads = Double.parseDouble(table.getTableAttributes().getAttribute("Local read count"));
            double writes = Double.parseDouble(table.getTableAttributes().getAttribute("Local write count"));

            double totalTableReads = reads / totalOps;
            double totalTableWrites = writes / totalOps;
            double tableSpaceUsed = Double.parseDouble(table.getTableAttributes().getAttribute("Space used (total)"));
            double weighted_space = tableSpaceUsed / totalSpace;
            double op_share_of_total_ops = totalTableReads + totalTableWrites;
            ComputedTableStats computedTableStats = new ComputedTableStats()
                .setReadShareOfTotalOps(reads / totalOps)
                .setReadShareOfTotalReads(reads / totalReads)
                .setWriteShareOfTotalOps(writes / totalOps)
                .setWriteShareOfTotalWrites(writes / totalWrites)
                .setOpShareOfTotalOps(op_share_of_total_ops)
                .setSpaceUsedOfTotalSpace(weighted_space);
            table.setComputedStats(computedTableStats);
        }

        return model;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String getName() {
        return this.name;
    }
}
