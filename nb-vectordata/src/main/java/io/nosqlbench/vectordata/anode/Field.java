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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/// One field of a record, viewed in place.
///
/// Nothing is copied: the name and the value payload are positions
/// into the buffer the record was read from, and the tag is the wire
/// discriminant. The accessors read the payload directly — a caller on
/// a hot path uses the primitive ones and never materializes a name or
/// an [MValue]; a caller wanting the AST view converts once through
/// [#value].
///
/// The accessors are typed by the tag's **wire** form, not its name:
/// `Date`, `Time`, and `DateTime` are length-prefixed strings and read
/// through [#stringValue]; `Half` holds binary16 bits and reads through
/// [#doubleValue], never as an integer; `Nanos` is two numbers and
/// reads through [#nanosSeconds] and [#nanosAdjust], never as one.
public final class Field {
    private final byte[] data;
    private final int index;
    private final int nameStart;
    private final int nameLength;
    private final int tag;
    private final int valueStart;
    private final int valueEnd;

    Field(byte[] data, int index, int nameStart, int nameLength, int tag, int valueStart, int valueEnd) {
        this.data = data; this.index = index; this.nameStart = nameStart; this.nameLength = nameLength;
        this.tag = tag; this.valueStart = valueStart; this.valueEnd = valueEnd;
    }

    /// Position in the record's field order — the stable identity a
    /// compiled binder addresses fields by.
    public int index() { return index; }
    /// The wire type discriminant; see [TypeTag].
    public int tag() { return tag; }
    /// The tag as an enum, or `null` for a byte the format does not define.
    public TypeTag typeTag() { return tag >= 0 && tag <= 28 ? TypeTag.fromCode(tag) : null; }
    /// The field's name, decoded. Allocates; a compiled path resolves
    /// names to positions once and never calls this per record.
    public String name() { return new String(data, nameStart, nameLength, StandardCharsets.UTF_8); }
    /// Where the value payload starts in the record, after the tag byte.
    public int valueOffset() { return valueStart; }
    /// The payload's length in bytes.
    public int valueLength() { return valueEnd - valueStart; }
    /// The payload as a little-endian buffer over the record's bytes.
    public ByteBuffer valueBuffer() { return ByteBuffer.wrap(data, valueStart, valueEnd - valueStart).slice().order(ByteOrder.LITTLE_ENDIAN); }

    /// Whether the payload **is** an integer: `Int` and `Millis` (i64),
    /// `EnumOrd` and `Int32` (i32), `Short` (i16).
    public boolean isIntegral() { return tag == 1 || tag == 18 || tag == 7 || tag == 12 || tag == 13; }
    /// The integer value; refused for any other family.
    public long longValue() {
        return switch (tag) {
            case 1, 18 -> i64(valueStart);
            case 7, 12 -> i32(valueStart);
            case 13 -> i16(valueStart);
            default -> throw wrongFamily("an integer");
        };
    }

    /// Whether the payload is a float: `Float`, `Float32`, or `Half`.
    public boolean isFloating() { return tag == 2 || tag == 16 || tag == 17; }
    /// The float value, a `Half` widened from its binary16 bits.
    public double doubleValue() {
        return switch (tag) {
            case 2 -> Double.longBitsToDouble(i64(valueStart));
            case 16 -> Float.intBitsToFloat(i32(valueStart));
            case 17 -> halfToDouble(u16(valueStart));
            default -> throw wrongFamily("a float");
        };
    }

    public boolean isBoolean() { return tag == 3; }
    public boolean booleanValue() { if (tag != 3) throw wrongFamily("a boolean"); return data[valueStart] != 0; }

    /// Whether the payload is text: `Text`, `EnumStr`, `TextValidated`,
    /// `Ascii`, and the temporal trio `Date`, `Time`, `DateTime`.
    public boolean isText() { return tag == 0 || tag == 6 || tag == 10 || tag == 11 || tag == 20 || tag == 21 || tag == 22; }
    /// The text, without its length prefix.
    public String stringValue() { if (!isText()) throw wrongFamily("text"); return new String(data, valueStart + 4, (int) u32(valueStart), StandardCharsets.UTF_8); }

