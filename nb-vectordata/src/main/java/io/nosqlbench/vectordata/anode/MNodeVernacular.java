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

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

/// SQL, CQL, and CDDL renderings of an [MNode]: its values as an
/// insert tuple, its tags as column definitions, its shape as a CDDL
/// group. Used for troubleshooting, visualization, and schema
/// documentation, in the native syntax of each language.
public final class MNodeVernacular {
    private MNodeVernacular() { }

    // -- SQL --

    /// `(v1, v2, …)` — an `INSERT … VALUES` tuple.
    public static String toSql(MNode node) { return tuple(node, MNodeVernacular::sqlValue); }

    /// `(\n  name TYPE,\n  …\n)` — `CREATE TABLE` column definitions.
    public static String toSqlSchema(MNode node) { return schema(node, MNodeVernacular::sqlType); }

    static String sqlValue(MValue value) {
        String text = MValues.text(value);
        List<MValue> items = MValues.items(value);
        byte[] id = MValues.identifier(value);
        return switch (value.tag()) {
            case TEXT, ENUM_STR, ASCII -> "'" + text.replace("'", "''") + "'";
            case INT, MILLIS, ENUM_ORD, INT32, SHORT, HALF -> Long.toString(MValues.integer(value));
            case FLOAT -> Numbers.f64(((MValue.Float) value).value());
            case FLOAT32 -> Numbers.f32(((MValue.Float32) value).value());
            case BOOL -> ((MValue.Bool) value).value() ? "TRUE" : "FALSE";
            case BYTES -> "X'" + Numbers.hex(((MValue.Bytes) value).value()) + "'";
            case NULL -> "NULL";
            case NANOS -> { MValue.Nanos n = (MValue.Nanos) value; yield "TIMESTAMP '" + n.epochSeconds() + "." + n.nanoAdjust() + "'"; }
            case DATE -> "DATE '" + text + "'";
            case TIME -> "TIME '" + text + "'";
            case DATETIME -> "TIMESTAMP '" + text + "'";
            case UUID_V1, UUID_V7 -> "'" + Numbers.uuid(id) + "'";
            case ULID -> "'" + Numbers.hex(id) + "'";
            case LIST, SET, ARRAY -> joined(items, MNodeVernacular::sqlValue, "ARRAY[", "]");
            case MAP -> toSql(((MValue.MapValue) value).node());
            case TYPED_MAP -> {
                StringJoiner inner = new StringJoiner(", ", "MAP(", ")");
                for (MValue.TypedMap.Entry e : ((MValue.TypedMap) value).entries()) inner.add(sqlValue(e.key()) + " => " + sqlValue(e.value()));
                yield inner.toString();
            }
            case TEXT_VALIDATED, DECIMAL, VARINT -> throw new ANodeException("no value carries tag " + value.tag());
        };
    }

    static String sqlType(TypeTag tag) {
        return switch (tag) {
            case TEXT, TEXT_VALIDATED, ENUM_STR -> "TEXT";
            case ASCII -> "VARCHAR";
            case INT, MILLIS -> "BIGINT";
            case INT32, ENUM_ORD -> "INT";
            case SHORT -> "SMALLINT";
            case FLOAT -> "DOUBLE PRECISION";
            case FLOAT32 -> "REAL";
            case HALF -> "SMALLINT";
            case BOOL -> "BOOLEAN";
            case BYTES, DECIMAL, VARINT -> "BLOB";
            case NULL -> "TEXT";
            case DATE -> "DATE";
            case TIME -> "TIME";
            case DATETIME, NANOS -> "TIMESTAMP";
            case UUID_V1, UUID_V7 -> "UUID";
            case ULID -> "CHAR(26)";
            case LIST, ARRAY, SET -> "TEXT[]";
            case MAP, TYPED_MAP -> "JSONB";
        };
    }

    // -- CQL --

    /// `(v1, v2, …)` — a CQL `VALUES` tuple.
    public static String toCql(MNode node) { return tuple(node, MNodeVernacular::cqlValue); }

    /// CQL column definitions.
    public static String toCqlSchema(MNode node) { return schema(node, MNodeVernacular::cqlType); }

    static String cqlValue(MValue value) {
        String text = MValues.text(value);
        List<MValue> items = MValues.items(value);
        byte[] id = MValues.identifier(value);
        return switch (value.tag()) {
            case TEXT, ENUM_STR, ASCII -> "'" + text.replace("'", "''") + "'";
            case INT, MILLIS, ENUM_ORD, INT32, SHORT, HALF -> Long.toString(MValues.integer(value));
            case FLOAT -> Numbers.f64(((MValue.Float) value).value());
            case FLOAT32 -> Numbers.f32(((MValue.Float32) value).value());
            case BOOL -> ((MValue.Bool) value).value() ? "true" : "false";
            case BYTES -> "0x" + Numbers.hex(((MValue.Bytes) value).value());
            case NULL -> "null";
            case NANOS -> { MValue.Nanos n = (MValue.Nanos) value; yield "'" + n.epochSeconds() + "." + n.nanoAdjust() + "'"; }
            case DATE, TIME, DATETIME -> "'" + text + "'";
            case UUID_V1, UUID_V7 -> Numbers.uuid(id);
            case ULID -> "0x" + Numbers.hex(id);
            case LIST, ARRAY -> joined(items, MNodeVernacular::cqlValue, "[", "]");
            case SET -> joined(items, MNodeVernacular::cqlValue, "{", "}");
            case MAP -> {
                StringJoiner inner = new StringJoiner(", ", "{", "}");
                ((MValue.MapValue) value).node().fields().forEach((k, v) -> inner.add("'" + k + "': " + cqlValue(v)));
                yield inner.toString();
            }
            case TYPED_MAP -> {
                StringJoiner inner = new StringJoiner(", ", "{", "}");
                for (MValue.TypedMap.Entry e : ((MValue.TypedMap) value).entries()) inner.add(cqlValue(e.key()) + ": " + cqlValue(e.value()));
                yield inner.toString();
            }
            case TEXT_VALIDATED, DECIMAL, VARINT -> throw new ANodeException("no value carries tag " + value.tag());
        };
    }

