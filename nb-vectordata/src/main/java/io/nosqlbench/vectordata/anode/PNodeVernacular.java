/*
 * Copyright (c) 2026 The NoSQLBench Authors.
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
package io.nosqlbench.vectordata.anode;

import io.nosqlbench.vectordata.anode.PNode.Comparand;
import io.nosqlbench.vectordata.anode.PNode.FieldRef;
import io.nosqlbench.vectordata.anode.PNode.OpType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

/// SQL, CQL, and CDDL renderings of a [PNode]: a `WHERE` expression in
/// the two query languages, and a CDDL group describing the predicate's
/// structure.
public final class PNodeVernacular {
    private PNodeVernacular() { }

    /// A SQL `WHERE` expression; `MATCHES` renders as `~`.
    public static String toSql(PNode node) { return expr(node, Dialect.SQL); }

    /// A CQL `WHERE` expression: `!=` becomes a `<`/`>` disjunction and
    /// `MATCHES` becomes `LIKE`, which is what CQL offers.
    public static String toCql(PNode node) { return expr(node, Dialect.CQL); }

    /// A CDDL group: `{ field: "age", op: "gt", value: 18 }`, conjugates
    /// as `{ and: [ … ] }`.
    public static String toCddl(PNode node) {
        if (node instanceof PNode.Predicate p) {
            String field = field(p.field());
            String op = p.op().name().toLowerCase(Locale.ROOT);
            if (p.comparands().size() == 1)
                return "{ field: \"" + field + "\", op: \"" + op + "\", value: " + cddlComparand(p.comparands().get(0)) + " }";
            StringJoiner vals = new StringJoiner(", ");
            for (Comparand c : p.comparands()) vals.add(cddlComparand(c));
            return "{ field: \"" + field + "\", op: \"" + op + "\", values: [" + vals + "] }";
        }
        PNode.Conjugate c = (PNode.Conjugate) node;
        List<String> children = new ArrayList<>();
        for (PNode child : c.children()) children.add(toCddl(child));
        return "{ " + c.type().name().toLowerCase(Locale.ROOT) + ": [" + String.join(", ", children) + "] }";
    }

    private enum Dialect { SQL, CQL }

    private static String expr(PNode node, Dialect dialect) {
        if (node instanceof PNode.Predicate p) {
            String field = field(p.field());
            List<Comparand> cs = p.comparands();
            return switch (p.op()) {
                case IN -> field + " IN (" + join(cs, dialect) + ")";
                case MATCHES -> field + (dialect == Dialect.SQL ? " ~ " : " LIKE ") + (cs.isEmpty() ? "''" : comparand(cs.get(0), dialect));
                case NE -> dialect == Dialect.CQL && cs.size() == 1
                    ? "(" + field + " < " + comparand(cs.get(0), dialect) + " OR " + field + " > " + comparand(cs.get(0), dialect) + ")"
                    : single(field, p.op(), cs, dialect);
                default -> single(field, p.op(), cs, dialect);
            };
        }
        PNode.Conjugate c = (PNode.Conjugate) node;
        List<String> parts = new ArrayList<>();
        for (PNode child : c.children()) parts.add(expr(child, dialect));
        return parts.size() == 1 ? parts.get(0) : "(" + String.join(" " + c.type().name() + " ", parts) + ")";
    }

    private static String single(String field, OpType op, List<Comparand> cs, Dialect dialect) {
        if (cs.size() == 1) return field + " " + op.symbol() + " " + comparand(cs.get(0), dialect);
        return field + " " + op.symbol() + " (" + join(cs, dialect) + ")";
    }

    private static String join(List<Comparand> cs, Dialect dialect) {
        StringJoiner vals = new StringJoiner(", ");
        for (Comparand c : cs) vals.add(comparand(c, dialect));
        return vals.toString();
    }

    static String field(FieldRef field) { return field instanceof FieldRef.Index i ? "field_" + i.value() : ((FieldRef.Named) field).name(); }

    private static String comparand(Comparand c, Dialect dialect) {
        if (c instanceof Comparand.Int v) return Long.toString(v.value());
        if (c instanceof Comparand.Float v) return Numbers.withPoint(Numbers.f64(v.value()));
        if (c instanceof Comparand.Text v) return "'" + v.value().replace("'", "''") + "'";
        if (c instanceof Comparand.Bool v) return dialect == Dialect.SQL ? (v.value() ? "TRUE" : "FALSE") : (v.value() ? "true" : "false");
        if (c instanceof Comparand.Bytes v) return dialect == Dialect.SQL ? "X'" + Numbers.hex(v.value()) + "'" : "0x" + Numbers.hex(v.value());
        return dialect == Dialect.SQL ? "NULL" : "null";
    }

    private static String cddlComparand(Comparand c) {
        if (c instanceof Comparand.Int v) return Long.toString(v.value());
        if (c instanceof Comparand.Float v) return Numbers.withPoint(Numbers.f64(v.value()));
        if (c instanceof Comparand.Text v) return "\"" + v.value() + "\"";
        if (c instanceof Comparand.Bool v) return v.value() ? "true" : "false";
        if (c instanceof Comparand.Bytes v) return "h'" + Numbers.hex(v.value()) + "'";
        return "null";
    }
}
