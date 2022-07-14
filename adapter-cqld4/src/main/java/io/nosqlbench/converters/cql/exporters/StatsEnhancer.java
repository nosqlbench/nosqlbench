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

package io.nosqlbench.converters.cql.exporters;

import io.nosqlbench.converters.cql.cqlast.CqlModel;
import io.nosqlbench.converters.cql.cqlast.CqlTable;

import java.util.Map;
import java.util.function.Function;

public class StatsEnhancer implements Function<CqlModel, CqlModel> {
    private final CqlSchemaStats schemaStats;

    public StatsEnhancer(CqlSchemaStats schemaStats) {
        this.schemaStats = schemaStats;
    }

    @Override
    public CqlModel apply(CqlModel model) {
        if (schemaStats != null) {
            //TODO: rewrite this in something resembling an efficient way
            CqlKeyspaceStats ksStats = null;
            for (String ksName : model.getKeyspaces().keySet()) {
                if ((ksStats = schemaStats.getKeyspace(ksName)) != null) {
                    model.getKeyspaces().get(ksName).setKeyspaceAttributes(ksStats.getKeyspaceAttributes());
                    Map<String, CqlTable> ksTables = model.getTablesByKeyspace().get(ksName);
                    for (String tableName : ksTables.keySet()) {
                        if (ksStats.getKeyspaceTable(tableName) != null) {
                            model.getTablesByKeyspace().get(ksName).get(tableName)
                                .setTableAttributes(ksStats.getKeyspaceTable(tableName).getAttributes());
                        }
                    }
                }
            }

        }
        return model;
    }
}
