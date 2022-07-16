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

import io.nosqlbench.converters.cql.cqlast.CqlModel;
import io.nosqlbench.converters.cql.cqlast.CqlTable;
import io.nosqlbench.converters.cql.cqlast.CqlType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CGNameObfuscator implements CGModelTransformer {
    private final static Logger logger = LogManager.getLogger(CGNameObfuscator.class);

    private final CGCachingNameRemapper remapper = new CGCachingNameRemapper();
    private Object keyspaceName;

    @Override
    public CqlModel apply(CqlModel model) {

        for (String keyspaceName : model.getAllKnownKeyspaceNames()) {
            String newKeyspaceName = remapper.nameForType("keyspace",keyspaceName);
            model.renamekeyspace(keyspaceName,newKeyspaceName);
        }
        for (CqlTable cqlTable : model.getTableDefs()) {
            String tablename = cqlTable.getName();
            String newTableName = remapper.nameFor(cqlTable);
            model.renameTable(cqlTable.getKeySpace(), tablename, newTableName);
            cqlTable.renameColumns(remapper.mapperForType(cqlTable));
        }
        for (CqlType type : model.getTypes()) {
            String typeName = type.getName();
            String newTypeName = remapper.nameFor(type);
            model.renameType(type.getKeyspace(),typeName,newTypeName);
        }


        return model;

    }
}
