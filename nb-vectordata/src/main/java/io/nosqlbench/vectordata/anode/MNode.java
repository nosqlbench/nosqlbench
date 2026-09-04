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

import java.io.ByteArrayOutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/// A metadata record: an ordered map of named, typed fields, and the
/// wire format `metadata_content` facets hold.
///
/// ```text
/// [0x01][field_count: u16 LE]
/// per field: [name_len: u16 LE][name: UTF-8][type_tag: u8][value…]
/// ```
///
/// Every field carries its own [TypeTag], so a record is decodable
/// without a schema; the leading byte is the dialect leader that lets
/// [ANode] tell an MNode from a PNode. The framed variant prefixes the
/// payload with its `u32 LE` length for embedding in a stream.
public final class MNode {
    /// The dialect leader byte identifying an MNode record.
    public static final int DIALECT = 0x01;

    private final LinkedHashMap<String, MValue> fields = new LinkedHashMap<>();

    public MNode() { }

    /// Inserts a field, preserving insertion order; a repeated name
    /// replaces the value in place.
    public MNode insert(String name, MValue value) { fields.put(name, value); return this; }

    /// The field, or `null`.
    public MValue get(String name) { return fields.get(name); }

    /// The field at a position in declared order, or `null`.
    public MValue getAt(int index) {
        if (index < 0 || index >= fields.size()) return null;
        int at = 0;
        for (MValue value : fields.values()) if (at++ == index) return value;
        return null;
    }

    /// The fields in declared order, read-only.
    public Map<String, MValue> fields() { return Collections.unmodifiableMap(fields); }

    public int size() { return fields.size(); }

    /// The same names and tags with every value replaced by its type
    /// default: two records with equal fingerprints are congruent.
    public MNode fingerprint() {
        MNode out = new MNode();
        fields.forEach((name, value) -> out.insert(name, value.fingerprint()));
        return out;
    }

    /// Whether two records have the same fields in the same order
    /// with the same types, differing only in values.
    public boolean isCongruent(MNode other) { return fingerprint().display().equals(other.fingerprint().display()); }

    /// The reference's `Display`: `{name: 'value', age: 30}`.
    public String display() {
        StringJoiner text = new StringJoiner(", ", "{", "}");
        fields.forEach((name, value) -> text.add(name + ": " + value.display()));
        return text.toString();
    }

    @Override public String toString() { return display(); }
    @Override public boolean equals(Object other) { return other instanceof MNode m && fields.equals(m.fields); }
    @Override public int hashCode() { return fields.hashCode(); }

    // -- wire --

