package io.nosqlbench.virtdata.lib.vectors.vectordata;

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


import io.nosqlbench.vectordata.anode.MNode;
import io.nosqlbench.vectordata.anode.MValue;
import io.nosqlbench.vectordata.binding.BindType;
import io.nosqlbench.vectordata.binding.Layout;

import java.util.ArrayList;
import java.util.List;

/// CQL column definitions derived from a record layout — the schema a
/// table needs to hold what [RecordFields] binds, typed from the bind
/// types rather than the schema vernacular so that what the columns
/// accept is what the projections produce: a `Half` is a `float`, a
/// `Millis` a `timestamp`, a `DateTime` the text it is on the wire. A
/// container's element type comes from a sample record when the layout
/// alone cannot say, and is `text` when no sample can either.
public final class CqlColumns {
    private CqlColumns() { }

    /// `name type, name type, …` for every field of the layout, in
    /// wire order, element types read from `sample` where a tag alone
    /// leaves them open. `sample` may be `null`.
    public static String columns(Layout layout, MNode sample) {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < layout.names().size(); i++) {
            String name = layout.names().get(i);
            MValue value = sample == null ? null : sample.get(name);
            out.add(name + " " + cqlType(layout.types().get(i), value));
        }
        return String.join(", ", out);
    }

    /// The CQL type a bind type maps to, with `sample` resolving a
    /// container's element types when it holds any.
    public static String cqlType(BindType type, MValue sample) {
        return switch (type.kind()) {
            case TEXT, TIMESTAMP_TEXT, NULL -> "text";
            case ASCII -> "ascii";
            case BOOL -> "boolean";
            case BLOB, ULID -> "blob";
            case INT16 -> "smallint";
            case INT32 -> "int";
            case INT64 -> "bigint";
            case FLOAT16, FLOAT32 -> "float";
            case FLOAT64 -> "double";
            case DECIMAL -> "decimal";
            case VARINT -> "varint";
            case DATE -> "date";
            case TIME -> "time";
            case TIMESTAMP_MILLIS, TIMESTAMP_NANOS -> "timestamp";
            case TIME_UUID -> "timeuuid";
            case UUID -> "uuid";
            case LIST -> "list<" + element(type.element(), firstItem(sample)) + ">";
            case SET -> "set<" + element(type.element(), firstItem(sample)) + ">";
            case MAP -> {
                MValue key = null, value = null;
                if (sample instanceof MValue.MapValue m && !m.node().fields().isEmpty()) { key = new MValue.Text(""); value = m.node().fields().values().iterator().next(); }
                if (sample instanceof MValue.TypedMap t && !t.entries().isEmpty()) { key = t.entries().get(0).key(); value = t.entries().get(0).value(); }
                yield "map<" + element(type.key(), key) + ", " + element(type.value(), value) + ">";
            }
        };
    }

    private static String element(BindType declared, MValue sample) {
        if (declared != null) return cqlType(declared, sample);
        if (sample != null) return cqlType(BindType.ofTag(sample.tag()), sample);
        return "text";
    }

    private static MValue firstItem(MValue sample) {
        if (sample instanceof MValue.ListValue l && !l.items().isEmpty()) return l.items().get(0);
        if (sample instanceof MValue.SetValue s && !s.items().isEmpty()) return s.items().get(0);
        if (sample instanceof MValue.ArrayValue a && !a.items().isEmpty()) return a.items().get(0);
        return null;
    }
}