    /// Whether the payload is bytes: `Bytes` and `Varint` (length
    /// prefixed) or the three sixteen-byte identifiers.
    public boolean isBinary() { return tag == 4 || tag == 15 || tag == 23 || tag == 24 || tag == 25; }
    /// A copy of the bytes, without any length prefix.
    public byte[] bytesValue() {
        if (tag == 4 || tag == 15) return Arrays.copyOfRange(data, valueStart + 4, valueStart + 4 + (int) u32(valueStart));
        if (isBinary()) return Arrays.copyOfRange(data, valueStart, valueEnd);
        throw wrongFamily("bytes");
    }

    /// Whether the payload is a `Nanos` instant.
    public boolean isNanos() { return tag == 19; }
    /// The whole seconds of a `Nanos` instant.
    public long nanosSeconds() { if (tag != 19) throw wrongFamily("a nanos instant"); return i64(valueStart); }
    /// The sub-second adjustment of a `Nanos` instant. Two fields on
    /// the wire, read as two: the seconds alone would look right and
    /// lose this.
    public int nanosAdjust() { if (tag != 19) throw wrongFamily("a nanos instant"); return i32(valueStart + 8); }

    /// Whether the payload is a `Decimal`.
    public boolean isDecimal() { return tag == 14; }
    /// A `Decimal`'s scale; the value is the digits scaled by `10^-scale`.
    public int decimalScale() { if (tag != 14) throw wrongFamily("a decimal"); return i32(valueStart); }
    /// A `Decimal`'s unscaled big-integer digits, handed over rather
    /// than converted: the caller chooses the arbitrary-precision type.
    public byte[] decimalDigits() { if (tag != 14) throw wrongFamily("a decimal"); return Arrays.copyOfRange(data, valueStart + 8, valueStart + 8 + (int) u32(valueStart + 4)); }

    /// Whether this field is the null value.
    public boolean isNull() { return tag == 5; }

    /// The AST view of this field: its payload materialized as an
    /// [MValue], read exactly as [MNode#fromBytes] would read it. The
    /// bridge from a bound field to the node types, for a caller that
    /// wants structure rather than a primitive.
    public MValue value() {
        TypeTag type = typeTag();
        if (type == null) throw ScanException.invalidTag(tag);
        return MNode.readValue(valueBuffer(), type);
    }

    private IllegalStateException wrongFamily(String wanted) {
        return new IllegalStateException("field '" + name() + "' at " + index + " holds tag " + tag + ", not " + wanted);
    }

    private int u16(int at) { return (data[at] & 0xff) | ((data[at + 1] & 0xff) << 8); }
    private short i16(int at) { return (short) u16(at); }
    private int i32(int at) { return (data[at] & 0xff) | ((data[at + 1] & 0xff) << 8) | ((data[at + 2] & 0xff) << 16) | ((data[at + 3] & 0xff) << 24); }
    private long u32(int at) { return i32(at) & 0xffffffffL; }
    private long i64(int at) { return (i32(at) & 0xffffffffL) | ((long) i32(at + 4) << 32); }

    /// Widens an IEEE binary16 bit pattern. `Half` stores the bits, not
    /// a value, so this is the only correct way to read one as a number.
    static double halfToDouble(int bits) {
        double sign = (bits & 0x8000) != 0 ? -1.0 : 1.0;
        int exp = (bits >> 10) & 0x1F;
        double frac = bits & 0x03FF;
        if (exp == 0) return sign * frac * Math.pow(2, -24);
        if (exp == 31) return frac == 0 ? sign * Double.POSITIVE_INFINITY : Double.NaN;
        return sign * (1.0 + frac / 1024.0) * Math.pow(2, exp - 15);
    }

    @Override public String toString() { return "Field[" + index + " " + name() + " tag=" + tag + " bytes=" + valueLength() + "]"; }
}
