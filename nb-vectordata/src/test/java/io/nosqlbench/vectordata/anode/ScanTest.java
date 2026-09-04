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
import io.nosqlbench.vectordata.anode.PNode.ConjugateType;
import io.nosqlbench.vectordata.anode.PNode.FieldRef;
import io.nosqlbench.vectordata.anode.PNode.OpType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// The raw walk over MNode bytes: every tag skipped at its true width,
/// every accessor typed by the wire form, the leader byte respected,
/// and a malformed record stopped rather than misread.
@Tag("unit")
class ScanTest {
    static byte[] sixteen(int fill) { byte[] b = new byte[16]; Arrays.fill(b, (byte) fill); return b; }

    /// One field of every value kind an MNode can carry, in a fixed order.
    static MNode everyKind() {
        return new MNode()
            .insert("text", new MValue.Text("héllo"))
            .insert("int", new MValue.Int(-7L))
            .insert("float", new MValue.Float(2.5))
            .insert("bool", new MValue.Bool(true))
            .insert("bytes", new MValue.Bytes(new byte[] {1, 2, 3}))
            .insert("null", MValue.NULL)
            .insert("enum_str", new MValue.EnumStr("red"))
            .insert("enum_ord", new MValue.EnumOrd(3))
            .insert("list", new MValue.ListValue(List.of(new MValue.Int(1), new MValue.Text("a"))))
            .insert("map", new MValue.MapValue(new MNode().insert("k", new MValue.Int(9))))
            .insert("ascii", new MValue.Ascii("abc"))
            .insert("int32", new MValue.Int32(65536))
            .insert("short", new MValue.Short((short) -2))
            .insert("float32", new MValue.Float32(1.5f))
            .insert("half", new MValue.Half(0x3C00))
            .insert("millis", new MValue.Millis(1_700_000_000_000L))
            .insert("nanos", new MValue.Nanos(1_700_000_000L, 123))
            .insert("date", new MValue.Date("2026-01-02"))
            .insert("time", new MValue.Time("03:04:05"))
            .insert("datetime", new MValue.DateTime("2026-01-02T03:04:05Z"))
            .insert("uuid1", new MValue.UuidV1(sixteen(1)))
            .insert("uuid7", new MValue.UuidV7(sixteen(7)))
            .insert("ulid", new MValue.Ulid(sixteen(9)))
            .insert("array", new MValue.ArrayValue(TypeTag.INT32, List.of(new MValue.Int32(1), new MValue.Int32(2))))
            .insert("set", new MValue.SetValue(List.of(new MValue.Text("x"))))
            .insert("typed_map", new MValue.TypedMap(List.of(new MValue.TypedMap.Entry(new MValue.Text("k"), new MValue.Int(1)))));
    }

    static Map<String, Field> byName(byte[] record) {
        Map<String, Field> out = new java.util.LinkedHashMap<>();
        for (Field f : Scan.fields(record)) out.put(f.name(), f);
        return out;
    }

    /// Every value kind is skipped at its true width — the walk lands
    /// exactly on the end of the record — and each field's in-place
    /// view materializes back to the value that was written.
    @Test void everyValueKindWalksInPlace() {
        MNode node = everyKind();
        byte[] bytes = node.toBytes();
        Scan.Fields fields = Scan.fields(bytes);
        List<String> names = new ArrayList<>();
        int index = 0;
        for (Field f : fields) {
            names.add(f.name());
            assertEquals(index++, f.index());
            MValue original = node.get(f.name());
            assertEquals(original.tag().code(), f.tag(), f.name());
            assertEquals(original.tag(), f.typeTag());
            assertEquals(original.display(), f.value().display(), "AST bridge for " + f.name());
        }
        assertEquals(new ArrayList<>(node.fields().keySet()), names, "wire order");
        assertEquals(bytes.length, fields.position(), "the walk consumed exactly the record");
        assertEquals(26, names.size());
    }