    /// Encodes with the dialect leader, no length prefix.
    public byte[] toBytes() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(DIALECT);
        writePayload(out);
        return out.toByteArray();
    }

    /// Encodes framed: a `u32 LE` payload length, then [#toBytes].
    public byte[] encodeFramed() {
        byte[] payload = toBytes();
        ByteBuffer framed = ByteBuffer.allocate(4 + payload.length).order(ByteOrder.LITTLE_ENDIAN);
        framed.putInt(payload.length).put(payload);
        return framed.array();
    }

    /// Decodes a record carrying the dialect leader.
    public static MNode fromBytes(byte[] data) {
        if (data.length == 0) throw new ANodeException("empty mnode data");
        if ((data[0] & 0xff) != DIALECT)
            throw new ANodeException(String.format("expected MNode dialect leader 0x%02x, got 0x%02x", DIALECT, data[0] & 0xff));
        ByteBuffer in = ByteBuffer.wrap(data, 1, data.length - 1).order(ByteOrder.LITTLE_ENDIAN);
        try { return readPayload(in); }
        catch (BufferUnderflowException | IndexOutOfBoundsException short_) { throw new ANodeException("mnode data is truncated"); }
    }

    /// Decodes one framed record from a stream buffer, advancing it.
    public static MNode fromFramed(ByteBuffer stream) {
        ByteBuffer in = stream.order(ByteOrder.LITTLE_ENDIAN);
        int length = in.getInt();
        byte[] payload = new byte[length];
        in.get(payload);
        return fromBytes(payload);
    }

    private void writePayload(ByteArrayOutputStream out) {
        Wire.u16(out, fields.size());
        fields.forEach((name, value) -> {
            byte[] utf8 = name.getBytes(StandardCharsets.UTF_8);
            Wire.u16(out, utf8.length);
            out.write(utf8, 0, utf8.length);
            writeTagged(out, value);
        });
    }

    static MNode readPayload(ByteBuffer in) {
        int count = Short.toUnsignedInt(in.getShort());
        MNode node = new MNode();
        for (int i = 0; i < count; i++) {
            String name = Wire.string(in, Short.toUnsignedInt(in.getShort()));
            node.insert(name, readTagged(in));
        }
        return node;
    }

    static void writeTagged(ByteArrayOutputStream out, MValue value) {
        out.write(value.tag().code());
        writeValue(out, value);
    }

    static void writeValue(ByteArrayOutputStream out, MValue value) {
        switch (value.tag()) {
            case TEXT, ENUM_STR, ASCII, DATE, TIME, DATETIME -> Wire.string(out, MValues.text(value));
            case INT -> Wire.i64(out, ((MValue.Int) value).value());
            case MILLIS -> Wire.i64(out, ((MValue.Millis) value).value());
            case FLOAT -> Wire.i64(out, Double.doubleToRawLongBits(((MValue.Float) value).value()));
            case BOOL -> out.write(((MValue.Bool) value).value() ? 1 : 0);
            case BYTES -> { byte[] b = ((MValue.Bytes) value).value(); Wire.u32(out, b.length); out.write(b, 0, b.length); }
            case NULL -> { }
            case ENUM_ORD -> Wire.i32(out, ((MValue.EnumOrd) value).value());
            case INT32 -> Wire.i32(out, ((MValue.Int32) value).value());
            case LIST -> { List<MValue> items = ((MValue.ListValue) value).items(); Wire.u32(out, items.size()); for (MValue item : items) writeTagged(out, item); }
            case SET -> { List<MValue> items = ((MValue.SetValue) value).items(); Wire.u32(out, items.size()); for (MValue item : items) writeTagged(out, item); }
            case MAP -> { byte[] payload = ((MValue.MapValue) value).node().toBytes(); Wire.u32(out, payload.length); out.write(payload, 0, payload.length); }
            case SHORT -> Wire.i16(out, ((MValue.Short) value).value());
            case FLOAT32 -> Wire.i32(out, java.lang.Float.floatToRawIntBits(((MValue.Float32) value).value()));
            case HALF -> Wire.u16(out, ((MValue.Half) value).bits());
            case NANOS -> { MValue.Nanos n = (MValue.Nanos) value; Wire.i64(out, n.epochSeconds()); Wire.i32(out, n.nanoAdjust()); }
            case UUID_V1 -> out.write(((MValue.UuidV1) value).value(), 0, 16);
            case UUID_V7 -> out.write(((MValue.UuidV7) value).value(), 0, 16);
            case ULID -> out.write(((MValue.Ulid) value).value(), 0, 16);
            case ARRAY -> { MValue.ArrayValue a = (MValue.ArrayValue) value; out.write(a.elementTag().code()); Wire.u32(out, a.items().size()); for (MValue item : a.items()) writeValue(out, item); }
            case TYPED_MAP -> { List<MValue.TypedMap.Entry> entries = ((MValue.TypedMap) value).entries(); Wire.u32(out, entries.size()); for (MValue.TypedMap.Entry e : entries) { writeTagged(out, e.key()); writeTagged(out, e.value()); } }
            case TEXT_VALIDATED, DECIMAL, VARINT -> throw new ANodeException("no value carries tag " + value.tag());
        }
    }

    static MValue readTagged(ByteBuffer in) {
        int code = in.get() & 0xff;
        TypeTag tag = TypeTag.fromCode(code);
        if (tag == null) throw new ANodeException("unknown type tag: " + code);
        return readValue(in, tag);
    }

    static MValue readValue(ByteBuffer in, TypeTag tag) {
        return switch (tag) {
            case TEXT, TEXT_VALIDATED -> new MValue.Text(Wire.string(in, Wire.u32(in)));
            case INT -> new MValue.Int(in.getLong());
            case FLOAT -> new MValue.Float(in.getDouble());
            case BOOL -> new MValue.Bool(in.get() != 0);
            case BYTES -> new MValue.Bytes(Wire.bytes(in, Wire.u32(in)));
            case NULL -> MValue.NULL;
            case ENUM_STR -> new MValue.EnumStr(Wire.string(in, Wire.u32(in)));
            case ENUM_ORD -> new MValue.EnumOrd(in.getInt());
            case LIST -> { int n = Wire.u32(in); List<MValue> items = new ArrayList<>(n); for (int i = 0; i < n; i++) items.add(readTagged(in)); yield new MValue.ListValue(items); }
            case MAP -> new MValue.MapValue(fromBytes(Wire.bytes(in, Wire.u32(in))));
            case ASCII -> new MValue.Ascii(Wire.string(in, Wire.u32(in)));
            case INT32 -> new MValue.Int32(in.getInt());
            case SHORT -> new MValue.Short(in.getShort());
            // A decimal is its scale and digits; the reference keeps the
            // digits as bytes and drops the scale, and so does this.
            case DECIMAL -> { in.getInt(); yield new MValue.Bytes(Wire.bytes(in, Wire.u32(in))); }
            case VARINT -> new MValue.Bytes(Wire.bytes(in, Wire.u32(in)));
            case FLOAT32 -> new MValue.Float32(in.getFloat());
            case HALF -> new MValue.Half(Short.toUnsignedInt(in.getShort()));
            case MILLIS -> new MValue.Millis(in.getLong());
            case NANOS -> new MValue.Nanos(in.getLong(), in.getInt());
            case DATE -> new MValue.Date(Wire.string(in, Wire.u32(in)));
            case TIME -> new MValue.Time(Wire.string(in, Wire.u32(in)));
            case DATETIME -> new MValue.DateTime(Wire.string(in, Wire.u32(in)));
            case UUID_V1 -> new MValue.UuidV1(Wire.bytes(in, 16));
            case UUID_V7 -> new MValue.UuidV7(Wire.bytes(in, 16));
            case ULID -> new MValue.Ulid(Wire.bytes(in, 16));
            case ARRAY -> {
                int code = in.get() & 0xff;
                TypeTag element = TypeTag.fromCode(code);
                if (element == null) throw new ANodeException("unknown array element tag");
                int n = Wire.u32(in);
                List<MValue> items = new ArrayList<>(n);
                for (int i = 0; i < n; i++) items.add(readValue(in, element));
                yield new MValue.ArrayValue(element, items);
            }
            case SET -> { int n = Wire.u32(in); List<MValue> items = new ArrayList<>(n); for (int i = 0; i < n; i++) items.add(readTagged(in)); yield new MValue.SetValue(items); }
            case TYPED_MAP -> {
                int n = Wire.u32(in);
                List<MValue.TypedMap.Entry> entries = new ArrayList<>(n);
                for (int i = 0; i < n; i++) { MValue key = readTagged(in); entries.add(new MValue.TypedMap.Entry(key, readTagged(in))); }
                yield new MValue.TypedMap(entries);
            }
        };
    }
}
