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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// The stage-1 wire codecs, mirrored from the reference's `mnode`,
/// `pnode`, and `anode` unit tests: every tag round-trips, both PNode
/// wire modes and the legacy named form decode, and the dialect leader
/// is what tells the two apart.
@Tag("unit")
class ANodeWireTest {

    static PNode pred(String field, OpType op, Comparand... cs) { return new PNode.Predicate(new FieldRef.Named(field), op, List.of(cs)); }

    // -- MNode --

    @Test void anMNodeRoundTrips() {
        MNode node = new MNode().insert("name", new MValue.Text("alice")).insert("age", new MValue.Int(30))
            .insert("score", new MValue.Float(99.5)).insert("active", new MValue.Bool(true)).insert("empty", MValue.NULL);
        byte[] bytes = node.toBytes();
        assertEquals(MNode.DIALECT, bytes[0]);
        assertEquals(node, MNode.fromBytes(bytes));
    }

    @Test void aFramedMNodeRoundTrips() {
        MNode node = new MNode().insert("x", new MValue.Int32(42)).insert("y", new MValue.Float32(3.25f));
        assertEquals(node, MNode.fromFramed(ByteBuffer.wrap(node.encodeFramed())));
    }

    @Test void aNestedMNodeRoundTrips() {
        MNode inner = new MNode().insert("key", new MValue.Text("val"));
        MNode outer = new MNode().insert("nested", new MValue.MapValue(inner))
            .insert("list", new MValue.ListValue(List.of(new MValue.Int(1), new MValue.Text("two"))));
        assertEquals(outer, MNode.fromBytes(outer.toBytes()));
    }

    @Test void everyTagRoundTrips() {
        byte[] sixteen = new byte[16];
        for (int i = 0; i < 16; i++) sixteen[i] = (byte) (i * 17);
        MNode node = new MNode()
            .insert("text", new MValue.Text("t")).insert("int", new MValue.Int(-7)).insert("float", new MValue.Float(2.5))
            .insert("bool", new MValue.Bool(true)).insert("bytes", new MValue.Bytes(new byte[] {1, 2, 3})).insert("null", MValue.NULL)
            .insert("enum_str", new MValue.EnumStr("red")).insert("enum_ord", new MValue.EnumOrd(3))
            .insert("list", new MValue.ListValue(List.of(new MValue.Int(1), new MValue.Bool(false))))
            .insert("map", new MValue.MapValue(new MNode().insert("k", new MValue.Int32(9))))
            .insert("ascii", new MValue.Ascii("abc")).insert("int32", new MValue.Int32(-2)).insert("short", new MValue.Short((short) -3))
            .insert("float32", new MValue.Float32(1.5f)).insert("half", new MValue.Half(0x3c00)).insert("millis", new MValue.Millis(1234567890123L))
            .insert("nanos", new MValue.Nanos(1700000000L, 999)).insert("date", new MValue.Date("2026-09-04")).insert("time", new MValue.Time("12:34:56"))
            .insert("datetime", new MValue.DateTime("2026-09-04T12:34:56Z")).insert("uuid_v1", new MValue.UuidV1(sixteen))
            .insert("uuid_v7", new MValue.UuidV7(sixteen)).insert("ulid", new MValue.Ulid(sixteen))
            .insert("array", new MValue.ArrayValue(TypeTag.INT32, List.of(new MValue.Int32(1), new MValue.Int32(2))))
            .insert("set", new MValue.SetValue(List.of(new MValue.Text("a"), new MValue.Text("b"))))
            .insert("typed_map", new MValue.TypedMap(List.of(new MValue.TypedMap.Entry(new MValue.Text("k"), new MValue.Int(1)))));
        MNode decoded = MNode.fromBytes(node.toBytes());
        assertEquals(node, decoded);
        assertEquals(26, decoded.size());
        for (var field : decoded.fields().entrySet()) assertEquals(field.getKey(), field.getValue().tag().label(), "each field is named by its tag");
    }

    @Test void decimalVarintAndValidatedTextDecodeToTheirNearestVariants() {
        ByteBuffer bytes = ByteBuffer.allocate(64).order(ByteOrder.LITTLE_ENDIAN);
        bytes.put((byte) MNode.DIALECT).putShort((short) 3);
        bytes.putShort((short) 1).put((byte) 'd').put((byte) TypeTag.DECIMAL.code()).putInt(2).putInt(2).put((byte) 0x12).put((byte) 0x34);
        bytes.putShort((short) 1).put((byte) 'v').put((byte) TypeTag.VARINT.code()).putInt(1).put((byte) 0x7f);
        bytes.putShort((short) 1).put((byte) 't').put((byte) TypeTag.TEXT_VALIDATED.code()).putInt(2).put((byte) 'o').put((byte) 'k');
        MNode node = MNode.fromBytes(java.util.Arrays.copyOf(bytes.array(), bytes.position()));
        assertEquals(new MValue.Bytes(new byte[] {0x12, 0x34}), node.get("d"), "a decimal keeps its digits and drops its scale");
        assertEquals(new MValue.Bytes(new byte[] {0x7f}), node.get("v"));
        assertEquals(new MValue.Text("ok"), node.get("t"));
    }