    /// Accessors are typed by the wire form: integers, floats (a Half
    /// widened from its bits), text (the temporal trio included),
    /// bytes (identifiers unprefixed), a Nanos as two numbers.
    @Test void accessorsReadEachFamilyInPlace() {
        Map<String, Field> f = byName(everyKind().toBytes());
        assertTrue(f.get("int").isIntegral()); assertEquals(-7L, f.get("int").longValue());
        assertEquals(65536L, f.get("int32").longValue());
        assertEquals(-2L, f.get("short").longValue());
        assertEquals(3L, f.get("enum_ord").longValue());
        assertEquals(1_700_000_000_000L, f.get("millis").longValue());
        assertTrue(f.get("float").isFloating()); assertEquals(2.5, f.get("float").doubleValue());
        assertEquals(1.5, f.get("float32").doubleValue());
        assertEquals(1.0, f.get("half").doubleValue(), "0x3C00 is 1.0, not 15360");
        assertFalse(f.get("half").isIntegral(), "a half is a float on the wire");
        assertTrue(f.get("bool").booleanValue());
        assertEquals("héllo", f.get("text").stringValue());
        assertEquals("abc", f.get("ascii").stringValue());
        assertEquals("red", f.get("enum_str").stringValue());
        assertEquals("2026-01-02", f.get("date").stringValue());
        assertEquals("03:04:05", f.get("time").stringValue());
        assertEquals("2026-01-02T03:04:05Z", f.get("datetime").stringValue());
        assertFalse(f.get("datetime").isIntegral(), "a datetime is text on the wire");
        assertArrayEquals(new byte[] {1, 2, 3}, f.get("bytes").bytesValue());
        assertArrayEquals(sixteen(1), f.get("uuid1").bytesValue());
        assertArrayEquals(sixteen(7), f.get("uuid7").bytesValue());
        assertArrayEquals(sixteen(9), f.get("ulid").bytesValue());
        assertTrue(f.get("nanos").isNanos());
        assertEquals(1_700_000_000L, f.get("nanos").nanosSeconds());
        assertEquals(123, f.get("nanos").nanosAdjust());
        assertTrue(f.get("null").isNull());
        assertEquals(0, f.get("null").valueLength());
        assertThrows(IllegalStateException.class, () -> f.get("text").longValue(), "the wrong family is refused, not misread");
        assertThrows(IllegalStateException.class, () -> f.get("half").longValue());
    }