    static String cqlType(TypeTag tag) {
        return switch (tag) {
            case TEXT, TEXT_VALIDATED, ENUM_STR -> "text";
            case ASCII -> "ascii";
            case INT, MILLIS -> "bigint";
            case INT32, ENUM_ORD -> "int";
            case SHORT -> "smallint";
            case FLOAT -> "double";
            case FLOAT32 -> "float";
            case HALF -> "smallint";
            case BOOL -> "boolean";
            case BYTES -> "blob";
            case DECIMAL -> "decimal";
            case VARINT -> "varint";
            case NULL -> "text";
            case DATE -> "date";
            case TIME -> "time";
            case DATETIME, NANOS -> "timestamp";
            case UUID_V1 -> "timeuuid";
            case UUID_V7 -> "uuid";
            case ULID -> "blob";
            case LIST, ARRAY -> "list<text>";
            case SET -> "set<text>";
            case MAP, TYPED_MAP -> "map<text, text>";
        };
    }

    // -- CDDL --

    /// A CDDL (RFC 8610) group describing the record's field structure:
    /// `{\n  name : tstr,\n  …\n}`.
    public static String toCddl(MNode node) {
        List<String> lines = new ArrayList<>();
        node.fields().forEach((name, value) -> lines.add("  " + cddlKey(name) + " : " + cddlType(value.tag())));
        return "{\n" + String.join(",\n", lines) + "\n}";
    }

    /// A value as a CDDL literal.
    public static String toCddlValue(MValue value) {
        String text = MValues.text(value);
        List<MValue> items = MValues.items(value);
        byte[] id = MValues.identifier(value);
        return switch (value.tag()) {
            case TEXT, ENUM_STR, ASCII, DATE, TIME, DATETIME -> "\"" + text + "\"";
            case INT, MILLIS, ENUM_ORD, INT32, SHORT, HALF -> Long.toString(MValues.integer(value));
            case FLOAT -> Numbers.f64(((MValue.Float) value).value());
            case FLOAT32 -> Numbers.f32(((MValue.Float32) value).value());
            case BOOL -> ((MValue.Bool) value).value() ? "true" : "false";
            case BYTES -> "h'" + Numbers.hex(((MValue.Bytes) value).value()) + "'";
            case NULL -> "null";
            case NANOS -> { MValue.Nanos n = (MValue.Nanos) value; yield n.epochSeconds() + "." + n.nanoAdjust(); }
            case UUID_V1, UUID_V7 -> "\"" + Numbers.uuid(id) + "\"";
            case ULID -> "h'" + Numbers.hex(id) + "'";
            case LIST, SET, ARRAY -> joined(items, MNodeVernacular::toCddlValue, "[", "]");
            case MAP -> toCddl(((MValue.MapValue) value).node());
            case TYPED_MAP -> {
                StringJoiner inner = new StringJoiner(", ", "{", "}");
                for (MValue.TypedMap.Entry e : ((MValue.TypedMap) value).entries()) inner.add(toCddlValue(e.key()) + " => " + toCddlValue(e.value()));
                yield inner.toString();
            }
            case TEXT_VALIDATED, DECIMAL, VARINT -> throw new ANodeException("no value carries tag " + value.tag());
        };
    }

    /// A key is bare when it is an identifier (`[a-zA-Z_][a-zA-Z0-9_-]*`).
    static String cddlKey(String name) {
        boolean bare = !name.isEmpty() && (Character.isLetter(name.charAt(0)) || name.charAt(0) == '_');
        for (int i = 0; bare && i < name.length(); i++) {
            char c = name.charAt(i);
            bare = Character.isLetterOrDigit(c) || c == '_' || c == '-';
        }
        return bare ? name : "\"" + name + "\"";
    }

    static String cddlType(TypeTag tag) {
        return switch (tag) {
            case TEXT, TEXT_VALIDATED, ENUM_STR, ASCII -> "tstr";
            case INT, MILLIS, INT32, ENUM_ORD, SHORT -> "int";
            case FLOAT, FLOAT32 -> "float";
            case HALF -> "float16";
            case BOOL -> "bool";
            case BYTES, DECIMAL, VARINT -> "bstr";
            case NULL -> "null";
            case DATE, TIME, DATETIME -> "tstr";
            case NANOS -> "int";
            case UUID_V1, UUID_V7 -> "tstr";
            case ULID -> "bstr";
            case LIST, ARRAY, SET -> "[* any]";
            case MAP, TYPED_MAP -> "{* any => any}";
        };
    }

    // -- shared --

    private static String tuple(MNode node, Function<MValue, String> render) {
        StringJoiner parts = new StringJoiner(", ", "(", ")");
        for (MValue value : node.fields().values()) parts.add(render.apply(value));
        return parts.toString();
    }

    private static String schema(MNode node, Function<TypeTag, String> type) {
        List<String> cols = new ArrayList<>();
        node.fields().forEach((name, value) -> cols.add("  " + name + " " + type.apply(value.tag())));
        return "(\n" + String.join(",\n", cols) + "\n)";
    }

    private static String joined(List<MValue> items, Function<MValue, String> render, String open, String close) {
        StringJoiner inner = new StringJoiner(", ", open, close);
        for (MValue item : items) inner.add(render.apply(item));
        return inner.toString();
    }
}
