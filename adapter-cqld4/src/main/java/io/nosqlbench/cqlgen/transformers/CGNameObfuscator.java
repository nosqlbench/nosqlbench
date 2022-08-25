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

import io.nosqlbench.api.config.standard.*;
import io.nosqlbench.cqlgen.api.CGModelTransformer;
import io.nosqlbench.cqlgen.core.CGWorkloadExporter;
import io.nosqlbench.cqlgen.model.*;
import io.nosqlbench.cqlgen.transformers.namecache.*;
import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.VirtData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.Map;

public class CGNameObfuscator implements CGModelTransformer, NBConfigurable {
    private final static Logger logger = LogManager.getLogger(CGWorkloadExporter.APPNAME+"/name-obfuscator");

    NameCache cache = new NameCache();
    private final CGCachingNameRemapper remapper = new CGCachingNameRemapper();
    private String name;
    private String mapfile;

    @Override
    public CqlModel apply(CqlModel model) {
        remapper.setTypePrefixes(Map.of("keyspace", "ks_", "type", "typ_", "table", "tb_", "column","col_"));

        if (mapfile != null) {
            cache = NameCache.loadOrCreate(Path.of(mapfile));
        }

        for (CqlKeyspaceDef keyspaceDef : model.getKeyspaceDefs()) {
            NamedKeyspace namedKeyspace = cache.keyspace(keyspaceDef.getName());
            String newKeyspaceName = namedKeyspace.computeAlias(keyspaceDef, remapper::nameFor);
            keyspaceDef.setKeyspaceName(newKeyspaceName);

            for (CqlType typeDef : keyspaceDef.getTypeDefs()) {
                NamedType namedType = namedKeyspace.type(typeDef.getName());
                String typeDefName = namedType.computeAlias(typeDef,remapper::nameFor);
                namedType.setName(typeDefName);

                for (CqlTypeColumn columnDef : typeDef.getColumnDefs()) {
                    NamedColumn namedTypeColumn = namedType.column(columnDef.getName());
                    String newColumnName = namedTypeColumn.computeAlias(columnDef,remapper::nameFor);
                    columnDef.setName(newColumnName);
                }
            }

            for (CqlTable table : keyspaceDef.getTableDefs()) {

                NamedTable namedTable = namedKeyspace.table(table.getName());
                String newTableName = namedTable.computeAlias(table,remapper::nameFor);
                table.setName(newTableName);

                for (CqlColumnBase columnDef : table.getColumnDefs()) {
                    NamedColumn namedTableColumn = namedTable.column(columnDef.getName());
                    String newColumnName = namedTableColumn.computeAlias(columnDef,remapper::nameFor);
                    columnDef.setName(newColumnName);
                }

            }

        }

//        for (String keyspaceName : model.getAllKnownKeyspaceNames()) {
//            Map<String, String> labels = Map.of("type", "keyspace", "name", keyspaceName);
//            NamedKeyspace cachedKeyspace = cache.keyspace(keyspaceName);
//            cachedKeyspace.computeAlias(labels, remapper::nameFor);
////            model.renamekeyspace(keyspaceName, alias);
//        }
//
//        for (CqlTable cqlTable : model.getTableDefs()) {
//            String tablename = cqlTable.getName();
//            NamedTable cachedTable = cache.keyspace(cqlTable.getKeyspaceName()).table(tablename);
//            String alias = cachedTable.computeAlias(cqlTable, remapper::nameFor);
//            model.renameTable(cqlTable, alias);
//
//            for (CqlColumnBase coldef : cqlTable.getColumnDefs()) {
//                NamedColumn cachedColumn = cache.keyspace(cqlTable.getKeyspaceName()).table(tablename).column(coldef.getName());
//                cachedColumn.computeAlias(coldef, remapper::nameFor);
////                model.renameColumn(coldef, colalias);
//            }
//        }
//
//        for (CqlType type : model.getTypeDefs()) {
//            String typeName = type.getName();
//            NamedType cachedType = cache.keyspace(type.getKeyspace()).type(typeName);
//            cachedType.computeAlias(type, remapper::nameFor);
////            model.renameType(type.getKeyspace(), typeName, alias);
//
////            Function<String, String> colmapper = remapper.mapperForType(type, "typ");
//            Map<String, String> newdefs = new LinkedHashMap<>();
//
//            Set<String> keys = type.getFields().keySet();
//            for (String key : keys) {
//                NamedColumn cachedColdef = cache.keyspace(type.getKeyspace()).type(typeName).column(key);
//                cachedColdef.computeAlias(Map.of("type", "column", "name", key), remapper::nameFor);
////                String def = type.getFields().get(key);
////                newdefs.put(colalias, def);
////                type.setFields(newdefs);
//            }
//        }

        if (mapfile!=null) {
            cache.setPath(mapfile);
            cache.Save();
        }
        return model;

    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void applyConfig(NBConfiguration cfg) {
        String namer = cfg.get("namer");
        DataMapper<String> namerFunc = VirtData.getMapper(namer);
        this.remapper.setNamingFunction(namerFunc);
        this.mapfile = cfg.getOptional("mapfile").orElse(null);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return ConfigModel.of(CGNameObfuscator.class)
            .add(Param.defaultTo("namer", "Combinations('0-9;0-9;0-9;0-9;0-9')"))
            .add(Param.optional("mapfile", String.class))
            .asReadOnly();
    }

    @Override
    public String getName() {
        return this.name;
    }
}