    /// The three tags the node types read as something else — Decimal,
    /// Varint, TextValidated — are skipped at their own widths and read
    /// through their own accessors, from a hand-built record.
    @Test void decimalVarintAndValidatedTextAreWalkedFromTheWire() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(MNode.DIALECT); Wire.u16(out, 3);
        Wire.u16(out, 1); out.write('d'); out.write(14); Wire.i32(out, 2); Wire.u32(out, 1); out.write(7);
        Wire.u16(out, 1); out.write('v'); out.write(15); Wire.u32(out, 2); out.write(1); out.write(2);
        Wire.u16(out, 1); out.write('t'); out.write(10); Wire.u32(out, 2); out.write('h'); out.write('i');
        byte[] record = out.toByteArray();
        Map<String, Field> f = byName(record);
        assertTrue(f.get("d").isDecimal());
        assertEquals(2, f.get("d").decimalScale());
        assertArrayEquals(new byte[] {7}, f.get("d").decimalDigits());
        assertEquals(TypeTag.DECIMAL, f.get("d").typeTag());
        assertTrue(f.get("v").isBinary());
        assertArrayEquals(new byte[] {1, 2}, f.get("v").bytesValue());
        assertTrue(f.get("t").isText());
        assertEquals("hi", f.get("t").stringValue());
        assertEquals(TypeTag.TEXT_VALIDATED, f.get("t").typeTag());
        Scan.Fields walk = Scan.fields(record);
        while (walk.hasNext()) walk.next();
        assertEquals(record.length, walk.position(), "each width was right");
        ScanException bad = assertThrows(ScanException.class, () -> Scan.skipValue(record, 5, 99));
        assertEquals(ScanException.Kind.INVALID_TAG, bad.kind());
        assertEquals(99, bad.value());
    }

    /// A record says what it is: a PNode is refused by its own leader.
    @Test void aPredicateIsRefusedByItsLeaderByte() {
        PNode p = new PNode.Predicate(new FieldRef.Named("a"), OpType.EQ, List.of(new Comparand.Int(1)));
        ScanException e = assertThrows(ScanException.class, () -> Scan.fields(p.toBytesNamed()));
        assertEquals(ScanException.Kind.INVALID_DIALECT, e.kind());
        assertEquals(PNode.DIALECT, e.value());
    }

    /// A malformed record cannot be walked past: the fault is reported
    /// once and the walk ends, rather than once per declared field.
    @Test void aTruncatedRecordStopsTheWalk() {
        assertEquals(ScanException.Kind.UNEXPECTED_EOF, assertThrows(ScanException.class, () -> Scan.fields(new byte[0])).kind());
        assertEquals(ScanException.Kind.UNEXPECTED_EOF, assertThrows(ScanException.class, () -> Scan.fields(new byte[] {1, 5})).kind());
        byte[] whole = everyKind().toBytes();
        byte[] cut = Arrays.copyOf(whole, whole.length - 2);
        Scan.Fields fields = Scan.fields(cut);
        int walked = 0;
        ScanException e = null;
        while (fields.hasNext()) {
            try { fields.next(); walked++; }
            catch (ScanException fault) { e = fault; }
        }
        assertNotNull(e, "the cut field faulted");
        assertEquals(ScanException.Kind.UNEXPECTED_EOF, e.kind());
        assertEquals(25, walked, "every whole field before it was read");
        assertFalse(fields.hasNext(), "and the walk ended there");
    }

    /// The schema a sample record carries: names and tags in wire order.
    @Test void aSchemaIsDiscoveredFromOneRecord() {
        Scan.RecordSchema schema = Scan.discoverSchema(new MNode().insert("id", new MValue.Int(1)).insert("tag", new MValue.Text("a")).toBytes());
        assertEquals(List.of("id", "tag"), schema.fieldNames());
        assertEquals(List.of(TypeTag.INT.code(), TypeTag.TEXT.code()), schema.tags());
        assertEquals(2, schema.fieldCount());
    }

    static PNode pred(String field, OpType op, Comparand c) { return new PNode.Predicate(new FieldRef.Named(field), op, List.of(c)); }
    static PNode and(PNode... children) { return new PNode.Conjugate(ConjugateType.AND, List.of(children)); }

    /// A flat conjunction flattens, nested ANDs included; an OR anywhere
    /// or an indexed field leaves the predicate without a flat list.
    @Test void onlyAFlatConjunctionFlattens() {
        List<Scan.FlatCondition> one = Scan.flattenAnd(pred("a", OpType.GT, new Comparand.Int(1)));
        assertEquals(1, one.size());
        assertEquals("a", one.get(0).field());
        List<Scan.FlatCondition> nested = Scan.flattenAnd(and(pred("a", OpType.EQ, new Comparand.Int(1)), and(pred("b", OpType.LT, new Comparand.Float(2.0)), pred("c", OpType.IN, new Comparand.Text("x")))));
        assertEquals(List.of("a", "b", "c"), nested.stream().map(Scan.FlatCondition::field).toList());
        assertEquals(OpType.IN, nested.get(2).op());
        PNode or = new PNode.Conjugate(ConjugateType.OR, List.of(pred("a", OpType.EQ, new Comparand.Int(1)), pred("a", OpType.EQ, new Comparand.Int(2))));
        assertNull(Scan.flattenAnd(or));
        assertNull(Scan.flattenAnd(and(pred("a", OpType.EQ, new Comparand.Int(1)), or)), "an OR anywhere");
        assertNull(Scan.flattenAnd(new PNode.Predicate(new FieldRef.Index(0), OpType.EQ, List.of(new Comparand.Int(1)))), "an indexed field");
    }

    /// The evaluator's rule for an absent field, stated once.
    @Test void aMissingFieldPassesOnlyNullComparisons() {
        assertTrue(Scan.missingFieldPasses(OpType.EQ, List.of(Comparand.NULL)));
        assertTrue(Scan.missingFieldPasses(OpType.IN, List.of(new Comparand.Int(1), Comparand.NULL)));
        assertTrue(Scan.missingFieldPasses(OpType.NE, List.of(new Comparand.Int(5))));
        assertFalse(Scan.missingFieldPasses(OpType.NE, List.of(Comparand.NULL)));
        assertFalse(Scan.missingFieldPasses(OpType.EQ, List.of(new Comparand.Int(5))));
        assertFalse(Scan.missingFieldPasses(OpType.GT, List.of(Comparand.NULL)));
    }
}
