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

import io.nosqlbench.vectordata.anode.Field;
import io.nosqlbench.vectordata.anode.Scan;
import io.nosqlbench.vectordata.anode.ScanException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/// A compiled binding: which positions to read, and what to call them.
///
/// Built once from a [Layout], then used for every record. The
/// per-record path never looks at a field name — that work is done
/// here, which is what keeps binding free of per-record name
/// resolution. Binding is not rendering: the vernaculars inline
/// literals into text for a human, and this hands named, typed values
/// to a driver with the statement prepared once.
public final class Binder {
    /// Positions to bind, in parameter order.
    private final int[] positions;
    /// Parameter names, in the same order.
    private final List<String> parameters;
    /// What each parameter binds as.
    private final List<BindType> types;

    private Binder(int[] positions, List<String> parameters, List<BindType> types) {
        this.positions = positions; this.parameters = List.copyOf(parameters); this.types = List.copyOf(types);
    }

    /// Binds every field of the layout, under its own name.
    public static Binder all(Layout layout) {
        int[] positions = new int[layout.names().size()];
        for (int i = 0; i < positions.length; i++) positions[i] = i;
        return new Binder(positions, layout.names(), layout.types());
    }

    /// Binds the named fields, in the order given.
    ///
    /// A field the facet does not have is refused here, at compile
    /// time, naming what the facet does have — rather than binding a
    /// missing value on the first cycle.
    public static Binder select(Layout layout, List<String> fields) {
        int[] positions = new int[fields.size()];
        List<String> parameters = new ArrayList<>(fields.size());
        List<BindType> types = new ArrayList<>(fields.size());
        for (int i = 0; i < fields.size(); i++) {
            int at = layout.positionOf(fields.get(i));
            if (at < 0) throw BindException.noSuchField(fields.get(i), layout.names());
            positions[i] = at;
            parameters.add(layout.names().get(at));
            types.add(layout.types().get(at));
        }
        return new Binder(positions, parameters, types);
    }

    /// [#select] with the fields as arguments.
    public static Binder select(Layout layout, String... fields) { return select(layout, Arrays.asList(fields)); }

    /// Renames parameters for substitution.
    ///
    /// The metadata name is the parameter name unless a runtime says
    /// otherwise. An override is applied here, against resolved
    /// positions, so it costs nothing per record and cannot reach the
    /// data — the field keeps its name in the facet.
    public Binder withOverrides(Map<String, String> overrides) {
        List<String> renamed = new ArrayList<>(parameters.size());
        for (String p : parameters) renamed.add(overrides.getOrDefault(p, p));
        return new Binder(positions, renamed, types);
    }

    /// Parameter names, in bind order. Read once, to prepare a statement.
    public List<String> parameters() { return parameters; }
    /// What each parameter binds as, in bind order.
    public List<BindType> types() { return types; }

    /// Receives one bound field: the parameter slot it fills, in bind
    /// order, and the field viewed in place.
    @FunctionalInterface
    public interface FieldSink { void accept(int slot, Field field); }

    /// Binds one record, handing each parameter to `sink` in the order
    /// the walk meets it.
    ///
    /// The form for a cycle loop: nothing is copied and no field name
    /// is looked at. Each field is a small view over the record's own
    /// bytes, valid for as long as the record is — handed over rather
    /// than kept, so nothing outlives the record it came from.
    public void bindEach(byte[] record, FieldSink sink) {
        int seen = 0;
        try {
            for (Field field : Scan.fields(record)) {
                for (int slot = 0; slot < positions.length; slot++) {
                    if (positions[slot] == field.index()) { sink.accept(slot, field); seen++; }
                }
            }
        } catch (ScanException e) {
            throw BindException.record(e.getMessage());
        }
        if (seen < positions.length)
            throw BindException.record("record has " + seen + " of the " + positions.length + " bound fields — its layout differs from the one this binder was compiled against");
    }

    /// Binds one record, returning its bound fields in bind order.
    ///
    /// For a caller that wants random access to the bound values. The
    /// list is one walk's worth of views over this record's bytes and
    /// is scoped to it; see [#bindEach] for the loop form.
    public List<Field> bind(byte[] record) {
        // One walk, picking out the wanted positions, so a template may
        // name fields in any order without costing a second pass.
        Field[] found = new Field[positions.length];
        try {
            for (Field field : Scan.fields(record)) {
                for (int slot = 0; slot < positions.length; slot++) if (positions[slot] == field.index()) found[slot] = field;
            }
        } catch (ScanException e) {
            throw BindException.record(e.getMessage());
        }
        for (int slot = 0; slot < found.length; slot++)
            if (found[slot] == null) throw BindException.noSuchField(parameters.get(slot), List.of());
        return List.of(found);
    }
}
