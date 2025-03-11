/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.nosqlbench.virtdata.predicates.nodewalk.repr;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.nbvectors.buildhdf5.predicates.types.ConjugateNode;
import io.nosqlbench.nbvectors.buildhdf5.predicates.types.NodeRepresenter;
import io.nosqlbench.nbvectors.buildhdf5.predicates.types.PNode;
import io.nosqlbench.nbvectors.buildhdf5.predicates.types.PredicateNode;

/**
 * Renders HDF5 predicate nodes as JSON strings.
 * This class implements NodeRepresenter to provide JSON string representations
 * of predicate nodes used in HDF5 operations.
 */
public class H5JsonNodeRenderer implements NodeRepresenter {
    /** Schema information used for rendering */
    private final String[] schema;
    /** Gson instance configured for pretty printing */
    private final static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Creates a new renderer with the specified schema.
     *
     * @param schema Array of strings representing the schema structure
     */
    public H5JsonNodeRenderer(String[] schema) {
        this.schema = schema;
    }

    /**
     * Converts a predicate node to its JSON string representation.
     *
     * @param node The predicate node to render
     * @return JSON string representation of the node
     */
    @Override
    public String apply(PNode<?> node) {
        return switch (node) {
            case ConjugateNode n -> renderConjugate(n);
            case PredicateNode p -> renderPredicate(p);
        };
    }

    /**
     * Renders a predicate node as JSON.
     *
     * @param p The predicate node to render
     * @return JSON string representation of the predicate
     */
    private String renderPredicate(PredicateNode p) {
        return gson.toJson(p);
    }

    /**
     * Renders a conjugate node as JSON.
     *
     * @param n The conjugate node to render
     * @return JSON string representation of the conjugate
     */
    private String renderConjugate(ConjugateNode n) {
        return gson.toJson(n);
    }
}