    @Test void anMNodeDisplays() {
        String text = new MNode().insert("name", new MValue.Text("bob")).insert("age", new MValue.Int(25)).display();
        assertTrue(text.contains("name: 'bob'")); assertTrue(text.contains("age: 25"));
    }

    @Test void fingerprintsStripValuesAndKeepShape() {
        assertEquals(new MValue.Text(""), new MValue.Text("hello").fingerprint());
        assertEquals(new MValue.Int(0), new MValue.Int(42).fingerprint());
        assertEquals(new MValue.Float(0.0), new MValue.Float(3.25).fingerprint());
        assertEquals(new MValue.Bool(false), new MValue.Bool(true).fingerprint());
        assertEquals(MValue.NULL, MValue.NULL.fingerprint());
        MNode a = new MNode().insert("name", new MValue.Text("alice")).insert("age", new MValue.Int(30));
        MNode b = new MNode().insert("name", new MValue.Text("bob")).insert("age", new MValue.Int(25));
        MNode c = new MNode().insert("name", new MValue.Text("carol")).insert("score", new MValue.Float(99.0));
        assertTrue(a.isCongruent(b)); assertEquals(a.fingerprint(), b.fingerprint()); assertFalse(a.isCongruent(c));
        MNode nested = new MNode().insert("nested", new MValue.MapValue(new MNode().insert("k", new MValue.Int(999))))
            .insert("items", new MValue.ListValue(List.of(new MValue.Text("x"))));
        MNode fp = nested.fingerprint();
        assertEquals(new MValue.Int(0), ((MValue.MapValue) fp.get("nested")).node().get("k"));
        assertEquals(new MValue.Text(""), ((MValue.ListValue) fp.get("items")).items().get(0));
    }

    @Test void badMNodeBytesAreRefused() {
        assertThrows(ANodeException.class, () -> MNode.fromBytes(new byte[0]));
        assertThrows(ANodeException.class, () -> MNode.fromBytes(new byte[] {0x02, 0, 0}), "a PNode leader is not an MNode");
        assertThrows(ANodeException.class, () -> MNode.fromBytes(new byte[] {0x01, 1, 0, 1, 0, 'a', 29}), "tag 29 names nothing");
        assertThrows(ANodeException.class, () -> MNode.fromBytes(new byte[] {0x01, 1, 0, 1, 0, 'a', 1, 0}), "a truncated i64");
        assertNull(TypeTag.fromCode(29)); assertNull(TypeTag.fromCode(255));
        for (int i = 0; i <= 28; i++) assertEquals(i, TypeTag.fromCode(i).code());
    }

    // -- PNode --

    @Test void anIndexedPredicateRoundTrips() {
        PNode node = new PNode.Predicate(new FieldRef.Index(3), OpType.EQ, List.of(new Comparand.Int(42)));
        assertEquals(node, PNode.fromBytesIndexed(node.toBytesIndexed()));
        assertEquals(14, node.toBytesIndexed().length, "leader + pred + index + op + count + one i64");
    }

    @Test void aNamedPredicateRoundTrips() {
        PNode node = pred("color", OpType.IN, new Comparand.Int(1), new Comparand.Int(2), new Comparand.Int(3));
        assertEquals(node, PNode.fromBytesNamed(node.toBytesNamed()));
    }

    @Test void aConjugateTreeRoundTrips() {
        PNode tree = new PNode.Conjugate(ConjugateType.AND, List.of(
            new PNode.Predicate(new FieldRef.Index(0), OpType.GT, List.of(new Comparand.Int(10))),
            new PNode.Conjugate(ConjugateType.OR, List.of(
                new PNode.Predicate(new FieldRef.Index(1), OpType.EQ, List.of(new Comparand.Int(5))),
                new PNode.Predicate(new FieldRef.Index(2), OpType.LE, List.of(new Comparand.Int(100)))))));
        assertEquals(tree, PNode.fromBytesIndexed(tree.toBytesIndexed()));
        String text = new PNode.Conjugate(ConjugateType.AND, List.of(pred("age", OpType.GT, new Comparand.Int(18)), pred("score", OpType.LE, new Comparand.Int(100)))).display();
        assertTrue(text.contains("AND")); assertTrue(text.contains("age > 18")); assertTrue(text.contains("score <= 100"));
    }

    @Test void everyComparandTypeRoundTripsInTypedNamedMode() {
        PNode tree = new PNode.Conjugate(ConjugateType.AND, List.of(
            pred("age", OpType.GE, new Comparand.Int(18)), pred("score", OpType.LT, new Comparand.Float(99.5)),
            pred("name", OpType.EQ, new Comparand.Text("alice")), pred("active", OpType.EQ, new Comparand.Bool(true)),
            pred("data", OpType.EQ, new Comparand.Bytes(new byte[] {(byte) 0xDE, (byte) 0xAD})), pred("empty", OpType.EQ, Comparand.NULL)));
        byte[] bytes = tree.toBytesNamed();
        assertEquals(PNode.DIALECT, bytes[0]); assertEquals((byte) PNode.TYPED_MARKER, bytes[1]);
        assertEquals(tree, PNode.fromBytesNamed(bytes));
    }

