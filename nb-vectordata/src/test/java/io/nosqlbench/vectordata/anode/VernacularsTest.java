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

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// Stage 2, mirrored from the reference's vernacular tests: every
/// rendering, every parser that reads its own output back, and the
/// exact strings the SQL/CQL/CDDL adapters emit.
@Tag("unit")
class VernacularsTest {

    static MNode sampleMNode() {
        return new MNode().insert("name", new MValue.Text("alice")).insert("age", new MValue.Int(30))
            .insert("score", new MValue.Float(99.5)).insert("active", new MValue.Bool(true)).insert("empty", MValue.NULL);
    }

    static PNode pred(String field, OpType op, Comparand... cs) { return new PNode.Predicate(new FieldRef.Named(field), op, List.of(cs)); }

    static PNode samplePNode() {
        return new PNode.Conjugate(ConjugateType.AND, List.of(pred("age", OpType.GT, new Comparand.Int(18)),
            pred("status", OpType.IN, new Comparand.Int(1), new Comparand.Int(2), new Comparand.Int(3))));
    }

    static String render(MNode m, Vernacular v) { return Vernaculars.render(ANode.of(m), v); }
    static String render(PNode p, Vernacular v) { return Vernaculars.render(ANode.of(p), v); }
    static MNode parsed(String text, Vernacular v) { return ((ANode.M) Vernaculars.parse(text, v)).node(); }

    @Test void mnodeRenderings() {
        MNode m = sampleMNode();
        String cddl = render(m, Vernacular.CDDL);
        assertTrue(cddl.contains("name : tstr")); assertTrue(cddl.contains("age : int"));
        String sql = render(m, Vernacular.SQL);
        assertTrue(sql.contains("'alice'")); assertTrue(sql.contains("30")); assertTrue(sql.contains("TRUE"));
        assertEquals("('alice', 30, 99.5, TRUE, NULL)", sql);
        assertEquals(sql, render(m, Vernacular.SQLITE));
        String cql = render(m, Vernacular.CQL);
        assertTrue(cql.contains("'alice'")); assertTrue(cql.contains("true"));
        assertEquals("('alice', 30, 99.5, true, null)", cql);
        String jsonl = render(m, Vernacular.JSONL);
        assertFalse(jsonl.contains("\n")); assertTrue(jsonl.contains("\"name\":\"alice\""));
        assertEquals("{\"name\":\"alice\",\"age\":30,\"score\":99.5,\"active\":true,\"empty\":null}", jsonl);
        String yaml = render(m, Vernacular.YAML);
        assertTrue(yaml.contains("name: alice")); assertTrue(yaml.contains("age: 30"));
        String readout = render(m, Vernacular.READOUT);
        assertTrue(readout.contains("name")); assertTrue(readout.contains("'alice'")); assertTrue(readout.contains("30"));
        assertTrue(readout.startsWith("name   : 'alice'"), "names are padded to the widest: " + readout);
        assertTrue(render(m, Vernacular.DISPLAY).contains("name: 'alice'"));
        assertTrue(render(m, Vernacular.CDDL_VALUE).contains("name : \"alice\""));
    }

    @Test void jsonRoundTripsAndKeepsFieldOrder() {
        MNode m = sampleMNode();
        String json = render(m, Vernacular.JSON);
        assertTrue(json.contains("\"name\"")); assertTrue(json.contains("\"alice\"")); assertTrue(json.contains("\n"));
        MNode back = parsed(json, Vernacular.JSON);
        assertEquals(new MValue.Text("alice"), back.get("name"));
        assertEquals(new MValue.Int(30), back.get("age"));
        assertEquals(new MValue.Float(99.5), back.get("score"));
        assertEquals(new MValue.Bool(true), back.get("active"));
        assertEquals(MValue.NULL, back.get("empty"));
        assertEquals(List.of("name", "age", "score", "active", "empty"), List.copyOf(back.fields().keySet()), "objects keep their key order");
        assertArrayEquals(m.toBytes(), ANode.encode(Vernaculars.parse(json, Vernacular.JSON)), "so the reference's round-trip contract holds byte for byte");
        MNode nested = parsed("{\"a\": {\"b\": [1, 2.5, \"x\", null, true]}, \"e\": \"q\\\"uote\\n\"}", Vernacular.JSON);
        MValue.ListValue list = (MValue.ListValue) ((MValue.MapValue) nested.get("a")).node().get("b");
        assertEquals(List.of(new MValue.Int(1), new MValue.Float(2.5), new MValue.Text("x"), MValue.NULL, new MValue.Bool(true)), list.items());
        assertEquals(new MValue.Text("q\"uote\n"), nested.get("e"));
        assertThrows(ANodeException.class, () -> Vernaculars.parse("[1,2]", Vernacular.JSON), "JSON input must be an object");
        assertThrows(ANodeException.class, () -> Vernaculars.parse("{\"a\": }", Vernacular.JSON));
    }

