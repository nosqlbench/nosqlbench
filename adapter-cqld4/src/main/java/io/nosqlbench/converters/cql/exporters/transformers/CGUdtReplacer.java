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

import io.nosqlbench.converters.cql.cqlast.CqlColumnDef;
import io.nosqlbench.converters.cql.cqlast.CqlModel;
import io.nosqlbench.converters.cql.cqlast.CqlTable;

import java.util.List;

public class CGUdtReplacer implements CGModelTransformer {

    @Override
    public CqlModel apply(CqlModel model) {
        List<String> toReplace = model.getTypes().stream().map(t -> t.getKeyspace() + "." + t.getName()).toList();
        for (CqlTable table : model.getTableDefs()) {
            for (CqlColumnDef coldef : table.getColumnDefinitions()) {
                String coldefDdl = coldef.getDefinitionDdl();
                for (String searchFor : toReplace) {
                    if (coldefDdl.contains(searchFor)) {
                        String typedef = coldef.getType();
                        coldef.setType("blob");
                        String replaced = coldef.getDefinitionDdl().replace(typedef, "blob");
                        coldef.setDefinitionRefDdl(replaced);
                        table.setRefDdl(table.getRefDdl().replace(typedef,"blob"));
                    }
                }
            }
        }

        return model;
    }


}
