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
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/// A predicate tree — the wire format `metadata_predicates` facets
/// hold: leaf [Predicate]s of a field, an operator, and comparands,
/// combined by [Conjugate] `AND`/`OR` nodes. Encoding is recursive and
/// pre-order, each node led by its [ConjugateType] byte.
///
/// Three wire sub-formats exist. **Indexed** names fields by position
/// and carries only `i64` comparands. **Named** carries field names,
/// and comes in two spellings distinguished by the byte after the
/// dialect leader: the legacy form (an `i64` per comparand) begins
/// with a [ConjugateType] byte, and the typed form begins with `0xFF`
/// and tags each comparand with its [Comparand] type.
public sealed interface PNode {
    /// The dialect leader byte identifying a PNode record.
    int DIALECT = 0x02;
    /// The marker byte that opens the typed named format.
    int TYPED_MARKER = 0xFF;

    /// A leaf: `field op (v1, v2, …)`. Single-comparand operators carry
    /// one value; `IN` carries the membership set.
    record Predicate(FieldRef field, OpType op, List<Comparand> comparands) implements PNode {
        public Predicate { comparands = List.copyOf(comparands); }
    }

    /// An interior node combining children with `AND` or `OR`.
    record Conjugate(ConjugateType type, List<PNode> children) implements PNode {
        public Conjugate { children = List.copyOf(children); if (type == ConjugateType.PRED) throw new IllegalArgumentException("a conjugate is AND or OR"); }
    }

    /// The tree with every comparand replaced by its type default:
    /// same shape, fields, operators, and comparand types.
    default PNode fingerprint() {
        if (this instanceof Predicate p) {
            List<Comparand> blank = new ArrayList<>(p.comparands().size());
            for (Comparand c : p.comparands()) blank.add(c.fingerprint());
            return new Predicate(p.field(), p.op(), blank);
        }
        Conjugate c = (Conjugate) this;
        List<PNode> children = new ArrayList<>(c.children().size());
        for (PNode child : c.children()) children.add(child.fingerprint());
        return new Conjugate(c.type(), children);
    }

    /// Whether two trees differ only in comparand values.
    default boolean isCongruent(PNode other) { return fingerprint().display().equals(other.fingerprint().display()); }

    /// The reference's `Display`: `age > 18`, `status IN (1, 2)`,
    /// `(a = 1 AND b < 2)`. [PNodeDisplay#parse] reads it back.
    default String display() {
        if (this instanceof Predicate p) {
            StringBuilder text = new StringBuilder(p.field().display()).append(' ').append(p.op().symbol()).append(' ');
            if (p.comparands().size() == 1) return text.append(p.comparands().get(0).display()).toString();
            StringJoiner list = new StringJoiner(", ", "(", ")");
            for (Comparand c : p.comparands()) list.add(c.display());
            return text.append(list).toString();
        }
        Conjugate c = (Conjugate) this;
        StringJoiner text = new StringJoiner(" " + c.type().name() + " ", "(", ")");
        for (PNode child : c.children()) text.add(child.display());
        return text.toString();
    }

    // -- wire --

