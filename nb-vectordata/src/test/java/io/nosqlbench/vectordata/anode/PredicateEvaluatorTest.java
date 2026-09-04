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

import static io.nosqlbench.vectordata.anode.PredicateEvaluator.evaluate;
import static org.junit.jupiter.api.Assertions.*;

/// Predicate semantics, mirrored case for case from the reference's
/// evaluator tests: each operator, missing and explicit nulls, every
/// integer- and float-family coercion, text subtypes, cross-type
/// mismatches, and positional fields.
@Tag("unit")
class PredicateEvaluatorTest {

    static PNode pred(String name, OpType op, Comparand... cs) { return new PNode.Predicate(new FieldRef.Named(name), op, List.of(cs)); }
    static MNode one(String name, MValue value) { return new MNode().insert(name, value); }

    @Test void equality() {
        assertTrue(evaluate(pred("x", OpType.EQ, new Comparand.Int(42)), one("x", new MValue.Int(42))));
        assertFalse(evaluate(pred("x", OpType.EQ, new Comparand.Int(42)), one("x", new MValue.Int(99))));
        assertTrue(evaluate(pred("x", OpType.EQ, new Comparand.Float(3.25)), one("x", new MValue.Float(3.25))));
        assertFalse(evaluate(pred("x", OpType.EQ, new Comparand.Float(3.25)), one("x", new MValue.Float(2.5))));
        assertTrue(evaluate(pred("x", OpType.EQ, new Comparand.Text("hello")), one("x", new MValue.Text("hello"))));
        assertFalse(evaluate(pred("x", OpType.EQ, new Comparand.Text("hello")), one("x", new MValue.Text("world"))));
        assertTrue(evaluate(pred("x", OpType.EQ, new Comparand.Bool(true)), one("x", new MValue.Bool(true))));
        assertFalse(evaluate(pred("x", OpType.EQ, new Comparand.Bool(true)), one("x", new MValue.Bool(false))));
        assertTrue(evaluate(pred("x", OpType.EQ, Comparand.NULL), one("x", MValue.NULL)));
        assertFalse(evaluate(pred("x", OpType.EQ, Comparand.NULL), one("x", new MValue.Int(1))));
        assertTrue(evaluate(pred("x", OpType.EQ, new Comparand.Bytes(new byte[] {1, 2, 3})), one("x", new MValue.Bytes(new byte[] {1, 2, 3}))));
        assertFalse(evaluate(pred("x", OpType.EQ, new Comparand.Bytes(new byte[] {1, 2, 3})), one("x", new MValue.Bytes(new byte[] {4, 5}))));
        assertFalse(evaluate(pred("x", OpType.NE, new Comparand.Int(42)), one("x", new MValue.Int(42))));
        assertTrue(evaluate(pred("x", OpType.NE, new Comparand.Int(42)), one("x", new MValue.Int(99))));
        assertFalse(evaluate(pred("x", OpType.NE, Comparand.NULL), one("x", MValue.NULL)));
        assertTrue(evaluate(pred("x", OpType.NE, Comparand.NULL), one("x", new MValue.Int(1))));
    }

    @Test void matchesIsSubstringContainment() {
        PNode the = pred("caption", OpType.MATCHES, new Comparand.Text("the"));
        assertTrue(evaluate(the, one("caption", new MValue.Text("the cat"))));
        assertTrue(evaluate(the, one("caption", new MValue.Text("with the dog"))));
        assertTrue(evaluate(pred("caption", OpType.MATCHES, new Comparand.Text("dog")), one("caption", new MValue.Text("the dog"))));
        assertFalse(evaluate(pred("caption", OpType.MATCHES, new Comparand.Text("xyz")), one("caption", new MValue.Text("the cat"))));
        PNode empty = pred("caption", OpType.MATCHES, new Comparand.Text(""));
        assertTrue(evaluate(empty, one("caption", new MValue.Text("anything"))));
        assertTrue(evaluate(empty, one("caption", new MValue.Text(""))), "LIKE '%%' semantics");
        assertTrue(evaluate(pred("x", OpType.MATCHES, new Comparand.Text("foo")), one("x", new MValue.Ascii("xfoobar"))));
        assertTrue(evaluate(pred("x", OpType.MATCHES, new Comparand.Text("foo")), one("x", new MValue.EnumStr("foo"))));
        PNode blob = pred("blob", OpType.MATCHES, new Comparand.Bytes(new byte[] {(byte) 0xAB, (byte) 0xCD}));
        assertTrue(evaluate(blob, one("blob", new MValue.Bytes(new byte[] {0x01, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF}))));
        assertFalse(evaluate(blob, one("blob", new MValue.Bytes(new byte[] {1, 2, 3}))));
        assertFalse(evaluate(pred("x", OpType.MATCHES, new Comparand.Text("foo")), one("x", new MValue.Int(42))));
        assertFalse(evaluate(pred("x", OpType.MATCHES, new Comparand.Text("foo")), one("x", new MValue.Float(3.25))));
        PNode any = pred("caption", OpType.MATCHES, new Comparand.Text("xyz"), new Comparand.Text("cat"));
        assertTrue(evaluate(any, one("caption", new MValue.Text("the cat"))), "several patterns are an OR");
        assertFalse(evaluate(any, one("caption", new MValue.Text("the dog"))));
        assertFalse(evaluate(pred("x", OpType.MATCHES, new Comparand.Text(".*")), one("x", new MValue.Text("anything"))), "not a regex");
    }

