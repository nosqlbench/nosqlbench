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

import java.util.ArrayList;
import java.util.List;

/// The reverse of [PNode#display]: parses the display grammar back into
/// a tree. The parser is deliberately strict — it accepts exactly what
/// the renderer emits and rejects everything else, and a successful
/// round trip is the codec contract.
///
/// ```text
/// pnode      := conjugate | predicate
/// conjugate  := '(' pnode (' AND ' pnode)+ ')' | '(' pnode (' OR ' pnode)+ ')'
/// predicate  := field ' ' op ' ' comparands
/// field      := bare_name | 'field[' digits ']'
/// op         := '>' | '<' | '=' | '!=' | '>=' | '<=' | 'IN' | 'MATCHES'
/// comparands := comparand | '(' comparand (', ' comparand)* ')'
/// comparand  := int | float | "'" text "'" | 'true' | 'false' | "X'" hex "'" | 'NULL'
/// ```
public final class PNodeDisplay {
    private PNodeDisplay() { }

    /// A position-tagged parse failure; `position` is the offset into
    /// the input where the parser stopped.
    public static final class ParseException extends ANodeException {
        private final int position;
        ParseException(String message, int position) { super("PNode parse error at byte " + position + ": " + message); this.position = position; }
        public int position() { return position; }
    }

    /// Parses display-form text into a tree.
    public static PNode parse(String text) {
        Parser p = new Parser(text);
        PNode node = p.node();
        p.skipWs();
        if (p.at < text.length()) throw p.err("trailing input after predicate tree");
        return node;
    }

    private static final class Parser {
        private final String input; private int at;
        Parser(String input) { this.input = input; }

        ParseException err(String message) { return new ParseException(message, at); }
        private String rest() { return input.substring(at); }
        private int peek() { return at < input.length() ? input.charAt(at) : -1; }
        void skipWs() { while (at < input.length() && Character.isWhitespace(input.charAt(at))) at++; }

        private void consume(char expected) {
            int c = peek();
            if (c == expected) { at++; return; }
            throw err(c < 0 ? "expected '" + expected + "', got EOF" : "expected '" + expected + "', got '" + (char) c + "'");
        }

        private boolean literal(String lit) { if (input.startsWith(lit, at)) { at += lit.length(); return true; } return false; }

        /// A keyword must be followed by whitespace, `(`, or the end,
        /// so `AND` does not eat the front of a field named `ANDREW`.
        private boolean keyword(String kw) {
            if (!input.startsWith(kw, at)) return false;
            int next = at + kw.length() < input.length() ? input.charAt(at + kw.length()) : -1;
            boolean ok = next < 0 || next == ' ' || next == '\t' || next == '\n' || next == '(';
            if (ok) at += kw.length();
            return ok;
        }

        private boolean keywordValue(String kw) {
            if (!input.startsWith(kw, at)) return false;
            int next = at + kw.length() < input.length() ? input.charAt(at + kw.length()) : -1;
            boolean ok = next < 0 || next == ' ' || next == '\t' || next == ',' || next == ')';
            if (ok) at += kw.length();
            return ok;
        }

        PNode node() {
            skipWs();
            return peek() == '(' ? conjugateOrParenthesised() : predicate();
        }

        /// A leading `(` opens either a conjugate or a parenthesised
        /// node; the first inner node is parsed, then `AND`/`OR` decides.
        private PNode conjugateOrParenthesised() {
            consume('(');
            PNode first = node();
            skipWs();
            ConjugateType kind = keyword("AND") ? ConjugateType.AND : keyword("OR") ? ConjugateType.OR : null;
            if (kind == null) { skipWs(); consume(')'); return first; }
            List<PNode> children = new ArrayList<>();
            children.add(first);
            skipWs();
            children.add(node());
            while (true) {
                skipWs();
                if (!keyword(kind.name())) break;
                skipWs();
                children.add(node());
            }
            skipWs();
            consume(')');
            return new PNode.Conjugate(kind, children);
        }

        private PNode predicate() {
            FieldRef field = fieldRef();
            skipWs();
            OpType op = op();
            skipWs();
            return new PNode.Predicate(field, op, comparands());
        }

