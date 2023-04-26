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

import io.nosqlbench.cqlgen.model.CqlColumnBase;
import io.nosqlbench.cqlgen.model.CqlModel;
import io.nosqlbench.cqlgen.model.CqlTable;
import io.nosqlbench.cqlgen.core.CGElementNamer;
import io.nosqlbench.api.config.NBLabeledElement;

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

    public NamingFolio(final String namerspec) {
        namer = new CGElementNamer(
            namerspec,
            List.of(s -> s.toLowerCase().replaceAll("[^a-zA-Z0-9_-]", ""))
        );
    }

    public NamingFolio() {
        namer = new CGElementNamer(NamingFolio.DEFAULT_NAMER_SPEC);
    }

    public void addFieldRef(final Map<String, String> labels) {
        final String name = this.namer.apply(labels);
        this.graph.put(name, NBLabeledElement.forMap(labels));
    }

    public void addFieldRef(final String column, final String typedef, final String table, final String keyspace) {
        this.addFieldRef(Map.of("column", column, "typedef", typedef, "table", table, "keyspace", keyspace));
    }

    /**
     * This will eventually elide extraneous fields according to knowledge of all known names
     * by name, type, table, keyspace. For now it just returns everything in fully qualified form.
     */
    public String nameFor(final NBLabeledElement labeled, final String... fields) {
        final Map<String, String> labelsPlus = labeled.getLabelsAnd(fields);
        final String name = this.namer.apply(labelsPlus);
        return name;
    }

    public String nameFor(final NBLabeledElement labeled, final Map<String,String> fields) {
        final Map<String, String> labelsPlus = labeled.getLabelsAnd(fields);
        final String name = this.namer.apply(labelsPlus);
        return name;

    }

    public void informNamerOfAllKnownNames(final CqlModel model) {
        for (final CqlTable table : model.getTableDefs())
            for (final CqlColumnBase coldef : table.getColumnDefs()) this.addFieldRef(coldef.getLabels());
    }

    public Set<String> getNames() {
        return new LinkedHashSet<>(this.graph.keySet());
    }

}
