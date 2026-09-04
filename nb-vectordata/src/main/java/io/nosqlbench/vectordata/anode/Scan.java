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

import io.nosqlbench.vectordata.anode.PNode.Comparand;
import io.nosqlbench.vectordata.anode.PNode.FieldRef;
import io.nosqlbench.vectordata.anode.PNode.OpType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/// The raw walk over MNode bytes — the reference's `mnode::scan`.
///
/// Walks a record's fields without materializing an [MNode] or an
/// [MValue]: names are skipped over rather than copied, and each value
/// is reached as a position and a tag. A caller addressing fields by
/// position never looks at a name. Built for predicate scanning in the
/// reference and reused there as the binding hot path; here it is the
/// binding hot path.
public final class Scan {
    private Scan() { }

    /// Walks a record's fields in wire order. The leader byte must be
    /// the MNode dialect: a record says what it is, and a PNode brought
    /// here is refused by its own first byte.
    public static Fields fields(byte[] data) {
        if (data.length == 0) throw ScanException.unexpectedEof();
        if ((data[0] & 0xff) != MNode.DIALECT) throw ScanException.invalidDialect(data[0] & 0xff);
        if (data.length < 3) throw ScanException.unexpectedEof();
        return new Fields(data, 3, u16(data, 1));
    }

    /// The fields of one record, walked lazily. One [Field] view per
    /// field, each a handful of offsets into the record; no copies.
    public static final class Fields implements Iterator<Field>, Iterable<Field> {
        private final byte[] data;
        private int pos;
        private int remaining;
        private int index;

        Fields(byte[] data, int pos, int count) { this.data = data; this.pos = pos; this.remaining = count; }

        @Override public Iterator<Field> iterator() { return this; }
        @Override public boolean hasNext() { return remaining > 0; }

        @Override public Field next() {
            if (remaining == 0) throw new NoSuchElementException();
            remaining--;
            int at = index++;
            try {
                if (pos + 2 > data.length) throw ScanException.unexpectedEof();
                int nameLength = u16(data, pos);
                int nameStart = pos + 2;
                if (nameStart + nameLength > data.length) throw ScanException.unexpectedEof();
                int tagAt = nameStart + nameLength;
                if (tagAt >= data.length) throw ScanException.unexpectedEof();
                int tag = data[tagAt] & 0xff;
                int valueStart = tagAt + 1;
                int end = skipValue(data, valueStart, tag);
                pos = end;
                return new Field(data, at, nameStart, nameLength, tag, valueStart, end);
            } catch (ScanException fault) {
                // A malformed record cannot be walked past; stop rather
                // than report the same fault once per declared field.
                remaining = 0;
                throw fault;
            }
        }

        /// The offset the walk has reached: the end of the last field
        /// returned, or the end of the record once exhausted.
        public int position() { return pos; }
    }

    /// Advances past one typed value whose payload starts at `pos`,
    /// immediately after the tag byte, returning the offset of the
    /// first byte after it. Pure indexing, no buffers.
    public static int skipValue(byte[] data, int pos, int tag) {
        switch (tag) {
            case 5: return pos;                                   // Null
            case 3: return bounded(data, pos, 1);                 // Bool
            case 13: case 17: return bounded(data, pos, 2);       // Short, Half
            case 7: case 12: case 16: return bounded(data, pos, 4); // EnumOrd, Int32, Float32
            case 1: case 2: case 18: return bounded(data, pos, 8); // Int, Float, Millis
            case 19: return bounded(data, pos, 12);               // Nanos
            case 23: case 24: case 25: return bounded(data, pos, 16); // UuidV1, UuidV7, Ulid
            // Length-prefixed: Text, Bytes, EnumStr, Map, TextValidated, Ascii, Date, Time, DateTime, Varint
            case 0: case 4: case 6: case 9: case 10: case 11: case 20: case 21: case 22: case 15: return skipLength32(data, pos);
            case 14: {                                            // Decimal: i32 scale + u32 len + digits
                if (pos + 8 > data.length) throw ScanException.unexpectedEof();
                return bounded(data, pos, 8 + u32(data, pos + 4));
            }
            case 8: case 27: return skipTaggedList(data, pos);    // List, Set
            case 26: {                                            // Array: elem tag + u32 count + untagged elements
                if (pos + 5 > data.length) throw ScanException.unexpectedEof();
                int elementTag = data[pos] & 0xff;
                long count = u32(data, pos + 1);
                int p = pos + 5;
                for (long i = 0; i < count; i++) p = skipValue(data, p, elementTag);
                return p;
            }
            case 28: {                                            // TypedMap: u32 count + tagged pairs
                if (pos + 4 > data.length) throw ScanException.unexpectedEof();
                long count = u32(data, pos);
                int p = pos + 4;
                for (long i = 0; i < count; i++) {
                    if (p >= data.length) throw ScanException.unexpectedEof();
                    p = skipValue(data, p + 1, data[p] & 0xff);
                    if (p >= data.length) throw ScanException.unexpectedEof();
                    p = skipValue(data, p + 1, data[p] & 0xff);
                }
                return p;
            }
            default: throw ScanException.invalidTag(tag);
        }
    }

