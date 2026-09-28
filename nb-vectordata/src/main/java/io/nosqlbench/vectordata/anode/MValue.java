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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/// A single typed value in an [MNode] field. Each variant maps to a
/// [TypeTag] on the wire; scalars carry their value directly and
/// containers recurse. Three wire tags have no variant of their own
/// and decode into the nearest one, as the reference does:
/// `text_validated` reads as [Text], `decimal` and `varint` as [Bytes].
public sealed interface MValue {

    /// The wire tag this value carries.
    TypeTag tag();

    /// A structural fingerprint: the same tag — and for a container,
    /// the same recursive structure — with every payload replaced by
    /// its type default. Two values with equal fingerprints are
    /// congruent: same shape, different data.
    MValue fingerprint();

    /// The reference's `Display` rendering: quoted text, bare numbers,
    /// `NULL`, `0x…` bytes, `[…]` lists, `{…}` sets and maps.
    String display();

    // -- scalars --
    record Text(String value) implements MValue {
        public TypeTag tag() { return TypeTag.TEXT; }
        public MValue fingerprint() { return new Text(""); }
        public String display() { return "'" + value + "'"; }
    }
    record Int(long value) implements MValue {
        public TypeTag tag() { return TypeTag.INT; }
        public MValue fingerprint() { return new Int(0); }
        public String display() { return Long.toString(value); }
    }
    record Float(double value) implements MValue {
        public TypeTag tag() { return TypeTag.FLOAT; }
        public MValue fingerprint() { return new Float(0.0); }
        public String display() { return Numbers.f64(value); }
    }
    record Bool(boolean value) implements MValue {
        public TypeTag tag() { return TypeTag.BOOL; }
        public MValue fingerprint() { return new Bool(false); }
        public String display() { return Boolean.toString(value); }
    }
    /// Raw bytes; equal by content.
    record Bytes(byte[] value) implements MValue {
        public Bytes { value = value.clone(); }
        public TypeTag tag() { return TypeTag.BYTES; }
        public MValue fingerprint() { return new Bytes(new byte[0]); }
        public String display() { return "0x" + Numbers.hex(value); }
        @Override public boolean equals(Object other) { return other instanceof Bytes b && Arrays.equals(value, b.value); }
        @Override public int hashCode() { return Arrays.hashCode(value); }
        @Override public String toString() { return "Bytes[" + Numbers.hex(value) + "]"; }
    }
    /// The null value. One instance, [#NULL].
    record Null() implements MValue {
        public TypeTag tag() { return TypeTag.NULL; }
        public MValue fingerprint() { return this; }
        public String display() { return "NULL"; }
    }
    MValue NULL = new Null();
    record EnumStr(String value) implements MValue {
        public TypeTag tag() { return TypeTag.ENUM_STR; }
        public MValue fingerprint() { return new EnumStr(""); }
        public String display() { return "'" + value + "'"; }
    }
    record EnumOrd(int value) implements MValue {
        public TypeTag tag() { return TypeTag.ENUM_ORD; }
        public MValue fingerprint() { return new EnumOrd(0); }
        public String display() { return "enum(" + value + ")"; }
    }
    record Ascii(String value) implements MValue {
        public TypeTag tag() { return TypeTag.ASCII; }
        public MValue fingerprint() { return new Ascii(""); }
        public String display() { return "'" + value + "'"; }
    }
    record Int32(int value) implements MValue {
        public TypeTag tag() { return TypeTag.INT32; }
        public MValue fingerprint() { return new Int32(0); }
        public String display() { return Integer.toString(value); }
    }
    record Short(short value) implements MValue {
        public TypeTag tag() { return TypeTag.SHORT; }
        public MValue fingerprint() { return new Short((short) 0); }
        public String display() { return java.lang.Short.toString(value); }
    }
    record Float32(float value) implements MValue {
        public TypeTag tag() { return TypeTag.FLOAT32; }
        public MValue fingerprint() { return new Float32(0f); }
        public String display() { return Numbers.f32(value); }
    }
    /// An IEEE 754 half-precision float as its raw 16 bits, `0..65535`.
    record Half(int bits) implements MValue {
        public TypeTag tag() { return TypeTag.HALF; }
        public MValue fingerprint() { return new Half(0); }
        public String display() { return String.format("half(0x%04x)", bits & 0xffff); }
    }
    record Millis(long value) implements MValue {
        public TypeTag tag() { return TypeTag.MILLIS; }
        public MValue fingerprint() { return new Millis(0); }
        public String display() { return "millis(" + value + ")"; }
    }
    record Nanos(long epochSeconds, int nanoAdjust) implements MValue {
        public TypeTag tag() { return TypeTag.NANOS; }
        public MValue fingerprint() { return new Nanos(0, 0); }
        public String display() { return "nanos(" + epochSeconds + "." + nanoAdjust + ")"; }
    }
    record Date(String value) implements MValue {
        public TypeTag tag() { return TypeTag.DATE; }
        public MValue fingerprint() { return new Date(""); }
        public String display() { return "'" + value + "'"; }
    }
    record Time(String value) implements MValue {
        public TypeTag tag() { return TypeTag.TIME; }
        public MValue fingerprint() { return new Time(""); }
        public String display() { return "'" + value + "'"; }
    }
    record DateTime(String value) implements MValue {
        public TypeTag tag() { return TypeTag.DATETIME; }
        public MValue fingerprint() { return new DateTime(""); }
        public String display() { return "'" + value + "'"; }
    }
    record UuidV1(byte[] value) implements MValue {
        public UuidV1 { value = sixteen(value); }
        public TypeTag tag() { return TypeTag.UUID_V1; }
        public MValue fingerprint() { return new UuidV1(new byte[16]); }
        public String display() { return Numbers.uuid(value); }
        @Override public boolean equals(Object other) { return other instanceof UuidV1 b && Arrays.equals(value, b.value); }
        @Override public int hashCode() { return Arrays.hashCode(value); }
        @Override public String toString() { return "UuidV1[" + Numbers.uuid(value) + "]"; }
    }
    record UuidV7(byte[] value) implements MValue {
        public UuidV7 { value = sixteen(value); }
        public TypeTag tag() { return TypeTag.UUID_V7; }
        public MValue fingerprint() { return new UuidV7(new byte[16]); }
        public String display() { return Numbers.uuid(value); }
        @Override public boolean equals(Object other) { return other instanceof UuidV7 b && Arrays.equals(value, b.value); }
        @Override public int hashCode() { return Arrays.hashCode(value); }
        @Override public String toString() { return "UuidV7[" + Numbers.uuid(value) + "]"; }
    }
    record Ulid(byte[] value) implements MValue {
        public Ulid { value = sixteen(value); }
        public TypeTag tag() { return TypeTag.ULID; }
        public MValue fingerprint() { return new Ulid(new byte[16]); }
        public String display() { return "ulid(" + Numbers.hex(value) + ")"; }
        @Override public boolean equals(Object other) { return other instanceof Ulid b && Arrays.equals(value, b.value); }
        @Override public int hashCode() { return Arrays.hashCode(value); }
        @Override public String toString() { return "Ulid[" + Numbers.hex(value) + "]"; }
    }