    @Test void theLegacyNamedFormatStillDecodes() {
        ByteBuffer legacy = ByteBuffer.allocate(32).order(ByteOrder.LITTLE_ENDIAN);
        legacy.put((byte) PNode.DIALECT).put((byte) 0).putShort((short) 1).put((byte) 'x').put((byte) 2).putShort((short) 1).putLong(42);
        PNode decoded = PNode.fromBytesNamed(java.util.Arrays.copyOf(legacy.array(), legacy.position()));
        assertEquals(pred("x", OpType.EQ, new Comparand.Int(42)), decoded);
    }

    @Test void indexedModeCarriesOnlyIntegers() {
        PNode node = new PNode.Predicate(new FieldRef.Index(0), OpType.EQ, List.of(new Comparand.Text("nope")));
        assertThrows(ANodeException.class, node::toBytesIndexed);
        assertThrows(ANodeException.class, () -> pred("x", OpType.EQ, new Comparand.Int(1)).toBytesIndexed(), "a named field has no index");
    }

    @Test void pnodeFingerprintsAndCongruence() {
        assertEquals(pred("age", OpType.GT, new Comparand.Int(0)), pred("age", OpType.GT, new Comparand.Int(42)).fingerprint());
        PNode a = new PNode.Conjugate(ConjugateType.AND, List.of(pred("x", OpType.EQ, new Comparand.Text("hello")), pred("y", OpType.LT, new Comparand.Int(100))));
        PNode b = new PNode.Conjugate(ConjugateType.AND, List.of(pred("x", OpType.EQ, new Comparand.Text("world")), pred("y", OpType.LT, new Comparand.Int(999))));
        assertTrue(a.isCongruent(b));
        assertFalse(a.isCongruent(pred("x", OpType.EQ, new Comparand.Text("hello"))));
        assertEquals(pred("f", OpType.IN, new Comparand.Text(""), new Comparand.Text("")), pred("f", OpType.IN, new Comparand.Text("a"), new Comparand.Text("b")).fingerprint());
    }

    @Test void comparandsDisplayAsTheReferenceDoes() {
        assertEquals("42", new Comparand.Int(42).display());
        assertEquals("3.25", new Comparand.Float(3.25).display());
        assertEquals("1.0", new Comparand.Float(1.0).display(), "a whole float still says it is a float");
        assertEquals("'hello'", new Comparand.Text("hello").display());
        assertEquals("true", new Comparand.Bool(true).display());
        assertEquals("X'cafe'", new Comparand.Bytes(new byte[] {(byte) 0xCA, (byte) 0xFE}).display());
        assertEquals("NULL", Comparand.NULL.display());
    }

    // -- ANode --

    @Test void theLeaderByteSelectsTheDialect() {
        assertEquals(0x00, ANode.DIALECT_INVALID); assertEquals(0x01, ANode.DIALECT_MNODE); assertEquals(0x02, ANode.DIALECT_PNODE);
        MNode m = new MNode().insert("x", new MValue.Int(42));
        assertInstanceOf(ANode.M.class, ANode.decode(m.toBytes()));
        assertEquals(new MValue.Int(42), ((ANode.M) ANode.decode(m.toBytes())).node().get("x"));
        PNode p = pred("age", OpType.GT, new Comparand.Int(18));
        assertEquals(p, ((ANode.P) ANode.decode(p.toBytesNamed())).node());
        assertThrows(ANodeException.class, () -> ANode.decode(new byte[] {(byte) 0xFF, 0, 1}));
        assertThrows(ANodeException.class, () -> ANode.decode(new byte[0]));
    }

    @Test void encodeAndDecodeAreInverses() {
        ANode m = ANode.of(new MNode().insert("name", new MValue.Text("alice")).insert("score", new MValue.Float(99.5)));
        assertEquals(m, ANode.decode(ANode.encode(m)));
        ANode p = ANode.of(new PNode.Conjugate(ConjugateType.AND, List.of(pred("x", OpType.EQ, new Comparand.Int(1)), pred("y", OpType.LT, new Comparand.Int(10)))));
        assertEquals(p, ANode.decode(ANode.encode(p)));
        assertTrue(ANode.of(new MNode().insert("k", new MValue.Int(1))).display().contains("k: 1"));
    }

    @Test void anodeCongruenceIsWithinAKind() {
        ANode a = ANode.of(new MNode().insert("name", new MValue.Text("alice")).insert("age", new MValue.Int(30)));
        ANode b = ANode.of(new MNode().insert("name", new MValue.Text("bob")).insert("age", new MValue.Int(25)));
        assertTrue(a.isCongruent(b)); assertEquals(a.fingerprint().display(), b.fingerprint().display());
        assertTrue(ANode.of(pred("x", OpType.EQ, new Comparand.Int(1))).isCongruent(ANode.of(pred("x", OpType.EQ, new Comparand.Int(999)))));
        assertFalse(ANode.of(new MNode().insert("x", new MValue.Int(1))).isCongruent(ANode.of(pred("x", OpType.EQ, new Comparand.Int(1)))));
    }
}