    @Test void pnodeRenderings() {
        PNode p = samplePNode();
        String sql = render(p, Vernacular.SQL);
        assertTrue(sql.contains("age > 18")); assertTrue(sql.contains("status IN (1, 2, 3)")); assertTrue(sql.contains("AND"));
        assertEquals("(age > 18 AND status IN (1, 2, 3))", sql);
        assertEquals(sql, render(p, Vernacular.CQL));
        String json = render(p, Vernacular.JSON);
        assertTrue(json.contains("\"and\"")); assertTrue(json.contains("\"age\""));
        assertEquals("{\"type\":\"and\",\"children\":[{\"type\":\"predicate\",\"field\":\"age\",\"op\":\">\",\"value\":18},{\"type\":\"predicate\",\"field\":\"status\",\"op\":\"IN\",\"values\":[1,2,3]}]}",
            render(p, Vernacular.JSONL));
        String cddl = render(p, Vernacular.CDDL);
        assertTrue(cddl.contains("and")); assertTrue(cddl.contains("\"age\"")); assertTrue(cddl.contains("\"gt\""));
        assertEquals("type: and\nchildren:\n  -\n    field: age\n    op: >\n    value: 18\n  -\n    field: status\n    op: IN\n    values:\n      - 1\n      - 2\n      - 3", render(p, Vernacular.YAML));
        assertEquals("AND:\n\tage > 18\nAND\n\tstatus IN (1, 2, 3)", render(p, Vernacular.READOUT));
        assertEquals("(age > 18 AND status IN (1, 2, 3))", render(p, Vernacular.DISPLAY));
    }

    @Test void typedComparandsRenderPerDialect() {
        assertEquals("name = 'alice'", PNodeVernacular.toSql(pred("name", OpType.EQ, new Comparand.Text("alice"))));
        assertEquals("active = TRUE", PNodeVernacular.toSql(pred("active", OpType.EQ, new Comparand.Bool(true))));
        assertEquals("name = 'bob'", PNodeVernacular.toCql(pred("name", OpType.EQ, new Comparand.Text("bob"))));
        assertEquals("active = false", PNodeVernacular.toCql(pred("active", OpType.EQ, new Comparand.Bool(false))));
        assertEquals("(x < 5 OR x > 5)", PNodeVernacular.toCql(pred("x", OpType.NE, new Comparand.Int(5))), "CQL has no !=");
        assertEquals("x != 5", PNodeVernacular.toSql(pred("x", OpType.NE, new Comparand.Int(5))));
        assertEquals("s LIKE 'ab'", PNodeVernacular.toCql(pred("s", OpType.MATCHES, new Comparand.Text("ab"))));
        assertEquals("s ~ 'ab'", PNodeVernacular.toSql(pred("s", OpType.MATCHES, new Comparand.Text("ab"))));
        assertEquals("b = X'cafe'", PNodeVernacular.toSql(pred("b", OpType.EQ, new Comparand.Bytes(new byte[] {(byte) 0xCA, (byte) 0xFE}))));
        assertEquals("b = 0xcafe", PNodeVernacular.toCql(pred("b", OpType.EQ, new Comparand.Bytes(new byte[] {(byte) 0xCA, (byte) 0xFE}))));
        assertEquals("f > 1.0", PNodeVernacular.toSql(pred("f", OpType.GT, new Comparand.Float(1.0))));
        assertEquals("{ field: \"age\", op: \"gt\", value: 18 }", PNodeVernacular.toCddl(pred("age", OpType.GT, new Comparand.Int(18))));
    }

