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
package io.nosqlbench.vectordata.binding;

import io.nosqlbench.vectordata.anode.ANode;
import io.nosqlbench.vectordata.anode.ANodeException;
import io.nosqlbench.vectordata.anode.PNode;
import io.nosqlbench.vectordata.anode.PNode.Comparand;
import io.nosqlbench.vectordata.anode.Scan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/// A compiled predicate template: the shape of a filter, prepared once.
///
/// Compiled against **two** things — a sample predicate, which supplies
/// the fields and operators, and the metadata layout, which supplies
/// the types. Neither alone is enough: a predicate knows what it
/// constrains and not what type that field is. A filtered workload
/// needs a `WHERE` clause with parameters, not a rendered fragment with
/// comparands inlined; this is the parameter list.
public final class PredicateBinder {
    private final List<Condition> conditions;
    /// The shape every bound record must match, values removed.
    private final PNode shape;

    private PredicateBinder(List<Condition> conditions, PNode shape) { this.conditions = List.copyOf(conditions); this.shape = shape; }

    /// Compiles a predicate template.
    ///
    /// Only a flat conjunction compiles: a nested or disjunctive
    /// predicate has no flat parameter list, and pretending otherwise
    /// would bind values into the wrong conditions. Such a predicate is
    /// refused by name rather than partially flattened.
    public static PredicateBinder compile(PNode predicate, Layout layout) {
        List<Scan.FlatCondition> flat = Scan.flattenAnd(predicate);
        if (flat == null)
            throw BindException.record("this predicate is not a flat conjunction of named field comparisons, so it has no parameter list — bind its conditions individually, or use a template that is");
        List<Condition> conditions = new ArrayList<>(flat.size());
        for (Scan.FlatCondition c : flat) {
            int position = layout.positionOf(c.field());
            if (position < 0) throw BindException.noSuchField(c.field(), layout.names());
            // The field's type, not the comparand's.
            conditions.add(new Condition(c.field(), c.field(), c.op(), layout.types().get(position), c.comparands().size()));
        }
        return new PredicateBinder(conditions, predicate.fingerprint());
    }

    /// Renames parameters for substitution, as the metadata binder does.
    public PredicateBinder withOverrides(Map<String, String> overrides) {
        List<Condition> renamed = new ArrayList<>(conditions.size());
        for (Condition c : conditions) renamed.add(new Condition(c.field(), overrides.getOrDefault(c.field(), c.parameter()), c.op(), c.bindType(), c.arity()));
        return new PredicateBinder(renamed, shape);
    }

    /// The conditions, in order. Read once, to build the filter.
    public List<Condition> conditions() { return conditions; }

    /// The shape this binder was compiled against, in the display
    /// grammar with every comparand replaced by its type default — the
    /// identity a caller keys one binder per shape by.
    public String shape() { return shape.display(); }

    /// Receives one condition's comparands from a bound predicate.
    @FunctionalInterface
    public interface ConditionSink { void accept(Condition condition, List<Comparand> comparands); }

    /// Binds one predicate record, handing each condition's comparands
    /// to `sink` in condition order.
    ///
    /// The record is checked against the compiled shape first: a
    /// predicate of a different shape would otherwise bind values into
    /// conditions they do not belong to, which no downstream check
    /// would catch. A predicate is decoded per record — there is no
    /// raw scanner for a PNode as there is for an MNode — because a
    /// predicate is small and the shape check dominates.
    public void bindEach(byte[] record, ConditionSink sink) {
        ANode node;
        try { node = ANode.decode(record); }
        catch (ANodeException e) { throw BindException.record(e.getMessage()); }
        // The leader byte is the authority on what a record is; a
        // template cannot make an MNode into a predicate.
        if (!(node instanceof ANode.P p))
            throw BindException.record("this record is not a predicate — its dialect byte says MNode, and a form does not override what a record is");
        bindEach(p.node(), sink);
    }

    /// Binds one already-decoded predicate, for a caller that decoded
    /// the record itself — to read its fingerprint and choose the
    /// binder, say — so the node is not decoded twice. The shape check
    /// is the same.
    public void bindEach(PNode predicate, ConditionSink sink) {
        if (!predicate.isCongruent(shape))
            throw BindException.record("this predicate's shape differs from the one this binder was compiled against; its values would bind to the wrong conditions");
        List<Scan.FlatCondition> flat = Scan.flattenAnd(predicate);
        if (flat == null) throw BindException.record("predicate is not a flat conjunction");
        for (int i = 0; i < conditions.size() && i < flat.size(); i++) sink.accept(conditions.get(i), flat.get(i).comparands());
    }
}
