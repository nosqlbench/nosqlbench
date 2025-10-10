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

import io.nosqlbench.vectordata.spec.predicates.ConjugateNode;
import io.nosqlbench.vectordata.spec.predicates.PNode;
import io.nosqlbench.vectordata.spec.predicates.PredicateNode;

import java.util.function.Function;

/**
 * Renders predicate nodes into CQL-compatible string representations.
 * This class implements {@link Function} to convert different types of nodes
 * into their corresponding CQL syntax.
 */
public class CqlNodeRenderer implements Function<PNode<?>, String> {
    /** Schema field names used for rendering */
    private final String[] schema;
    /** Constant for space character used in string building */
    public static final String SPACE = " ";

    /**
     * Creates a new CQL node renderer with the specified schema.
     * @param schema Array of field names used for rendering predicates
     */
    public CqlNodeRenderer(String[] schema) {
        this.schema = schema;
    }

    /**
     * Converts a predicate node into its CQL string representation.
     * @param node The node to convert
     * @return CQL string representation of the node
     */
    @Override
    public String apply(PNode<?> node) {
        return switch (node) {
            case ConjugateNode n -> reprConjugate(n);
            case PredicateNode p -> reprPredicate(p);
            default -> throw new IllegalArgumentException("Unsupported node type: " + node.getClass().getName());
        };
    }

    /**
     * Renders a predicate node into CQL syntax.
     * @param p The predicate node to render
     * @return CQL representation like "field_name operator value"
     */
    private String reprPredicate(PredicateNode p) {
        StringBuilder sb = new StringBuilder();
        sb.append(schema[p.field()]);
        if (isChar(p.op().symbol())) {
            sb.append(SPACE);
        }
        sb.append(p.op().symbol());
        if (p.v().length>1) {
            sb.append("(");
            String delim="";
            for (long v : p.v()) {
                sb.append(delim);
                sb.append(v);
                delim=",";
            }
            sb.append(")");
        } else {
            sb.append(p.v()[0]);
        }
        return sb.toString();
    }

    /**
     * Checks if the first character of a symbol is alphabetic.
     * @param symbol The symbol to check
     * @return true if first character is a letter, false otherwise
     */
    private boolean isChar(String symbol) {
        char c = symbol.charAt(0);
        return (c >= 'A' && c <= 'Z') || (c>='a' && c<='z');
    }

    /**
     * Renders a conjugate node (AND/OR combinations) into CQL syntax.
     * @param n The conjugate node to render
     * @return CQL representation of the conjugated predicates
     */
    private String reprConjugate(ConjugateNode n) {
        return switch (n.type()) {
            case AND,OR -> concatenate(n.type().name(),n.values());
            case PRED -> throw new RuntimeException("impossible unless broken code");
        };
    }

    /**
     * Concatenates multiple nodes with the specified operator.
     * @param name The operator name (AND/OR)
     * @param values The nodes to concatenate
     * @return Space-separated string of nodes joined by the operator
     */
    private String concatenate(String name, PNode<?>[] values) {
        StringBuilder sb = new StringBuilder();

        for (PNode<?> value : values) {
            String nodeRep = apply(value);
            if (!sb.isEmpty()) {
                sb.append(SPACE).append(name).append(SPACE);
            }
            sb.append(nodeRep);
        }
        return sb.toString();
    }

}