    @Test void ordering() {
        PNode gt = pred("x", OpType.GT, new Comparand.Int(10));
        assertTrue(evaluate(gt, one("x", new MValue.Int(11)))); assertFalse(evaluate(gt, one("x", new MValue.Int(10)))); assertFalse(evaluate(gt, one("x", new MValue.Int(9))));
        PNode lt = pred("x", OpType.LT, new Comparand.Float(5.0));
        assertTrue(evaluate(lt, one("x", new MValue.Float(4.9)))); assertFalse(evaluate(lt, one("x", new MValue.Float(5.0)))); assertFalse(evaluate(lt, one("x", new MValue.Float(5.1))));
        PNode ge = pred("x", OpType.GE, new Comparand.Int(10));
        assertTrue(evaluate(ge, one("x", new MValue.Int(10)))); assertTrue(evaluate(ge, one("x", new MValue.Int(11)))); assertFalse(evaluate(ge, one("x", new MValue.Int(9))));
        PNode le = pred("x", OpType.LE, new Comparand.Float(5.0));
        assertTrue(evaluate(le, one("x", new MValue.Float(5.0)))); assertTrue(evaluate(le, one("x", new MValue.Float(4.9)))); assertFalse(evaluate(le, one("x", new MValue.Float(5.1))));
        PNode text = pred("x", OpType.GT, new Comparand.Text("bob"));
        assertTrue(evaluate(text, one("x", new MValue.Text("charlie")))); assertFalse(evaluate(text, one("x", new MValue.Text("alice"))));
    }

    @Test void membership() {
        PNode ints = pred("x", OpType.IN, new Comparand.Int(1), new Comparand.Int(3), new Comparand.Int(5));
        assertTrue(evaluate(ints, one("x", new MValue.Int(3)))); assertFalse(evaluate(ints, one("x", new MValue.Int(2))));
        PNode texts = pred("x", OpType.IN, new Comparand.Text("a"), new Comparand.Text("b"));
        assertTrue(evaluate(texts, one("x", new MValue.Text("b")))); assertFalse(evaluate(texts, one("x", new MValue.Text("c"))));
    }

    @Test void missingAndExplicitNulls() {
        MNode other = one("other", new MValue.Int(1));
        assertTrue(evaluate(pred("missing", OpType.EQ, Comparand.NULL), other));
        assertFalse(evaluate(pred("missing", OpType.EQ, new Comparand.Int(42)), other));
        assertFalse(evaluate(pred("missing", OpType.NE, Comparand.NULL), other));
        assertFalse(evaluate(pred("missing", OpType.GT, new Comparand.Int(0)), other));
        assertTrue(evaluate(pred("missing", OpType.IN, new Comparand.Int(1), Comparand.NULL), other));
        assertTrue(evaluate(pred("x", OpType.EQ, Comparand.NULL), one("x", MValue.NULL)));
        assertFalse(evaluate(pred("x", OpType.NE, Comparand.NULL), one("x", MValue.NULL)));
        assertFalse(evaluate(pred("x", OpType.GT, new Comparand.Int(0)), one("x", MValue.NULL)));
    }

