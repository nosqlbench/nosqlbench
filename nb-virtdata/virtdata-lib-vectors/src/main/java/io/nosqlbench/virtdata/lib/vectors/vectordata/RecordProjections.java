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


import io.nosqlbench.vectordata.anode.Field;
import io.nosqlbench.vectordata.anode.MNode;
import io.nosqlbench.vectordata.anode.MValue;
import io.nosqlbench.vectordata.anode.PNode.Comparand;
import io.nosqlbench.vectordata.anode.TypeTag;
import io.nosqlbench.vectordata.binding.BindType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/// Projects bound values to the Java types a driver binds: what a value
/// **is**, by its bind type, rather than how a vernacular would print
/// it. Integers by width (`Short`, `Integer`, `Long`), floats by width
/// (`Float` for binary16 and binary32, `Double`), text families as
/// `String`, the numeric timestamps as [Instant], the UUIDs as
/// [UUID], a ULID and a blob as `byte[]`, and containers as
/// insertion-ordered lists, sets, and maps of the same projections.
///
/// Varint and decimal payloads are projected as [BigInteger] and
/// [BigDecimal] on the assumption that they carry big-endian two's
/// complement digits — the encoding the CQL types they are named after
/// use. The reference reads both as opaque bytes and states no
/// convention; a dataset written otherwise would need its own
/// projection.
public final class RecordProjections {
    private RecordProjections() { }

    /// A bound field as a Java value, read in place where the family
    /// allows and through the AST view for containers.
    public static Object of(Field field) {
        TypeTag tag = field.typeTag();
        if (tag == null) { ByteBuffer raw = field.valueBuffer(); byte[] copy = new byte[raw.remaining()]; raw.get(copy); return copy; }
        return switch (tag) {
            case NULL -> null;
            case SHORT -> (short) field.longValue();
            case INT32, ENUM_ORD -> (int) field.longValue();
            case INT -> field.longValue();
            case HALF, FLOAT32 -> (float) field.doubleValue();
            case FLOAT -> field.doubleValue();
            case BOOL -> field.booleanValue();
            case TEXT, TEXT_VALIDATED, ENUM_STR, ASCII, DATE, TIME, DATETIME -> field.stringValue();
            case BYTES, ULID -> field.bytesValue();
            case VARINT -> bigInteger(field.bytesValue());
            case DECIMAL -> new BigDecimal(bigInteger(field.decimalDigits()), field.decimalScale());
            case MILLIS -> Instant.ofEpochMilli(field.longValue());
            case NANOS -> Instant.ofEpochSecond(field.nanosSeconds(), field.nanosAdjust());
            case UUID_V1, UUID_V7 -> uuid(field.bytesValue());
            case LIST, ARRAY, SET, MAP, TYPED_MAP -> of(field.value());
        };
    }

    /// A node value as a Java value, recursively.
    public static Object of(MValue value) {
        return switch (value.tag()) {
            case NULL -> null;
            case TEXT -> ((MValue.Text) value).value();
            case ENUM_STR -> ((MValue.EnumStr) value).value();
            case ASCII -> ((MValue.Ascii) value).value();
            case DATE -> ((MValue.Date) value).value();
            case TIME -> ((MValue.Time) value).value();
            case DATETIME -> ((MValue.DateTime) value).value();
            case INT -> ((MValue.Int) value).value();
            case INT32 -> ((MValue.Int32) value).value();
            case ENUM_ORD -> ((MValue.EnumOrd) value).value();
            case SHORT -> ((MValue.Short) value).value();
            case FLOAT -> ((MValue.Float) value).value();
            case FLOAT32 -> ((MValue.Float32) value).value();
            case HALF -> Float.float16ToFloat((short) ((MValue.Half) value).bits());
            case BOOL -> ((MValue.Bool) value).value();
            case BYTES -> ((MValue.Bytes) value).value();
            case MILLIS -> Instant.ofEpochMilli(((MValue.Millis) value).value());
            case NANOS -> { MValue.Nanos n = (MValue.Nanos) value; yield Instant.ofEpochSecond(n.epochSeconds(), n.nanoAdjust()); }
            case UUID_V1 -> uuid(((MValue.UuidV1) value).value());
            case UUID_V7 -> uuid(((MValue.UuidV7) value).value());
            case ULID -> ((MValue.Ulid) value).value();
            case LIST -> list(((MValue.ListValue) value).items());
            case ARRAY -> list(((MValue.ArrayValue) value).items());
            case SET -> { Set<Object> out = new LinkedHashSet<>(); for (MValue item : ((MValue.SetValue) value).items()) out.add(of(item)); yield out; }
            case MAP -> map(((MValue.MapValue) value).node());
            case TYPED_MAP -> { Map<Object, Object> out = new LinkedHashMap<>(); for (MValue.TypedMap.Entry e : ((MValue.TypedMap) value).entries()) out.put(of(e.key()), of(e.value())); yield out; }
            case TEXT_VALIDATED, DECIMAL, VARINT -> throw new IllegalStateException("no node value carries tag " + value.tag());
        };
    }

    /// A record as an insertion-ordered map of projected values.
    public static Map<String, Object> map(MNode node) {
        Map<String, Object> out = new LinkedHashMap<>();
        node.fields().forEach((name, value) -> out.put(name, of(value)));
        return out;
    }

    /// A predicate comparand as a Java value, coerced toward the
    /// **field's** bind type where its own variant allows: an `Int`
    /// comparand against a `TIMESTAMP_MILLIS` field becomes an
    /// [Instant], against an `INT32` field an `Integer`; a `Bytes`
    /// comparand against a `UUID` field becomes a [UUID]. A comparand
    /// the type cannot claim keeps its natural projection.
    public static Object of(Comparand comparand, BindType type) {
        BindType.Kind kind = type == null ? BindType.Kind.NULL : type.kind();
        if (comparand instanceof Comparand.Null) return null;
        if (comparand instanceof Comparand.Int i) {
            return switch (kind) {
                case INT16 -> (short) i.value();
                case INT32 -> (int) i.value();
                case FLOAT16, FLOAT32 -> (float) i.value();
                case FLOAT64 -> (double) i.value();
                case TIMESTAMP_MILLIS -> Instant.ofEpochMilli(i.value());
                case TIMESTAMP_NANOS -> Instant.ofEpochSecond(i.value());
                case VARINT -> BigInteger.valueOf(i.value());
                case DECIMAL -> BigDecimal.valueOf(i.value());
                default -> i.value();
            };
        }
        if (comparand instanceof Comparand.Float f) {
            return switch (kind) {
                case FLOAT16, FLOAT32 -> (float) f.value();
                case DECIMAL -> BigDecimal.valueOf(f.value());
                default -> f.value();
            };
        }
        if (comparand instanceof Comparand.Text t) return t.value();
        if (comparand instanceof Comparand.Bool b) return b.value();
        byte[] bytes = ((Comparand.Bytes) comparand).value();
        return switch (kind) {
            case UUID, TIME_UUID -> bytes.length == 16 ? uuid(bytes) : bytes;
            case VARINT -> bigInteger(bytes);
            default -> bytes;
        };
    }

    private static List<Object> list(List<MValue> items) { List<Object> out = new ArrayList<>(items.size()); for (MValue item : items) out.add(of(item)); return out; }

    private static BigInteger bigInteger(byte[] twosComplement) { return twosComplement.length == 0 ? BigInteger.ZERO : new BigInteger(twosComplement); }

    private static UUID uuid(byte[] sixteen) { ByteBuffer b = ByteBuffer.wrap(sixteen); return new UUID(b.getLong(), b.getLong()); }
}