    // -- containers --
    /// A heterogeneous list of tagged values.
    record ListValue(List<MValue> items) implements MValue {
        public ListValue { items = List.copyOf(items); }
        public TypeTag tag() { return TypeTag.LIST; }
        public MValue fingerprint() { return new ListValue(fingerprints(items)); }
        public String display() { return joined("[", "]", items); }
    }
    /// A nested record.
    record MapValue(MNode node) implements MValue {
        public TypeTag tag() { return TypeTag.MAP; }
        public MValue fingerprint() { return new MapValue(node.fingerprint()); }
        public String display() { return node.display(); }
    }
    /// A homogeneous array: one element tag, untagged values.
    record ArrayValue(TypeTag elementTag, List<MValue> items) implements MValue {
        public ArrayValue { items = List.copyOf(items); Objects.requireNonNull(elementTag); }
        public TypeTag tag() { return TypeTag.ARRAY; }
        public MValue fingerprint() { return new ArrayValue(elementTag, fingerprints(items)); }
        public String display() { return joined("[", "]", items); }
    }
    /// An unordered set of tagged values.
    record SetValue(List<MValue> items) implements MValue {
        public SetValue { items = List.copyOf(items); }
        public TypeTag tag() { return TypeTag.SET; }
        public MValue fingerprint() { return new SetValue(fingerprints(items)); }
        public String display() { return joined("{", "}", items); }
    }
    /// A map of tagged keys to tagged values, in declared order.
    record TypedMap(List<Entry> entries) implements MValue {
        public TypedMap { entries = List.copyOf(entries); }
        public record Entry(MValue key, MValue value) { }
        public TypeTag tag() { return TypeTag.TYPED_MAP; }
        public MValue fingerprint() {
            List<Entry> out = new ArrayList<>(entries.size());
            for (Entry e : entries) out.add(new Entry(e.key().fingerprint(), e.value().fingerprint()));
            return new TypedMap(out);
        }
        public String display() {
            StringJoiner text = new StringJoiner(", ", "{", "}");
            for (Entry e : entries) text.add(e.key().display() + ": " + e.value().display());
            return text.toString();
        }
    }

    private static byte[] sixteen(byte[] value) {
        if (value.length != 16) throw new IllegalArgumentException("a 16-byte identifier, not " + value.length + " bytes");
        return value.clone();
    }

    private static List<MValue> fingerprints(List<MValue> items) {
        List<MValue> out = new ArrayList<>(items.size());
        for (MValue item : items) out.add(item.fingerprint());
        return out;
    }

    private static String joined(String open, String close, List<MValue> items) {
        StringJoiner text = new StringJoiner(", ", open, close);
        for (MValue item : items) text.add(item.display());
        return text.toString();
    }
}
