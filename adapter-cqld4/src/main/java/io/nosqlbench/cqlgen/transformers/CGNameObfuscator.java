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
package io.nosqlbench.cqlgen.transformers;

import io.nosqlbench.cqlgen.api.CGModelTransformer;
import io.nosqlbench.cqlgen.api.CGTransformerConfigurable;
import io.nosqlbench.cqlgen.model.CqlModel;
import io.nosqlbench.cqlgen.model.CqlTable;
import io.nosqlbench.cqlgen.model.CqlType;
import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.VirtData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

public class CGNameObfuscator implements CGModelTransformer, CGTransformerConfigurable {
    private final static Logger logger = LogManager.getLogger(CGNameObfuscator.class);

    private final CGCachingNameRemapper remapper = new CGCachingNameRemapper();

    @Override
    public CqlModel apply(CqlModel model) {

        for (String keyspaceName : model.getAllKnownKeyspaceNames()) {
            String newKeyspaceName = remapper.nameForType("keyspace", keyspaceName, "ks_");
            model.renamekeyspace(keyspaceName, newKeyspaceName);
        }

        for (CqlTable cqlTable : model.getTableDefs()) {
            String tablename = cqlTable.getName();
            String newTableName = remapper.nameFor(cqlTable, "tbl_");
            model.renameTable(cqlTable, newTableName);
            cqlTable.renameColumns(remapper.mapperForType(cqlTable, "col_"));
        }

        for (CqlType type : model.getTypeDefs()) {
            String typeName = type.getName();
            String newTypeName = remapper.nameFor(type, "typ_");
            model.renameType(type.getKeyspace(), typeName, newTypeName);
            type.renameColumns(remapper.mapperForType(type, "typ"));
        }

        return model;

    }

    @Override
    public void accept(Object configObject) {
        if (configObject instanceof Map cfgmap) {
            Object namer = cfgmap.get("namer");
            Optional<DataMapper<String>> optionalMapper = VirtData.getOptionalMapper(namer.toString());
            LongFunction<String> namerFunc = optionalMapper.orElseThrow(
                () -> new RuntimeException("Unable to resolve obfuscator namer '" + namer + "'")
            );
            remapper.setNamingFunction(namerFunc);
        } else {
            throw new RuntimeException("name obfuscator requires a map for its configuration value.");
        }
    }
}