    @Test void conjugates() {
        PNode and = new PNode.Conjugate(ConjugateType.AND, List.of(pred("x", OpType.GT, new Comparand.Int(5)), pred("y", OpType.EQ, new Comparand.Text("ok"))));
        assertTrue(evaluate(and, new MNode().insert("x", new MValue.Int(10)).insert("y", new MValue.Text("ok"))));
        assertFalse(evaluate(and, new MNode().insert("x", new MValue.Int(10)).insert("y", new MValue.Text("no"))));
        PNode or = new PNode.Conjugate(ConjugateType.OR, List.of(pred("x", OpType.EQ, new Comparand.Int(1)), pred("x", OpType.EQ, new Comparand.Int(2))));
        assertTrue(evaluate(or, one("x", new MValue.Int(1)))); assertTrue(evaluate(or, one("x", new MValue.Int(2)))); assertFalse(evaluate(or, one("x", new MValue.Int(3))));
    }

    @Test void numericCoercions() {
        assertTrue(evaluate(pred("x", OpType.EQ, new Comparand.Int(42)), one("x", new MValue.Int32(42))));
        assertFalse(evaluate(pred("x", OpType.EQ, new Comparand.Int(42)), one("x", new MValue.Int32(99))));
        assertTrue(evaluate(pred("x", OpType.GT, new Comparand.Int(10)), one("x", new MValue.Short((short) 11))));
        assertFalse(evaluate(pred("x", OpType.GT, new Comparand.Int(10)), one("x", new MValue.Short((short) 10))));
        PNode le = pred("x", OpType.LE, new Comparand.Int(1000));
        assertTrue(evaluate(le, one("x", new MValue.Millis(1000)))); assertTrue(evaluate(le, one("x", new MValue.Millis(999)))); assertFalse(evaluate(le, one("x", new MValue.Millis(1001))));
        assertTrue(evaluate(pred("x", OpType.LT, new Comparand.Float(5.0)), one("x", new MValue.Float32(4.5f))));
        assertFalse(evaluate(pred("x", OpType.LT, new Comparand.Float(5.0)), one("x", new MValue.Float32(5.5f))));
        assertTrue(evaluate(pred("x", OpType.EQ, new Comparand.Float(42.0)), one("x", new MValue.Int(42))));
        assertFalse(evaluate(pred("x", OpType.EQ, new Comparand.Float(42.0)), one("x", new MValue.Int(43))));
        assertTrue(evaluate(pred("x", OpType.EQ, new Comparand.Int(42)), one("x", new MValue.Float(42.0))));
        assertFalse(evaluate(pred("x", OpType.EQ, new Comparand.Int(42)), one("x", new MValue.Float(42.5))));
        assertTrue(evaluate(pred("x", OpType.GT, new Comparand.Float(10.5)), one("x", new MValue.Int(11))));
        assertFalse(evaluate(pred("x", OpType.GT, new Comparand.Float(10.5)), one("x", new MValue.Int(10))));
        assertTrue(evaluate(pred("x", OpType.LT, new Comparand.Int(10)), one("x", new MValue.Float(9.5))));
        assertFalse(evaluate(pred("x", OpType.LT, new Comparand.Int(10)), one("x", new MValue.Float(10.5))));
    }

    @Test void textSubtypesAndMismatches() {
        assertTrue(evaluate(pred("x", OpType.EQ, new Comparand.Text("hello")), one("x", new MValue.Ascii("hello"))));
        assertTrue(evaluate(pred("x", OpType.EQ, new Comparand.Text("red")), one("x", new MValue.EnumStr("red"))));
        assertFalse(evaluate(pred("x", OpType.EQ, new Comparand.Int(42)), one("x", new MValue.Text("42"))));
        assertFalse(evaluate(pred("x", OpType.EQ, new Comparand.Int(1)), one("x", new MValue.Bool(true))));
    }

    @Test void positionalFields() {
        MNode m = new MNode().insert("first", new MValue.Int(42)).insert("second", new MValue.Int(99));
        assertTrue(evaluate(new PNode.Predicate(new FieldRef.Index(0), OpType.EQ, List.of(new Comparand.Int(42))), m));
        assertTrue(evaluate(new PNode.Predicate(new FieldRef.Index(1), OpType.EQ, List.of(new Comparand.Int(99))), m));
        assertFalse(evaluate(new PNode.Predicate(new FieldRef.Index(2), OpType.EQ, List.of(new Comparand.Int(99))), m), "past the last field is missing");
    }
}
