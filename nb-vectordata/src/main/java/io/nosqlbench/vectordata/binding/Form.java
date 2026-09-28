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
import io.nosqlbench.vectordata.anode.MNode;
import io.nosqlbench.vectordata.anode.MValue;
import io.nosqlbench.vectordata.anode.Vernacular;
import io.nosqlbench.vectordata.anode.Vernaculars;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// One op-template form a facet offers: what operation its records can
/// become, which fields that binds, and what to call them.
///
/// Open by construction: keys this build does not know are kept in
/// [#extra] rather than rejected. A writer recording a form this build
/// does not implement is recording, not misbehaving.
public final class Form {
    /// The name an unnamed, implicit form goes by.
    public static final String IMPLICIT = "default";

    private final String name;
    private final String kind;
    private final String wireFormat;
    private final String operation;
    private final List<String> fields;
    private final Map<String, String> parameters;
    private final Map<String, MValue> extra;

    public Form(String name, String kind, String wireFormat, String operation, List<String> fields, Map<String, String> parameters, Map<String, MValue> extra) {
        this.name = name; this.kind = kind; this.wireFormat = wireFormat; this.operation = operation;
        this.fields = fields == null ? null : List.copyOf(fields);
        this.parameters = Collections.unmodifiableMap(new LinkedHashMap<>(parameters));
        this.extra = Collections.unmodifiableMap(new LinkedHashMap<>(extra));
    }

    /// The form a facet with no `forms` namespace offers: every field,
    /// under its own name.
    public static Form implicit() { return new Form(IMPLICIT, null, null, null, null, Map.of(), Map.of()); }

    /// Reads a form from its JSON record: `name` required; `kind`,
    /// `wire_format`, `operation` optional strings; `fields` an
    /// optional list of names; `parameters` an optional map of
    /// renames; every other key preserved as it was written.
    public static Form fromJson(String json) {
        ANode node = Vernaculars.parse(json, Vernacular.JSON);
        MNode m = ((ANode.M) node).node();
        String name = null, kind = null, wireFormat = null, operation = null;
        List<String> fields = null;
        Map<String, String> parameters = new LinkedHashMap<>();
        Map<String, MValue> extra = new LinkedHashMap<>();
        for (Map.Entry<String, MValue> e : m.fields().entrySet()) {
            switch (e.getKey()) {
                case "name" -> name = text(e);
                case "kind" -> kind = optionalText(e);
                case "wire_format" -> wireFormat = optionalText(e);
                case "operation" -> operation = optionalText(e);
                case "fields" -> {
                    if (e.getValue() instanceof MValue.Null) break;
                    if (!(e.getValue() instanceof MValue.ListValue list)) throw new ANodeException("form 'fields' must be a list of names");
                    fields = new ArrayList<>();
                    for (MValue item : list.items()) {
                        if (!(item instanceof MValue.Text t)) throw new ANodeException("form 'fields' must be a list of names");
                        fields.add(t.value());
                    }
                }
                case "parameters" -> {
                    if (e.getValue() instanceof MValue.Null) break;
                    if (!(e.getValue() instanceof MValue.MapValue map)) throw new ANodeException("form 'parameters' must be a map of names");
                    for (Map.Entry<String, MValue> p : map.node().fields().entrySet()) {
                        if (!(p.getValue() instanceof MValue.Text t)) throw new ANodeException("form 'parameters' must be a map of names");
                        parameters.put(p.getKey(), t.value());
                    }
                }
                default -> extra.put(e.getKey(), e.getValue());
            }
        }
        if (name == null) throw new ANodeException("form record has no 'name'");
        return new Form(name, kind, wireFormat, operation, fields, parameters, extra);
    }

    private static String text(Map.Entry<String, MValue> e) {
        if (e.getValue() instanceof MValue.Text t) return t.value();
        throw new ANodeException("form '" + e.getKey() + "' must be a string");
    }

    private static String optionalText(Map.Entry<String, MValue> e) { return e.getValue() instanceof MValue.Null ? null : text(e); }

    /// The form's name, as selected.
    public String name() { return name; }
    /// Which dialect of record this form is for — `metadata` or
    /// `predicate` — or `null`. Advisory: the record's leader byte
    /// remains the authority on what a record is.
    public String kind() { return kind; }
    /// The record encoding this form consumes, e.g. `mnode:v1`, or `null`.
    public String wireFormat() { return wireFormat; }
    /// What operation the bound record becomes, e.g. `insert`, or `null`.
    public String operation() { return operation; }
    /// Fields this form binds, in bind order; `null` means all of them,
    /// in the facet's own order.
    public List<String> fields() { return fields; }
    /// Parameter renames, keyed by field name.
    public Map<String, String> parameters() { return parameters; }
    /// Anything this build does not recognise, preserved as written.
    public Map<String, MValue> extra() { return extra; }

    /// Compiles this form against a layout.
    public Binder binder(Layout layout) {
        Binder binder = fields == null ? Binder.all(layout) : Binder.select(layout, fields);
        return binder.withOverrides(parameters);
    }

    @Override public String toString() { return "Form[" + name + (operation == null ? "" : " " + operation) + (fields == null ? "" : " " + fields) + "]"; }
}
