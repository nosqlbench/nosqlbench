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

/**
 * @deprecated Superseded by direct rendering from AST in generator
 */
public class CGIfNotExistsInjector implements CGModelTransformer {

    @Override
    public CqlModel apply(CqlModel model) {
//        for (CqlKeyspace keyspace : model.getKeyspaceDefs()) {
//            keyspace.setRefDdl(keyspace.getRefddl().replaceAll(
//                "(?m)(?s)(?i)(\\s*CREATE (TABLE|KEYSPACE|TYPE) +)(?!IF NOT EXISTS)",
//                "$1IF NOT EXISTS "
//            ));
//        }
//        for (CqlTable table : model.getTableDefs()) {
//            String refddl = table.getRefDdl();
//            String replaced = refddl.replaceAll(
//                "(?m)(?s)(?i)(\\s*CREATE (TABLE|KEYSPACE|TYPE) +)(?!IF NOT EXISTS)",
//                "$1IF NOT EXISTS "
//            );
//
//            table.setRefDdl(replaced);
//        }
//        for (CqlType type : model.getTypes()) {
//            type.setRefddl(type.getRefDdl().replaceAll(
//                "(?m)(?s)(?i)(\\s*CREATE (TABLE|KEYSPACE|TYPE) +)(?!IF NOT EXISTS)",
//                "$1IF NOT EXISTS "
//            ));
//
//        }
        return model;
    }
}
