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

import static org.junit.jupiter.api.Assertions.*;

/// The display grammar round-trips: `parse(node.display())` is `node`,
/// for every operator, comparand type, and tree shape — and the parser
/// rejects what the renderer never emits.
@Tag("unit")
class PNodeDisplayTest {

    static PNode pred(String field, OpType op, Comparand c) { return new PNode.Predicate(new FieldRef.Named(field), op, List.of(c)); }

    static void roundTrip(PNode node) {
        String text = node.display();
        assertEquals(node, PNodeDisplay.parse(text), "round trip differs: rendered=" + text);
    }

    @Test void predicatesRoundTrip() {
        roundTrip(pred("age", OpType.EQ, new Comparand.Int(42)));
        for (OpType op : List.of(OpType.GT, OpType.LT, OpType.GE, OpType.LE, OpType.NE)) roundTrip(pred("score", op, new Comparand.Int(10)));
        roundTrip(pred("ratio", OpType.LT, new Comparand.Float(3.25)));
        roundTrip(pred("zero", OpType.EQ, new Comparand.Float(0.0)));
        roundTrip(pred("whole", OpType.EQ, new Comparand.Float(1.0)));
        roundTrip(pred("name", OpType.EQ, new Comparand.Text("alice bob")));
        roundTrip(pred("blob", OpType.MATCHES, new Comparand.Text("(^|, )foo(,|$)")));
        roundTrip(pred("active", OpType.EQ, new Comparand.Bool(true)));
        roundTrip(pred("active", OpType.EQ, new Comparand.Bool(false)));
        roundTrip(pred("missing", OpType.EQ, Comparand.NULL));
        roundTrip(pred("blob", OpType.EQ, new Comparand.Bytes(new byte[] {(byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef})));
        roundTrip(new PNode.Predicate(new FieldRef.Index(3), OpType.GT, List.of(new Comparand.Int(7))));
        roundTrip(new PNode.Predicate(new FieldRef.Named("status"), OpType.IN, List.of(new Comparand.Int(1), new Comparand.Int(2), new Comparand.Int(3))));
        roundTrip(pred("delta", OpType.LT, new Comparand.Int(-7)));
        roundTrip(pred("delta", OpType.LT, new Comparand.Float(-1.5)));
        roundTrip(pred("INDEX_NO", OpType.EQ, new Comparand.Int(5)));
    }

    @Test void treesRoundTrip() {
        roundTrip(new PNode.Conjugate(ConjugateType.AND, List.of(pred("age", OpType.GE, new Comparand.Int(18)), pred("name", OpType.MATCHES, new Comparand.Text("ali")))));
        roundTrip(new PNode.Conjugate(ConjugateType.OR, List.of(pred("a", OpType.EQ, new Comparand.Int(1)), pred("b", OpType.EQ, new Comparand.Int(2)), pred("c", OpType.EQ, new Comparand.Int(3)))));
        roundTrip(new PNode.Conjugate(ConjugateType.AND, List.of(pred("age", OpType.GE, new Comparand.Int(18)),
            new PNode.Conjugate(ConjugateType.OR, List.of(pred("status", OpType.EQ, new Comparand.Int(1)), pred("status", OpType.EQ, new Comparand.Int(2)))))));
    }

    @Test void whatTheRendererNeverEmitsIsRejected() {
        assertThrows(PNodeDisplay.ParseException.class, () -> PNodeDisplay.parse("age = 1 extra"));
        assertThrows(PNodeDisplay.ParseException.class, () -> PNodeDisplay.parse("age @ 1"));
        assertThrows(PNodeDisplay.ParseException.class, () -> PNodeDisplay.parse(""));
        assertThrows(PNodeDisplay.ParseException.class, () -> PNodeDisplay.parse("b = X'abc'"), "odd-length hex");
        PNodeDisplay.ParseException e = assertThrows(PNodeDisplay.ParseException.class, () -> PNodeDisplay.parse("age > "));
        assertTrue(e.getMessage().startsWith("PNode parse error at byte "), e.getMessage());
        assertEquals(pred("x", OpType.EQ, new Comparand.Int(1)), PNodeDisplay.parse("(x = 1)"), "a parenthesised predicate is tolerated");
    }
}
