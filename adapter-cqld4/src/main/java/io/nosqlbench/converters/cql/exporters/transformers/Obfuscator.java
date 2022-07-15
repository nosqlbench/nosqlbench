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

/**
 * Create random but stable names for all elements.
 * Use a deterministic method for creating names.
 * once an element is named, use the same name throughout
 * prefix each element type with a code for the type
 */
package io.nosqlbench.converters.cql.exporters.transformers;

import io.nosqlbench.converters.cql.cqlast.CqlColumnDef;
import io.nosqlbench.converters.cql.cqlast.CqlKeyspace;
import io.nosqlbench.converters.cql.cqlast.CqlModel;
import io.nosqlbench.converters.cql.cqlast.CqlTable;

import java.util.function.Function;

public class Obfuscator implements Function<CqlModel,CqlModel> {

    private final NameRemapper remapper = new NameRemapper();

    @Override
    public CqlModel apply(CqlModel model) {

        for (CqlKeyspace keyspace : model.getKeyspaces()) {
            String ksname = keyspace.getName();
            String ksnewname = remapper.nameFor(keyspace);
            keyspace.setKeyspaceName(ksnewname);
            keyspace.setRefDdl();

            for (CqlTable cqlTable : model.getTablesForKeyspace(keyspace.getName())) {
                String tablename = cqlTable.getName();
                String tbnewname = remapper.nameFor(cqlTable);
                cqlTable.setName(tbnewname);

                for (CqlColumnDef coldef : cqlTable.getColumnDefinitions()) {
                    String colname = coldef.getName();
                    String replacement = remapper.nameFor(coldef);
                    coldef.setName(replacement);
                }

            }

        }

    }
}
