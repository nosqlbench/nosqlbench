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

import java.util.Arrays;
import java.util.List;

/// Evaluates a [PNode] predicate tree against an [MNode] record, with
/// the reference's coercions: every integer-family value compares with
/// an `Int` comparand, every float-family value with a `Float`, the two
/// cross-compare numerically, text families compare with `Text`, and a
/// `MATCHES` is substring containment — the `LIKE '%pattern%'`
/// semantic — over text or bytes, any comparand sufficing. A missing
/// field equals `NULL` and nothing else.
public final class PredicateEvaluator {
    private PredicateEvaluator() { }

    /// Whether the record satisfies the predicate.
    public static boolean evaluate(PNode pnode, MNode mnode) {
        if (pnode instanceof PNode.Conjugate conj) {
            switch (conj.type()) {
                case AND -> { for (PNode child : conj.children()) if (!evaluate(child, mnode)) return false; return true; }
                case OR -> { for (PNode child : conj.children()) if (evaluate(child, mnode)) return true; return false; }
                default -> { return false; }
            }
        }
        PNode.Predicate pred = (PNode.Predicate) pnode;
        MValue value = pred.field() instanceof FieldRef.Named n ? mnode.get(n.name()) : mnode.getAt(((FieldRef.Index) pred.field()).value());
        if (value == null) {
            return switch (pred.op()) {
                case EQ, IN -> pred.comparands().stream().anyMatch(c -> c instanceof Comparand.Null);
                case NE -> pred.comparands().stream().noneMatch(c -> c instanceof Comparand.Null);
                default -> false;
            };
        }
        return leaf(value, pred.op(), pred.comparands());
    }

    private static boolean leaf(MValue value, OpType op, List<Comparand> comparands) {
        return switch (op) {
            case IN -> comparands.stream().anyMatch(c -> eq(value, c));
            case MATCHES -> comparands.stream().anyMatch(c -> matches(value, c));
            case EQ -> eq(value, comparands.get(0));
            case NE -> !eq(value, comparands.get(0));
            case GT, LT, GE, LE -> {
                Integer ord = ord(value, comparands.get(0));
                if (ord == null) yield false;
                yield switch (op) {
                    case GT -> ord > 0;
                    case LT -> ord < 0;
                    case GE -> ord >= 0;
                    default -> ord <= 0;
                };
            }
        };
    }

    private static boolean eq(MValue mv, Comparand c) {
        if (mv instanceof MValue.Null) return c instanceof Comparand.Null;
        if (c instanceof Comparand.Null) return false;
        if (mv instanceof MValue.Bool a && c instanceof Comparand.Bool b) return a.value() == b.value();
        if (mv instanceof MValue.Bytes a && c instanceof Comparand.Bytes b) return Arrays.equals(a.value(), b.value());
        String text = text(mv);
        if (text != null && c instanceof Comparand.Text b) return text.equals(b.value());
        Long i = integer(mv);
        Double f = floating(mv);
        if (i != null && c instanceof Comparand.Int ci) return i == ci.value();
        if (f != null && c instanceof Comparand.Float cf) return f == cf.value();
        if (i != null && c instanceof Comparand.Float cf) return (double) i == cf.value();
        if (f != null && c instanceof Comparand.Int ci) return f == (double) ci.value();
        return false;
    }

    private static boolean matches(MValue mv, Comparand c) {
        String text = text(mv);
        if (text != null && c instanceof Comparand.Text b) return text.contains(b.value());
        if (mv instanceof MValue.Bytes a && c instanceof Comparand.Bytes b) return contains(a.value(), b.value());
        return false;
    }

    private static boolean contains(byte[] haystack, byte[] needle) {
        if (needle.length == 0) return true;
        if (needle.length > haystack.length) return false;
        outer:
        for (int i = 0; i + needle.length <= haystack.length; i++) {
            for (int j = 0; j < needle.length; j++) if (haystack[i + j] != needle[j]) continue outer;
            return true;
        }
        return false;
    }

    /// The ordering of a value against a comparand, or `null` for a
    /// pair that has none.
    private static Integer ord(MValue mv, Comparand c) {
        String text = text(mv);
        if (text != null && c instanceof Comparand.Text b) return Integer.signum(text.compareTo(b.value()));
        Long i = integer(mv);
        Double f = floating(mv);
        if (i != null && c instanceof Comparand.Int ci) return Long.compare(i, ci.value());
        if (f != null && c instanceof Comparand.Float cf) return partial(f, cf.value());
        if (i != null && c instanceof Comparand.Float cf) return partial((double) i, cf.value());
        if (f != null && c instanceof Comparand.Int ci) return partial(f, (double) ci.value());
        return null;
    }

    private static Integer partial(double a, double b) {
        if (Double.isNaN(a) || Double.isNaN(b)) return null;
        return a == b ? 0 : a < b ? -1 : 1;
    }

    /// Text families: `Text`, `Ascii`, `EnumStr` — not the temporal
    /// strings, which the reference does not compare as text.
    private static String text(MValue mv) {
        if (mv instanceof MValue.Text t) return t.value();
        if (mv instanceof MValue.Ascii t) return t.value();
        if (mv instanceof MValue.EnumStr t) return t.value();
        return null;
    }

    /// Integer families: `Int`, `Int32`, `Short`, `Millis`.
    private static Long integer(MValue mv) {
        if (mv instanceof MValue.Int v) return v.value();
        if (mv instanceof MValue.Int32 v) return (long) v.value();
        if (mv instanceof MValue.Short v) return (long) v.value();
        if (mv instanceof MValue.Millis v) return v.value();
        return null;
    }

    private static Double floating(MValue mv) {
        if (mv instanceof MValue.Float v) return v.value();
        if (mv instanceof MValue.Float32 v) return (double) v.value();
        return null;
    }
}