    @Test void mnodeAdaptersEmitTheReferenceStrings() {
        MNode node = new MNode().insert("name", new MValue.Text("alice")).insert("age", new MValue.Int(30)).insert("active", new MValue.Bool(true));
        assertEquals("('alice', 30, TRUE)", MNodeVernacular.toSql(node));
        MNode two = new MNode().insert("name", new MValue.Text("x")).insert("id", new MValue.Int(1));
        assertEquals("(\n  name TEXT,\n  id BIGINT\n)", MNodeVernacular.toSqlSchema(two));
        assertEquals("(\n  name text,\n  id bigint\n)", MNodeVernacular.toCqlSchema(two));
        assertTrue(MNodeVernacular.toCql(new MNode().insert("name", new MValue.Text("bob")).insert("score", new MValue.Float(99.5))).contains("99.5"));
        assertEquals("{\n  name : tstr,\n  count : int\n}", MNodeVernacular.toCddl(new MNode().insert("name", new MValue.Text("x")).insert("count", new MValue.Int32(42))));
        assertTrue(MNodeVernacular.toSql(new MNode().insert("val", new MValue.Text("it's a test"))).contains("it''s a test"));
        assertEquals("\"a b\" : tstr", MNodeVernacular.toCddl(new MNode().insert("a b", new MValue.Text(""))).trim().replace("{", "").replace("}", "").trim(), "a key that is not an identifier is quoted");
        MNode rich = new MNode().insert("d", new MValue.Date("2026-01-02")).insert("n", new MValue.Nanos(5, 7))
            .insert("l", new MValue.ListValue(List.of(new MValue.Int(1), new MValue.Int(2)))).insert("s", new MValue.SetValue(List.of(new MValue.Text("a"))))
            .insert("m", new MValue.MapValue(new MNode().insert("k", new MValue.Int(1))));
        assertEquals("(DATE '2026-01-02', TIMESTAMP '5.7', ARRAY[1, 2], ARRAY['a'], (1))", MNodeVernacular.toSql(rich));
        assertEquals("('2026-01-02', '5.7', [1, 2], {'a'}, {'k': 1})", MNodeVernacular.toCql(rich));
    }

    @Test void parsersReadTheirOwnFormats() {
        MNode sql = parsed("('alice', 42, TRUE, NULL)", Vernacular.SQL);
        assertEquals(new MValue.Text("alice"), sql.get("col_0")); assertEquals(new MValue.Int(42), sql.get("col_1"));
        assertEquals(new MValue.Bool(true), sql.get("col_2")); assertEquals(MValue.NULL, sql.get("col_3"));
        MNode cql = parsed("('bob', 99, true, null)", Vernacular.CQL);
        assertEquals(new MValue.Text("bob"), cql.get("col_0")); assertEquals(new MValue.Int(99), cql.get("col_1"));
        assertEquals(new MValue.Bool(true), cql.get("col_2")); assertEquals(MValue.NULL, cql.get("col_3"));
        assertEquals(new MValue.Text("it's"), parsed("('it''s', 1.5)", Vernacular.SQL).get("col_0"));
        assertEquals(new MValue.Float(1.5), parsed("('it''s', 1.5)", Vernacular.SQL).get("col_1"));
        MNode cddl = parsed("{ name : tstr, count : int }", Vernacular.CDDL);
        assertEquals(2, cddl.size()); assertNotNull(cddl.get("name")); assertNotNull(cddl.get("count"));
        MNode yaml = parsed("name: alice\nage: 30\nactive: true", Vernacular.YAML);
        assertEquals(new MValue.Text("alice"), yaml.get("name")); assertEquals(new MValue.Int(30), yaml.get("age")); assertEquals(new MValue.Bool(true), yaml.get("active"));
        MNode readout = parsed("name : 'alice'\nage  : 30", Vernacular.READOUT);
        assertEquals(new MValue.Text("alice"), readout.get("name")); assertEquals(new MValue.Int(30), readout.get("age"));
        MNode back = parsed(render(sampleMNode(), Vernacular.READOUT), Vernacular.READOUT);
        assertEquals(new MValue.Float(99.5), back.get("score"));
        assertThrows(ANodeException.class, () -> Vernaculars.parse("foo", Vernacular.DISPLAY));
        assertThrows(ANodeException.class, () -> Vernaculars.parse("foo", Vernacular.SQL_SCHEMA));
    }