    /// Encodes in indexed mode: fields by position, `i64` comparands
    /// only. A named field or a non-integer comparand is refused.
    default byte[] toBytesIndexed() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(DIALECT);
        writeIndexed(this, out);
        return out.toByteArray();
    }

    /// Encodes in the typed named mode: `0x02 0xFF`, then the tree with
    /// field names and tagged comparands.
    default byte[] toBytesNamed() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(DIALECT);
        out.write(TYPED_MARKER);
        writeNamed(this, out);
        return out.toByteArray();
    }

    /// Decodes indexed-mode bytes; every comparand becomes an `Int`.
    static PNode fromBytesIndexed(byte[] data) {
        ByteBuffer in = leader(data, "pnode");
        try { return readIndexed(in); }
        catch (BufferUnderflowException | IndexOutOfBoundsException short_) { throw new ANodeException("pnode data is truncated"); }
    }

    /// Decodes named-mode bytes, legacy or typed by the marker byte.
    static PNode fromBytesNamed(byte[] data) {
        ByteBuffer in = leader(data, "pnode");
        if (data.length < 2) throw new ANodeException("pnode data too short");
        try {
            if ((data[1] & 0xff) == TYPED_MARKER) { in.get(); return readNamed(in, true); }
            return readNamed(in, false);
        } catch (BufferUnderflowException | IndexOutOfBoundsException short_) { throw new ANodeException("pnode data is truncated"); }
    }

    private static ByteBuffer leader(byte[] data, String what) {
        if (data.length == 0) throw new ANodeException("empty " + what + " data");
        if ((data[0] & 0xff) != DIALECT)
            throw new ANodeException(String.format("expected PNode dialect leader 0x%02x, got 0x%02x", DIALECT, data[0] & 0xff));
        return ByteBuffer.wrap(data, 1, data.length - 1).order(ByteOrder.LITTLE_ENDIAN);
    }

    private static void writeIndexed(PNode node, ByteArrayOutputStream out) {
        if (node instanceof Predicate p) {
            out.write(ConjugateType.PRED.code());
            if (!(p.field() instanceof FieldRef.Index index)) throw new ANodeException("named field in indexed mode");
            out.write(index.value());
            out.write(p.op().code());
            Wire.i16(out, p.comparands().size());
            for (Comparand c : p.comparands()) Wire.i64(out, c.asLong());
            return;
        }
        Conjugate c = (Conjugate) node;
        out.write(c.type().code());
        out.write(c.children().size());
        for (PNode child : c.children()) writeIndexed(child, out);
    }

    private static void writeNamed(PNode node, ByteArrayOutputStream out) {
        if (node instanceof Predicate p) {
            out.write(ConjugateType.PRED.code());
            if (!(p.field() instanceof FieldRef.Named named)) throw new ANodeException("indexed field in named mode");
            byte[] utf8 = named.name().getBytes(StandardCharsets.UTF_8);
            Wire.u16(out, utf8.length);
            out.write(utf8, 0, utf8.length);
            out.write(p.op().code());
            Wire.i16(out, p.comparands().size());
            for (Comparand c : p.comparands()) Comparand.write(out, c);
            return;
        }
        Conjugate c = (Conjugate) node;
        out.write(c.type().code());
        out.write(c.children().size());
        for (PNode child : c.children()) writeNamed(child, out);
    }

    private static PNode readIndexed(ByteBuffer in) {
        ConjugateType type = conjugate(in.get() & 0xff);
        if (type == ConjugateType.PRED) {
            int index = in.get() & 0xff;
            OpType op = op(in.get() & 0xff);
            int count = in.getShort();
            List<Comparand> comparands = new ArrayList<>(Math.max(0, count));
            for (int i = 0; i < count; i++) comparands.add(new Comparand.Int(in.getLong()));
            return new Predicate(new FieldRef.Index(index), op, comparands);
        }
        int children = in.get() & 0xff;
        List<PNode> out = new ArrayList<>(children);
        for (int i = 0; i < children; i++) out.add(readIndexed(in));
        return new Conjugate(type, out);
    }

    private static PNode readNamed(ByteBuffer in, boolean typed) {
        ConjugateType type = conjugate(in.get() & 0xff);
        if (type == ConjugateType.PRED) {
            String name = Wire.string(in, Short.toUnsignedInt(in.getShort()));
            OpType op = op(in.get() & 0xff);
            int count = in.getShort();
            List<Comparand> comparands = new ArrayList<>(Math.max(0, count));
            for (int i = 0; i < count; i++) comparands.add(typed ? Comparand.read(in) : new Comparand.Int(in.getLong()));
            return new Predicate(new FieldRef.Named(name), op, comparands);
        }
        int children = in.get() & 0xff;
        List<PNode> out = new ArrayList<>(children);
        for (int i = 0; i < children; i++) out.add(readNamed(in, typed));
        return new Conjugate(type, out);
    }

    private static ConjugateType conjugate(int code) {
        ConjugateType type = ConjugateType.fromCode(code);
        if (type == null) throw new ANodeException("unknown conjugate type: " + code);
        return type;
    }

    private static OpType op(int code) {
        OpType op = OpType.fromCode(code);
        if (op == null) throw new ANodeException("unknown op: " + code);
        return op;
    }

    /// The node type discriminant leading every node on the wire.
    enum ConjugateType {
        PRED(0), AND(1), OR(2);
        private final int code;
        ConjugateType(int code) { this.code = code; }
        public int code() { return code; }
        public static ConjugateType fromCode(int code) { return code >= 0 && code < 3 ? values()[code] : null; }
    }

    /// A comparison operator, with its wire byte and its symbol.
    enum OpType {
        GT(0, ">"), LT(1, "<"), EQ(2, "="), NE(3, "!="), GE(4, ">="), LE(5, "<="), IN(6, "IN"), MATCHES(7, "MATCHES");
        private final int code; private final String symbol;
        OpType(int code, String symbol) { this.code = code; this.symbol = symbol; }
        public int code() { return code; }
        public String symbol() { return symbol; }
        public static OpType fromCode(int code) { return code >= 0 && code < 8 ? values()[code] : null; }
        @Override public String toString() { return symbol; }
    }

    /// A field reference: a position (indexed wire format) or a name.
    sealed interface FieldRef {
        String display();
        record Index(int value) implements FieldRef {
            public Index { if (value < 0 || value > 255) throw new IllegalArgumentException("field index " + value + " does not fit a byte"); }
            public String display() { return "field[" + value + "]"; }
        }
        record Named(String name) implements FieldRef { public String display() { return name; } }
    }

    /// A typed comparand. The typed named format tags each with its
    /// kind: `0` int, `1` float, `2` text, `3` bool, `4` bytes, `5` null.
    sealed interface Comparand {
        Comparand fingerprint();
        /// The reference's `Display`: numbers bare (floats always with a
        /// point), text single-quoted, bytes as `X'…'`, `NULL`.
        String display();
        /// The integer value, for the indexed format that carries
        /// nothing else.
        default long asLong() { throw new ANodeException("indexed mode only supports Int comparands, got " + display()); }

        record Int(long value) implements Comparand {
            public Comparand fingerprint() { return new Int(0); }
            public String display() { return Long.toString(value); }
            @Override public long asLong() { return value; }
        }
        record Float(double value) implements Comparand {
            public Comparand fingerprint() { return new Float(0.0); }
            public String display() { return Numbers.withPoint(Numbers.f64(value)); }
        }
        record Text(String value) implements Comparand {
            public Comparand fingerprint() { return new Text(""); }
            public String display() { return "'" + value + "'"; }
        }
        record Bool(boolean value) implements Comparand {
            public Comparand fingerprint() { return new Bool(false); }
            public String display() { return Boolean.toString(value); }
        }
        record Bytes(byte[] value) implements Comparand {
            public Bytes { value = value.clone(); }
            public Comparand fingerprint() { return new Bytes(new byte[0]); }
            public String display() { return "X'" + Numbers.hex(value) + "'"; }
            @Override public boolean equals(Object other) { return other instanceof Bytes b && Arrays.equals(value, b.value); }
            @Override public int hashCode() { return Arrays.hashCode(value); }
            @Override public String toString() { return "Bytes[" + Numbers.hex(value) + "]"; }
        }
        record Null() implements Comparand {
            public Comparand fingerprint() { return this; }
            public String display() { return "NULL"; }
        }
        Comparand NULL = new Null();

        static void write(ByteArrayOutputStream out, Comparand c) {
            if (c instanceof Int v) { out.write(0); Wire.i64(out, v.value()); }
            else if (c instanceof Float v) { out.write(1); Wire.i64(out, Double.doubleToRawLongBits(v.value())); }
            else if (c instanceof Text v) { out.write(2); byte[] utf8 = v.value().getBytes(StandardCharsets.UTF_8); Wire.u16(out, utf8.length); out.write(utf8, 0, utf8.length); }
            else if (c instanceof Bool v) { out.write(3); out.write(v.value() ? 1 : 0); }
            else if (c instanceof Bytes v) { out.write(4); Wire.u32(out, v.value().length); out.write(v.value(), 0, v.value().length); }
            else out.write(5);
        }

        static Comparand read(ByteBuffer in) {
            int tag = in.get() & 0xff;
            return switch (tag) {
                case 0 -> new Int(in.getLong());
                case 1 -> new Float(in.getDouble());
                case 2 -> new Text(Wire.string(in, Short.toUnsignedInt(in.getShort())));
                case 3 -> new Bool(in.get() != 0);
                case 4 -> new Bytes(Wire.bytes(in, Wire.u32(in)));
                case 5 -> NULL;
                default -> throw new ANodeException("unknown comparand type tag: " + tag);
            };
        }
    }
}