    private static int bounded(byte[] data, int pos, long size) {
        long end = pos + size;
        if (end > data.length) throw ScanException.unexpectedEof();
        return (int) end;
    }

    private static int skipLength32(byte[] data, int pos) {
        if (pos + 4 > data.length) throw ScanException.unexpectedEof();
        return bounded(data, pos, 4 + u32(data, pos));
    }

    private static int skipTaggedList(byte[] data, int pos) {
        if (pos + 4 > data.length) throw ScanException.unexpectedEof();
        long count = u32(data, pos);
        int p = pos + 4;
        for (long i = 0; i < count; i++) {
            if (p >= data.length) throw ScanException.unexpectedEof();
            p = skipValue(data, p + 1, data[p] & 0xff);
        }
        return p;
    }

    static int u16(byte[] data, int at) { return (data[at] & 0xff) | ((data[at + 1] & 0xff) << 8); }
    static long u32(byte[] data, int at) { return ((data[at] & 0xff) | ((data[at + 1] & 0xff) << 8) | ((data[at + 2] & 0xff) << 16) | ((long) (data[at + 3] & 0xff) << 24)); }

    /// The field layout a sample record carries: names in wire order
    /// and the tag each holds, captured once so a compiled path can
    /// address fields by position thereafter.
    public record RecordSchema(List<String> fieldNames, List<Integer> tags) {
        public RecordSchema { fieldNames = List.copyOf(fieldNames); tags = List.copyOf(tags); }
        public int fieldCount() { return fieldNames.size(); }
    }

    /// Discovers the field layout from a raw MNode record, skipping
    /// values as it goes.
    public static RecordSchema discoverSchema(byte[] data) {
        List<String> names = new ArrayList<>();
        List<Integer> tags = new ArrayList<>();
        for (Field field : fields(data)) { names.add(field.name()); tags.add(field.tag()); }
        return new RecordSchema(names, tags);
    }

    /// One condition of a flat predicate: `field op comparands`.
    public record FlatCondition(String field, OpType op, List<Comparand> comparands) {
        public FlatCondition { comparands = List.copyOf(comparands); }
    }

    /// Flattens a predicate into `AND`-joined conditions over named
    /// fields, or `null` when it is not one: an `OR` anywhere, or an
    /// indexed field reference, leaves it without a flat parameter
    /// list.
    public static List<FlatCondition> flattenAnd(PNode node) {
        if (node instanceof PNode.Predicate p) {
            if (!(p.field() instanceof FieldRef.Named named)) return null;
            return List.of(new FlatCondition(named.name(), p.op(), p.comparands()));
        }
        PNode.Conjugate c = (PNode.Conjugate) node;
        if (c.type() != PNode.ConjugateType.AND) return null;
        List<FlatCondition> out = new ArrayList<>();
        for (PNode child : c.children()) {
            List<FlatCondition> flat = flattenAnd(child);
            if (flat == null) return null;
            out.addAll(flat);
        }
        return out;
    }

    /// Whether a field absent from a record satisfies a condition — the
    /// evaluator's rule: a missing field matches `= NULL`, `IN (…NULL…)`,
    /// and `!= <anything but NULL>`.
    public static boolean missingFieldPasses(OpType op, List<Comparand> comparands) {
        return switch (op) {
            case EQ, IN -> comparands.stream().anyMatch(c -> c instanceof Comparand.Null);
            case NE -> comparands.stream().noneMatch(c -> c instanceof Comparand.Null);
            default -> false;
        };
    }
}