    @Test void vernacularNamesResolve() {
        assertEquals(Vernacular.JSON, Vernacular.parse("json"));
        assertEquals(Vernacular.SQL, Vernacular.parse("SQL"));
        assertEquals(Vernacular.CDDL, Vernacular.parse("cddl"));
        assertEquals(Vernacular.CDDL_VALUE, Vernacular.parse("cddl-value"));
        assertEquals(Vernacular.CDDL_VALUE, Vernacular.parse("cddlvalue"));
        assertEquals(Vernacular.SQLITE_SCHEMA, Vernacular.parse("sqlite-schema"));
        assertNull(Vernacular.parse("unknown"));
        for (Vernacular v : Vernacular.values()) assertEquals(v, Vernacular.parse(v.label()));
    }

    @Test void numbersPrintAsTheReferenceDoes() {
        assertEquals("1", Numbers.f64(1.0)); assertEquals("99.5", Numbers.f64(99.5)); assertEquals("0.1", Numbers.f64(0.1));
        assertEquals("1000000000000000000000", Numbers.f64(1e21)); assertEquals("0.0000001", Numbers.f64(1e-7));
        assertEquals("-0", Numbers.f64(-0.0)); assertEquals("NaN", Numbers.f64(Double.NaN)); assertEquals("inf", Numbers.f64(Double.POSITIVE_INFINITY));
        assertEquals("3.25", Numbers.f32(3.25f)); assertEquals("0.1", Numbers.f32(0.1f), "shortest at single precision");
        assertEquals("1.0", Numbers.withPoint(Numbers.f64(1.0))); assertEquals("99.5", Numbers.withPoint("99.5"));
        assertEquals("\"score\":1.0", render(new MNode().insert("score", new MValue.Float(1.0)), Vernacular.JSONL).replace("{", "").replace("}", ""));
        assertEquals("1", MNodeVernacular.sqlValue(new MValue.Float(1.0)));
    }

    @Test void theTreeIsPlainJavaValues() {
        MNode m = sampleMNode().insert("bytes", new MValue.Bytes(new byte[] {1, (byte) 0xFF})).insert("half", new MValue.Half(3))
            .insert("nanos", new MValue.Nanos(1, 2)).insert("nested", new MValue.MapValue(new MNode().insert("k", new MValue.Int32(9))));
        @SuppressWarnings("unchecked") Map<String, Object> tree = (Map<String, Object>) Vernaculars.toTree(ANode.of(m));
        assertEquals("alice", tree.get("name")); assertEquals(30L, tree.get("age")); assertEquals(99.5, tree.get("score"));
        assertEquals(true, tree.get("active")); assertNull(tree.get("empty")); assertTrue(tree.containsKey("empty"));
        assertEquals("01ff", tree.get("bytes")); assertEquals(3L, tree.get("half"));
        assertEquals(Map.of("epoch_seconds", 1L, "nano_adjust", 2L), tree.get("nanos"));
        assertEquals(Map.of("k", 9L), tree.get("nested"));
        assertEquals(List.of("name", "age", "score", "active", "empty", "bytes", "half", "nanos", "nested"), List.copyOf(tree.keySet()));
        @SuppressWarnings("unchecked") Map<String, Object> p = (Map<String, Object>) Vernaculars.toTree(ANode.of(samplePNode()));
        assertEquals("and", p.get("type"));
        assertEquals(2, ((List<?>) p.get("children")).size());
    }
}
