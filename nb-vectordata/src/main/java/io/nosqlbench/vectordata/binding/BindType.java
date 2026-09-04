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

import io.nosqlbench.vectordata.anode.TypeTag;

import java.util.Locale;
import java.util.Objects;

/// The type a parameter binds as.
///
/// **Distinct from the rendering mapping**, deliberately. The schema
/// vernaculars answer "what column would hold this" and are correct as
/// DDL; they map `Half` to `smallint`, give `Null` a type of `text`,
/// and collapse container element types. Binding a value through those
/// answers would send a float's bit pattern as an integer and every
/// collection's elements as strings.
///
/// Dialect-neutral: this names what the value *is*, and an adapter maps
/// it to a driver's type. The mapping from [TypeTag] in [#ofTag] is an
/// exhaustive switch with no default arm, so a new tag fails to compile
/// until it is handled there.
public final class BindType {
    /// The kinds a value binds as. Containers carry their element
    /// types on the [BindType], when known.
    public enum Kind {
        TEXT, ASCII, BOOL, BLOB, INT16, INT32, INT64, FLOAT16, FLOAT32, FLOAT64, DECIMAL, VARINT,
        DATE, TIME, TIMESTAMP_MILLIS, TIMESTAMP_NANOS, TIMESTAMP_TEXT, TIME_UUID, UUID, ULID, NULL,
        LIST, SET, MAP
    }

    public static final BindType TEXT = new BindType(Kind.TEXT, null, null, null);
    public static final BindType ASCII = new BindType(Kind.ASCII, null, null, null);
    public static final BindType BOOL = new BindType(Kind.BOOL, null, null, null);
    public static final BindType BLOB = new BindType(Kind.BLOB, null, null, null);
    public static final BindType INT16 = new BindType(Kind.INT16, null, null, null);
    public static final BindType INT32 = new BindType(Kind.INT32, null, null, null);
    public static final BindType INT64 = new BindType(Kind.INT64, null, null, null);
    public static final BindType FLOAT16 = new BindType(Kind.FLOAT16, null, null, null);
    public static final BindType FLOAT32 = new BindType(Kind.FLOAT32, null, null, null);
    public static final BindType FLOAT64 = new BindType(Kind.FLOAT64, null, null, null);
    public static final BindType DECIMAL = new BindType(Kind.DECIMAL, null, null, null);
    public static final BindType VARINT = new BindType(Kind.VARINT, null, null, null);
    public static final BindType DATE = new BindType(Kind.DATE, null, null, null);
    public static final BindType TIME = new BindType(Kind.TIME, null, null, null);
    /// An instant, in milliseconds.
    public static final BindType TIMESTAMP_MILLIS = new BindType(Kind.TIMESTAMP_MILLIS, null, null, null);
    /// An instant, in seconds plus a nanosecond adjustment.
    public static final BindType TIMESTAMP_NANOS = new BindType(Kind.TIMESTAMP_NANOS, null, null, null);
    /// An instant carried as text. Distinct from the numeric timestamps
    /// because that is what it is on the wire, and a parameter typed as
    /// a number would be bound from a string.
    public static final BindType TIMESTAMP_TEXT = new BindType(Kind.TIMESTAMP_TEXT, null, null, null);
    /// A UUID whose ordering carries a timestamp.
    public static final BindType TIME_UUID = new BindType(Kind.TIME_UUID, null, null, null);
    public static final BindType UUID = new BindType(Kind.UUID, null, null, null);
    public static final BindType ULID = new BindType(Kind.ULID, null, null, null);
    /// Absent. Carries no type of its own — the parameter's type comes
    /// from the schema, not from the null.
    public static final BindType NULL = new BindType(Kind.NULL, null, null, null);

    private final Kind kind;
    private final BindType element;
    private final BindType key;
    private final BindType value;

    private BindType(Kind kind, BindType element, BindType key, BindType value) { this.kind = kind; this.element = element; this.key = key; this.value = value; }

    /// An ordered collection; `element` is `null` when the tag alone
    /// does not determine it — only a value knows what a list holds.
    public static BindType list(BindType element) { return new BindType(Kind.LIST, element, null, null); }
    /// An unordered collection.
    public static BindType set(BindType element) { return new BindType(Kind.SET, element, null, null); }
    /// Key/value pairs.
    public static BindType map(BindType key, BindType value) { return new BindType(Kind.MAP, null, key, value); }

    public Kind kind() { return kind; }
    /// The element type of a list or set, or `null` when unknown or not a collection.
    public BindType element() { return element; }
    /// The key type of a map, or `null`.
    public BindType key() { return key; }
    /// The value type of a map, or `null`.
    public BindType value() { return value; }

    /// The bind type a tag implies.
    ///
    /// Container element types come back unknown: a tag says a field
    /// holds a list, and only the value says a list of what. Reporting
    /// that honestly is the point — the rendering mapping guesses
    /// `text` there, which is fine in a `CREATE TABLE` and wrong in a
    /// bind.
    public static BindType ofTag(TypeTag tag) {
        return switch (tag) {
            case TEXT, TEXT_VALIDATED, ENUM_STR -> TEXT;
            case ASCII -> ASCII;
            case BOOL -> BOOL;
            case BYTES -> BLOB;
            case SHORT -> INT16;
            case INT32, ENUM_ORD -> INT32;
            case INT -> INT64;
            // A 16-bit float is a float. The rendering mapping calls it
            // a smallint, which would bind the bit pattern.
            case HALF -> FLOAT16;
            case FLOAT32 -> FLOAT32;
            case FLOAT -> FLOAT64;
            case DECIMAL -> DECIMAL;
            case VARINT -> VARINT;
            case DATE -> DATE;
            case TIME -> TIME;
            case MILLIS -> TIMESTAMP_MILLIS;
            case NANOS -> TIMESTAMP_NANOS;
            // Textual on the wire, whatever the name suggests.
            case DATETIME -> TIMESTAMP_TEXT;
            case UUID_V1 -> TIME_UUID;
            case UUID_V7 -> UUID;
            case ULID -> ULID;
            case NULL -> NULL;
            case LIST, ARRAY -> list(null);
            case SET -> set(null);
            case MAP, TYPED_MAP -> map(null, null);
        };
    }

    /// Whether this type is still missing an element type.
    ///
    /// A layout built from tags alone leaves containers open; a caller
    /// preparing a statement against one needs either a form that says
    /// what they hold, or a sample value.
    public boolean isUnderdetermined() {
        return switch (kind) {
            case LIST, SET -> element == null;
            case MAP -> key == null || value == null;
            default -> false;
        };
    }

    @Override public boolean equals(Object other) {
        return other instanceof BindType b && kind == b.kind && Objects.equals(element, b.element) && Objects.equals(key, b.key) && Objects.equals(value, b.value);
    }
    @Override public int hashCode() { return Objects.hash(kind, element, key, value); }
    @Override public String toString() {
        String name = kind.name().toLowerCase(Locale.ROOT);
        return switch (kind) {
            case LIST, SET -> name + "<" + (element == null ? "?" : element) + ">";
            case MAP -> name + "<" + (key == null ? "?" : key) + ", " + (value == null ? "?" : value) + ">";
            default -> name;
        };
    }
}