        private FieldRef fieldRef() {
            if (input.startsWith("field[", at)) {
                at += 6;
                int start = at;
                while (at < input.length() && Character.isDigit(input.charAt(at))) at++;
                if (at == start) throw err("expected digits after 'field['");
                int index;
                try { index = Integer.parseInt(input.substring(start, at)); if (index > 255) throw new NumberFormatException(); }
                catch (NumberFormatException e) { throw err("field index does not fit in u8"); }
                consume(']');
                return new FieldRef.Index(index);
            }
            int start = at;
            while (at < input.length()) {
                char c = input.charAt(at);
                if (Character.isWhitespace(c) || c == '(' || c == ')') break;
                at++;
            }
            if (at == start) throw err("expected field name");
            return new FieldRef.Named(input.substring(start, at));
        }

        private OpType op() {
            if (literal("!=")) return OpType.NE;
            if (literal(">=")) return OpType.GE;
            if (literal("<=")) return OpType.LE;
            if (literal(">")) return OpType.GT;
            if (literal("<")) return OpType.LT;
            if (literal("=")) return OpType.EQ;
            if (keyword("IN")) return OpType.IN;
            if (keyword("MATCHES")) return OpType.MATCHES;
            throw err("expected operator (>, <, =, !=, >=, <=, IN, MATCHES)");
        }

        private List<Comparand> comparands() {
            skipWs();
            List<Comparand> out = new ArrayList<>();
            if (peek() != '(') { out.add(comparand()); return out; }
            consume('(');
            skipWs();
            if (peek() == ')') { consume(')'); return out; }
            out.add(comparand());
            while (true) {
                skipWs();
                if (!literal(",")) break;
                skipWs();
                out.add(comparand());
            }
            skipWs();
            consume(')');
            return out;
        }

        private Comparand comparand() {
            skipWs();
            int c = peek();
            if (c < 0) throw err("expected comparand, got EOF");
            if (input.startsWith("NULL", at)) {
                int next = at + 4 < input.length() ? input.charAt(at + 4) : -1;
                if (next < 0 || next == ' ' || next == '\t' || next == ',' || next == ')') { at += 4; return Comparand.NULL; }
            }
            if (keywordValue("true")) return new Comparand.Bool(true);
            if (keywordValue("false")) return new Comparand.Bool(false);
            if (input.startsWith("X'", at)) return bytes();
            if (c == '\'') return text();
            if (c == '-' || c == '+' || Character.isDigit(c)) return number();
            throw err("unexpected character '" + (char) c + "' in comparand");
        }

        private Comparand bytes() {
            at += 2;
            int start = at;
            while (at < input.length() && input.charAt(at) != '\'') at++;
            String hex = input.substring(start, at);
            consume('\'');
            if (hex.length() % 2 != 0) throw new ParseException("odd-length hex literal", start);
            byte[] out = new byte[hex.length() / 2];
            for (int i = 0; i < hex.length(); i += 2) {
                int hi = Character.digit(hex.charAt(i), 16), lo = Character.digit(hex.charAt(i + 1), 16);
                if (hi < 0) throw new ParseException("non-hex character", start + i);
                if (lo < 0) throw new ParseException("non-hex character", start + i + 1);
                out[i / 2] = (byte) ((hi << 4) | lo);
            }
            return new Comparand.Bytes(out);
        }

        private Comparand text() {
            consume('\'');
            int start = at;
            while (at < input.length() && input.charAt(at) != '\'') at++;
            String body = input.substring(start, at);
            consume('\'');
            return new Comparand.Text(body);
        }

        private Comparand number() {
            int start = at;
            if (peek() == '+' || peek() == '-') at++;
            int digits = at;
            while (at < input.length() && Character.isDigit(input.charAt(at))) at++;
            if (at == digits) throw new ParseException("expected digits", digits);
            boolean isFloat = false;
            if (peek() == '.') { isFloat = true; at++; while (at < input.length() && Character.isDigit(input.charAt(at))) at++; }
            if (peek() == 'e' || peek() == 'E') {
                isFloat = true; at++;
                if (peek() == '+' || peek() == '-') at++;
                int exp = at;
                while (at < input.length() && Character.isDigit(input.charAt(at))) at++;
                if (at == exp) throw new ParseException("expected exponent digits", exp);
            }
            String slice = input.substring(start, at);
            try { return isFloat ? new Comparand.Float(Double.parseDouble(slice)) : new Comparand.Int(Long.parseLong(slice.startsWith("+") ? slice.substring(1) : slice)); }
            catch (NumberFormatException e) { throw new ParseException(isFloat ? "invalid float" : "invalid integer", start); }
        }
    }
}
