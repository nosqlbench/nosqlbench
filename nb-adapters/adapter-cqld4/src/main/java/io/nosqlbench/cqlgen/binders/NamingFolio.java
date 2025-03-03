/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.cqlgen.binders;

import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.cqlgen.model.CqlColumnBase;
import io.nosqlbench.cqlgen.model.CqlModel;
import io.nosqlbench.cqlgen.model.CqlTable;
import io.nosqlbench.cqlgen.core.CGElementNamer;
import io.nosqlbench.nb.api.labels.NBLabeledElement;

import java.util.*;

/**
 * The purpose of this class is to put all the logic/complexity of name condensing into one place.
 * Basically if you have identifiers that are globally unique within the active namespace,
 * <EM>WITHOUT</EM> using fully qualified names, then it is easier for users to use short names.
 * For example, if you have a column named "score" which is used as an int in one table and as
 * a double in another, then you must include the type information to provide two distinct identifiers
 * for the purpose of mapping bindings.
 *
 * This will be a pre-built inverted index of all field which need to have bindings assigned.
 * A field reference is presumed to be unique within the scope from which the traversal to
 * the working set has a single path.
 *
 * // name -> type -> table -> keyspace -> namespace
 */
public class NamingFolio {

    private final Map<String, NBLabeledElement> graph = new LinkedHashMap<>();
    private final CGElementNamer namer;
    public static final String DEFAULT_NAMER_SPEC = "[BLOCKNAME-][OPTYPE-][COLUMN]-[TYPEDEF-][TABLE][-KEYSPACE]";
    NamingStyle namingStyle = NamingStyle.SymbolicType;

    public NamingFolio(String namerspec) {
        this.namer = new CGElementNamer(
            namerspec,
            List.of(s -> s.toLowerCase().replaceAll("[^a-zA-Z0-9_-]", ""))
        );
    }

    public NamingFolio() {
        this.namer = new CGElementNamer(DEFAULT_NAMER_SPEC);
    }

    public void addFieldRef(Map<String, String> labels) {
        String name = namer.apply(labels);
        graph.put(name, NBLabeledElement.forMap(labels));
    }

    public void addFieldRef(String column, String typedef, String table, String keyspace) {
        addFieldRef(Map.of("column", column, "typedef", typedef, "table", table, "keyspace", keyspace));
    }

    /**
     * This will eventually elide extraneous fields according to knowledge of all known names
     * by name, type, table, keyspace. For now it just returns everything in fully qualified form.
     */
    public String nameFor(NBLabeledElement labeled, String... fields) {
        NBLabels labelsPlus = labeled.getLabels().andPairs(fields);
        String name = namer.apply(labelsPlus.asMap());
        return name;
    }

    public String nameFor(NBLabeledElement labeled, Map<String,String> fields) {
        NBLabels labelsPlus = labeled.getLabels().andMap(fields);
        String name = namer.apply(labelsPlus.asMap());
        return name;

    }

    public void informNamerOfAllKnownNames(CqlModel model) {
        for (CqlTable table : model.getTableDefs()) {
            for (CqlColumnBase coldef : table.getColumnDefs()) {
                addFieldRef(coldef.getLabels().asMap());
            }
        }
    }

    public Set<String> getNames() {
        return new LinkedHashSet<>(graph.keySet());
    }

}
