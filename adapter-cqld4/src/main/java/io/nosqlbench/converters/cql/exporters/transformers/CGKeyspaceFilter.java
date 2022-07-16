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

import io.nosqlbench.converters.cql.cqlast.CqlKeyspace;
import io.nosqlbench.converters.cql.cqlast.CqlModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class CGKeyspaceFilter implements CGModelTransformer, CGTransformerConfigType {

    private List<Pattern> patterns = List.of(Pattern.compile(".*"));
    private final static Logger logger = LogManager.getLogger(CGKeyspaceFilter.class);

    @Override
    public CqlModel apply(CqlModel model) {
        for (CqlKeyspace keyspace : model.getKeyspaceDefs()) {
            boolean included = false;
            for (Pattern pattern : patterns) {
                if (pattern.matcher(keyspace.getName()).matches()) {
                    included=true;
                    break;
                }
            }
            if (!included) {
                logger.info("removing keyspaces, tables and types for non-included keyspace '" + keyspace.getName() +"'");
                model.removeKeyspaceDef(keyspace.getName());
                model.removeTablesForKeyspace(keyspace.getName());
                model.removeTypesForKeyspace(keyspace.getName());
            } else {
                logger.info("including keyspace '" + keyspace.getName()+"'");
            }
        }

        return model;
    }

    @Override
    public void accept(Map<String, ?> cfgmap) {
        List<String> includes = (List<String>) cfgmap.get("include");
        this.patterns = includes.stream()
            .map(Pattern::compile)
            .toList();
    }
}
