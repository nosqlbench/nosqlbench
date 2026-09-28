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
import io.nosqlbench.vectordata.anode.TypeTag;
import io.nosqlbench.vectordata.records.RecordException;
import io.nosqlbench.vectordata.records.RecordFacet;

import java.util.ArrayList;
import java.util.List;

/// A facet's field layout: names in wire order, with the type each
/// binds as.
///
/// Learned **once**. The names are the metadata's own, in the order the
/// records carry them, and that order is the position a binder
/// addresses fields by for the rest of the run.
public final class Layout {
    private final List<String> names;
    private final List<BindType> types;

    private Layout(List<String> names, List<BindType> types) { this.names = List.copyOf(names); this.types = List.copyOf(types); }

    /// Learns a facet's layout from its first record.
    ///
    /// A facet publishing a schema namespace should be preferred over
    /// this, because a first record that omits an optional field would
    /// otherwise define the layout for every record after it. That
    /// preference is the caller's to express until a schema-namespace
    /// reader exists; this is the discovery path.
    public static Layout discover(RecordFacet facet) {
        try {
            if (facet.count() == 0) throw BindException.noLayout(facet.name());
            return discover(facet.recordBytes(0));
        } catch (RecordException e) {
            throw BindException.facet(e.getMessage());
        }
    }

    /// Learns a layout from one record's bytes — the same walk, for a
    /// caller holding a sample record rather than a facet.
    public static Layout discover(byte[] record) {
        List<String> names = new ArrayList<>();
        List<BindType> types = new ArrayList<>();
        try {
            for (Field field : Scan.fields(record)) {
                names.add(field.name());
                TypeTag tag = field.typeTag();
                types.add(tag == null ? BindType.BLOB : BindType.ofTag(tag));
            }
        } catch (ScanException e) {
            throw BindException.record(e.getMessage());
        }
        return new Layout(names, types);
    }

    /// Field names, in wire order.
    public List<String> names() { return names; }
    /// What each field binds as, by position.
    public List<BindType> types() { return types; }
    /// The position of a named field, or `-1`.
    public int positionOf(String field) { return names.indexOf(field); }
}
