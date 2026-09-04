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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/// Stage 2 of the codec: bi-directional conversion between an [ANode]
/// and text in a [Vernacular].
///
/// ```text
/// bytes ←→ [stage 1: ANode] ←→ ANode ←→ [stage 2: this] ←→ text
/// ```
///
/// [#render] serves every vernacular; [#parse] serves the ones that
/// read back (JSON, YAML, SQL, CQL, CDDL, readout), and its type
/// inference is the reference's — quoted strings, bare integers,
/// decimal-point floats, `true`/`false`, and each language's null.
/// [#toTree] is the bridge to plain Java values, the role serde plays
/// in the reference: a record as a map of standard types.
public final class Vernaculars {
    private Vernaculars() { }

    /// Renders a node in a vernacular.
    public static String render(ANode node, Vernacular vernacular) {
        return node instanceof ANode.M m ? renderMNode(m.node(), vernacular) : renderPNode(((ANode.P) node).node(), vernacular);
    }

    static String renderMNode(MNode m, Vernacular v) {
        return switch (v) {
            case CDDL -> MNodeVernacular.toCddl(m);
            case CDDL_VALUE -> cddlValue(m);
            case SQL, SQLITE -> MNodeVernacular.toSql(m);
            case SQL_SCHEMA, SQLITE_SCHEMA -> MNodeVernacular.toSqlSchema(m);
            case CQL -> MNodeVernacular.toCql(m);
            case CQL_SCHEMA -> MNodeVernacular.toCqlSchema(m);
            case JSON -> Json.pretty(mnodeToJson(m), 0);
            case JSONL -> Json.compact(mnodeToJson(m));
            case YAML -> yamlMNode(m);
            case READOUT -> readoutMNode(m, 0);
            case DISPLAY -> m.display();
        };
    }

    static String renderPNode(PNode p, Vernacular v) {
        return switch (v) {
            case CDDL, CDDL_VALUE -> PNodeVernacular.toCddl(p);
            case SQL, SQLITE, SQL_SCHEMA, SQLITE_SCHEMA -> PNodeVernacular.toSql(p);
            case CQL, CQL_SCHEMA -> PNodeVernacular.toCql(p);
            case JSON -> Json.pretty(pnodeToJson(p), 0);
            case JSONL -> Json.compact(pnodeToJson(p));
            case YAML -> yamlPNode(p, 0);
            case READOUT -> readoutPNode(p, 0);
            case DISPLAY -> p.display();
        };
    }

    // -- CDDL value --

    private static String cddlValue(MNode m) {
        List<String> lines = new ArrayList<>();
        m.fields().forEach((name, value) -> lines.add("  " + name + " : " + MNodeVernacular.toCddlValue(value)));
        return "{\n" + String.join(",\n", lines) + "\n}";
    }

    // -- JSON --

    /// A JSON value, kept minimal on purpose: rendering and parsing
    /// need exactly these shapes and nothing about them.
    sealed interface Json {
        record Null() implements Json { }
        record Bool(boolean value) implements Json { }
        record Int(long value) implements Json { }
        record Float(double value) implements Json { }
        record Str(String value) implements Json { }
        record Array(List<Json> items) implements Json { }
        record Object(List<Map.Entry<String, Json>> entries) implements Json { }

        Json NULL = new Null();

        static String escape(String s) {
            StringBuilder out = new StringBuilder(s.length());
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                switch (c) {
                    case '"' -> out.append("\\\"");
                    case '\\' -> out.append("\\\\");
                    case '\n' -> out.append("\\n");
                    case '\r' -> out.append("\\r");
                    case '\t' -> out.append("\\t");
                    default -> { if (c < 0x20) out.append(String.format("\\u%04x", (int) c)); else out.append(c); }
                }
            }
            return out.toString();
        }

        static String compact(Json value) {
            if (value instanceof Null) return "null";
            if (value instanceof Bool b) return b.value() ? "true" : "false";
            if (value instanceof Int i) return Long.toString(i.value());
            if (value instanceof Float f) return Numbers.withPoint(Numbers.f64(f.value()));
            if (value instanceof Str s) return "\"" + escape(s.value()) + "\"";
            if (value instanceof Array a) { List<String> inner = new ArrayList<>(); for (Json item : a.items()) inner.add(compact(item)); return "[" + String.join(",", inner) + "]"; }
            Object o = (Object) value;
            List<String> inner = new ArrayList<>();
            for (Map.Entry<String, Json> e : o.entries()) inner.add("\"" + escape(e.getKey()) + "\":" + compact(e.getValue()));
            return "{" + String.join(",", inner) + "}";
        }

        static String pretty(Json value, int indent) {
            String pad = "  ".repeat(indent), inner = "  ".repeat(indent + 1);
            if (value instanceof Array a) {
                if (a.items().isEmpty()) return "[]";
                List<String> lines = new ArrayList<>();
                for (Json item : a.items()) lines.add(inner + pretty(item, indent + 1));
                return "[\n" + String.join(",\n", lines) + "\n" + pad + "]";
            }
            if (value instanceof Object o) {
                if (o.entries().isEmpty()) return "{}";
                List<String> lines = new ArrayList<>();
                for (Map.Entry<String, Json> e : o.entries()) lines.add(inner + "\"" + escape(e.getKey()) + "\": " + pretty(e.getValue(), indent + 1));
                return "{\n" + String.join(",\n", lines) + "\n" + pad + "}";
            }
            return compact(value);
        }
    }

    static Json mvalueToJson(MValue v) {
        String text = MValues.text(v);
        List<MValue> items = MValues.items(v);
        return switch (v.tag()) {
            case TEXT, ENUM_STR, ASCII, DATE, TIME, DATETIME -> new Json.Str(text);
            case INT, MILLIS, INT32, ENUM_ORD, SHORT, HALF -> new Json.Int(MValues.integer(v));
            case FLOAT -> new Json.Float(((MValue.Float) v).value());
            case FLOAT32 -> new Json.Float(((MValue.Float32) v).value());
            case BOOL -> new Json.Bool(((MValue.Bool) v).value());
            case NULL -> Json.NULL;
            case BYTES -> new Json.Str(Numbers.hex(((MValue.Bytes) v).value()));
            case NANOS -> { MValue.Nanos n = (MValue.Nanos) v; yield new Json.Object(List.of(Map.entry("epoch_seconds", new Json.Int(n.epochSeconds())), Map.entry("nano_adjust", new Json.Int(n.nanoAdjust())))); }
            case UUID_V1, UUID_V7 -> new Json.Str(Numbers.uuid(MValues.identifier(v)));
            case ULID -> new Json.Str(Numbers.hex(MValues.identifier(v)));
            case LIST, SET, ARRAY -> { List<Json> out = new ArrayList<>(items.size()); for (MValue item : items) out.add(mvalueToJson(item)); yield new Json.Array(out); }
            case MAP -> mnodeToJson(((MValue.MapValue) v).node());
            case TYPED_MAP -> { List<Map.Entry<String, Json>> entries = new ArrayList<>(); for (MValue.TypedMap.Entry e : ((MValue.TypedMap) v).entries()) entries.add(Map.entry(e.key().display(), mvalueToJson(e.value()))); yield new Json.Object(entries); }
            case TEXT_VALIDATED, DECIMAL, VARINT -> throw new ANodeException("no value carries tag " + v.tag());
        };
    }

    static Json mnodeToJson(MNode m) {
        List<Map.Entry<String, Json>> entries = new ArrayList<>();
        m.fields().forEach((k, v) -> entries.add(Map.entry(k, mvalueToJson(v))));
        return new Json.Object(entries);
    }

    static Json comparandToJson(Comparand c) {
        if (c instanceof Comparand.Int v) return new Json.Int(v.value());
        if (c instanceof Comparand.Float v) return new Json.Float(v.value());
        if (c instanceof Comparand.Text v) return new Json.Str(v.value());
        if (c instanceof Comparand.Bool v) return new Json.Bool(v.value());
        if (c instanceof Comparand.Bytes v) return new Json.Str(Numbers.hex(v.value()));
        return Json.NULL;
    }

    static Json pnodeToJson(PNode p) {
        if (p instanceof PNode.Predicate pred) {
            List<Map.Entry<String, Json>> obj = new ArrayList<>();
            obj.add(Map.entry("type", new Json.Str("predicate")));
            obj.add(Map.entry("field", new Json.Str(PNodeVernacular.field(pred.field()))));
            obj.add(Map.entry("op", new Json.Str(pred.op().symbol())));
            if (pred.comparands().size() == 1) obj.add(Map.entry("value", comparandToJson(pred.comparands().get(0))));
            else { List<Json> vals = new ArrayList<>(); for (Comparand c : pred.comparands()) vals.add(comparandToJson(c)); obj.add(Map.entry("values", new Json.Array(vals))); }
            return new Json.Object(obj);
        }
        PNode.Conjugate conj = (PNode.Conjugate) p;
        List<Json> children = new ArrayList<>();
        for (PNode child : conj.children()) children.add(pnodeToJson(child));
        return new Json.Object(List.of(Map.entry("type", new Json.Str(conj.type().name().toLowerCase(Locale.ROOT))), Map.entry("children", new Json.Array(children))));
    }

    // -- YAML --

    private static String yamlMNode(MNode m) {
        List<String> lines = new ArrayList<>();
        m.fields().forEach((name, value) -> yamlField(lines, name, value, 0));
        return String.join("\n", lines);
    }

    private static void yamlField(List<String> lines, String name, MValue value, int indent) {
        String pad = "  ".repeat(indent), deeper = "  ".repeat(indent + 1);
        List<MValue> items = MValues.items(value);
        if (value instanceof MValue.MapValue v) { lines.add(pad + name + ":"); v.node().fields().forEach((k, x) -> yamlField(lines, k, x, indent + 1)); }
        else if (items != null) { lines.add(pad + name + ":"); for (MValue item : items) lines.add(deeper + "- " + yamlScalar(item)); }
        else if (value instanceof MValue.TypedMap v) { lines.add(pad + name + ":"); for (MValue.TypedMap.Entry e : v.entries()) lines.add(deeper + yamlScalar(e.key()) + ": " + yamlScalar(e.value())); }
        else lines.add(pad + name + ": " + yamlScalar(value));
    }

    private static String yamlText(String s) {
        if (s.contains(":") || s.contains("#") || s.contains("'") || s.contains("\"") || s.startsWith(" ") || s.isEmpty())
            return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        return s;
    }

    private static String yamlScalar(MValue v) {
        return switch (v.tag()) {
            case TEXT, ENUM_STR, ASCII, DATE, TIME, DATETIME -> yamlText(MValues.text(v));
            case INT, MILLIS, INT32, ENUM_ORD, SHORT, HALF -> Long.toString(MValues.integer(v));
            case FLOAT -> Numbers.f64(((MValue.Float) v).value());
            case FLOAT32 -> Numbers.f32(((MValue.Float32) v).value());
            case BOOL -> ((MValue.Bool) v).value() ? "true" : "false";
            case NULL -> "null";
            case BYTES -> "0x" + Numbers.hex(((MValue.Bytes) v).value());
            case NANOS -> { MValue.Nanos n = (MValue.Nanos) v; yield n.epochSeconds() + "." + n.nanoAdjust(); }
            case UUID_V1, UUID_V7 -> Numbers.uuid(MValues.identifier(v));
            case ULID -> "0x" + Numbers.hex(MValues.identifier(v));
            default -> v.display();
        };
    }

    private static String yamlComparand(Comparand c) { return c instanceof Comparand.Text t ? yamlText(t.value()) : c.display(); }

    private static String yamlPNode(PNode p, int indent) {
        String pad = "  ".repeat(indent);
        if (p instanceof PNode.Predicate pred) {
            String field = PNodeVernacular.field(pred.field());
            if (pred.comparands().size() == 1)
                return pad + "field: " + field + "\n" + pad + "op: " + pred.op().symbol() + "\n" + pad + "value: " + yamlComparand(pred.comparands().get(0));
            List<String> vals = new ArrayList<>();
            for (Comparand c : pred.comparands()) vals.add(pad + "  - " + yamlComparand(c));
            return pad + "field: " + field + "\n" + pad + "op: " + pred.op().symbol() + "\n" + pad + "values:\n" + String.join("\n", vals);
        }
        PNode.Conjugate conj = (PNode.Conjugate) p;
        List<String> children = new ArrayList<>();
        for (PNode child : conj.children()) children.add("  ".repeat(indent + 1) + "-\n" + yamlPNode(child, indent + 2));
        return pad + "type: " + conj.type().name().toLowerCase(Locale.ROOT) + "\n" + pad + "children:\n" + String.join("\n", children);
    }

    // -- Readout --

    private static String readoutMNode(MNode m, int indent) {
        String pad = "\t".repeat(indent);
        int width = 0;
        for (String name : m.fields().keySet()) width = Math.max(width, name.length());
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, MValue> field : m.fields().entrySet()) {
            String padded = width == 0 ? field.getKey() : String.format("%-" + width + "s", field.getKey());
            MValue value = field.getValue();
            List<MValue> items = MValues.items(value);
            if (value instanceof MValue.MapValue v) { lines.add(pad + padded + " :"); lines.add(readoutMNode(v.node(), indent + 1)); }
            else if (items != null) { lines.add(pad + padded + " :"); for (MValue item : items) lines.add(pad + "\t- " + item.display()); }
            else if (value instanceof MValue.TypedMap v) { lines.add(pad + padded + " :"); for (MValue.TypedMap.Entry e : v.entries()) lines.add(pad + "\t" + e.key().display() + " : " + e.value().display()); }
            else lines.add(pad + padded + " : " + value.display());
        }
        return String.join("\n", lines);
    }

    private static String readoutPNode(PNode p, int indent) {
        String pad = "\t".repeat(indent);
        if (p instanceof PNode.Predicate pred) {
            String field = PNodeVernacular.field(pred.field());
            if (pred.comparands().size() == 1) return pad + field + " " + pred.op().symbol() + " " + pred.comparands().get(0).display();
            List<String> vals = new ArrayList<>();
            for (Comparand c : pred.comparands()) vals.add(c.display());
            return pad + field + " " + pred.op().symbol() + " (" + String.join(", ", vals) + ")";
        }
        PNode.Conjugate conj = (PNode.Conjugate) p;
        String op = conj.type().name();
        List<String> children = new ArrayList<>();
        for (PNode child : conj.children()) children.add(readoutPNode(child, indent + 1));
        return pad + op + ":\n" + String.join("\n" + pad + op + "\n", children);
    }

    // -- Parse --

    /// Parses text in a vernacular into a node. JSON, YAML, SQL, CQL,
    /// CDDL, and readout read back; the schema, value, and display
    /// renderings do not, and say so.
    public static ANode parse(String text, Vernacular vernacular) {
        return switch (vernacular) {
            case JSON, JSONL -> parseJson(text);
            case SQL, SQLITE -> parseValues(text, false);
            case CQL -> parseValues(text, true);
            case CDDL -> parseCddlGroup(text);
            case YAML -> parseYaml(text);
            case READOUT -> parseReadout(text);
            default -> throw new ANodeException(vernacular + " parse not yet supported by ANode encoding");
        };
    }

    private static ANode parseJson(String text) {
        Json value;
        try { value = new JsonParser(text).document(); }
        catch (ANodeException e) { throw new ANodeException("JSON parse error: " + e.getMessage()); }
        if (!(value instanceof Json.Object object)) throw new ANodeException("JSON input must be an object");
        return ANode.of(jsonToMNode(object));
    }

    private static MNode jsonToMNode(Json.Object object) {
        MNode node = new MNode();
        for (Map.Entry<String, Json> e : object.entries()) node.insert(e.getKey(), jsonToMValue(e.getValue()));
        return node;
    }

    private static MValue jsonToMValue(Json value) {
        if (value instanceof Json.Null) return MValue.NULL;
        if (value instanceof Json.Bool b) return new MValue.Bool(b.value());
        if (value instanceof Json.Int i) return new MValue.Int(i.value());
        if (value instanceof Json.Float f) return new MValue.Float(f.value());
        if (value instanceof Json.Str s) return new MValue.Text(s.value());
        if (value instanceof Json.Array a) { List<MValue> items = new ArrayList<>(); for (Json item : a.items()) items.add(jsonToMValue(item)); return new MValue.ListValue(items); }
        return new MValue.MapValue(jsonToMNode((Json.Object) value));
    }

    /// A strict, minimal JSON reader: objects keep their key order, a
    /// number without a point or exponent that fits a long is an
    /// integer, anything else numeric is a float.
    static final class JsonParser {
        private final String text; private int at;
        JsonParser(String text) { this.text = text; }

        Json document() {
            Json value = value();
            ws();
            if (at < text.length()) throw fail("trailing characters");
            return value;
        }

        private Json value() {
            ws();
            if (at >= text.length()) throw fail("unexpected end of input");
            char c = text.charAt(at);
            if (c == '{') return object();
            if (c == '[') return array();
            if (c == '"') return new Json.Str(string());
            if (text.startsWith("true", at)) { at += 4; return new Json.Bool(true); }
            if (text.startsWith("false", at)) { at += 5; return new Json.Bool(false); }
            if (text.startsWith("null", at)) { at += 4; return Json.NULL; }
            if (c == '-' || Character.isDigit(c)) return number();
            throw fail("unexpected character '" + c + "'");
        }

        private Json object() {
            at++;
            List<Map.Entry<String, Json>> entries = new ArrayList<>();
            ws();
            if (peek() == '}') { at++; return new Json.Object(entries); }
            while (true) {
                ws();
                if (peek() != '"') throw fail("expected a key");
                String key = string();
                ws();
                if (peek() != ':') throw fail("expected ':'");
                at++;
                entries.add(Map.entry(key, value()));
                ws();
                char c = peek();
                if (c == ',') { at++; continue; }
                if (c == '}') { at++; return new Json.Object(entries); }
                throw fail("expected ',' or '}'");
            }
        }

        private Json array() {
            at++;
            List<Json> items = new ArrayList<>();
            ws();
            if (peek() == ']') { at++; return new Json.Array(items); }
            while (true) {
                items.add(value());
                ws();
                char c = peek();
                if (c == ',') { at++; continue; }
                if (c == ']') { at++; return new Json.Array(items); }
                throw fail("expected ',' or ']'");
            }
        }

        private String string() {
            at++;
            StringBuilder out = new StringBuilder();
            while (at < text.length()) {
                char c = text.charAt(at++);
                if (c == '"') return out.toString();
                if (c != '\\') { out.append(c); continue; }
                if (at >= text.length()) break;
                char e = text.charAt(at++);
                switch (e) {
                    case '"' -> out.append('"'); case '\\' -> out.append('\\'); case '/' -> out.append('/');
                    case 'b' -> out.append('\b'); case 'f' -> out.append('\f'); case 'n' -> out.append('\n');
                    case 'r' -> out.append('\r'); case 't' -> out.append('\t');
                    case 'u' -> { if (at + 4 > text.length()) throw fail("truncated escape"); out.append((char) Integer.parseInt(text.substring(at, at + 4), 16)); at += 4; }
                    default -> throw fail("invalid escape '\\" + e + "'");
                }
            }
            throw fail("unterminated string");
        }

        private Json number() {
            int start = at;
            if (peek() == '-') at++;
            while (at < text.length() && Character.isDigit(text.charAt(at))) at++;
            boolean fractional = false;
            if (at < text.length() && text.charAt(at) == '.') { fractional = true; at++; while (at < text.length() && Character.isDigit(text.charAt(at))) at++; }
            if (at < text.length() && (text.charAt(at) == 'e' || text.charAt(at) == 'E')) {
                fractional = true; at++;
                if (at < text.length() && (text.charAt(at) == '+' || text.charAt(at) == '-')) at++;
                while (at < text.length() && Character.isDigit(text.charAt(at))) at++;
            }
            String token = text.substring(start, at);
            try { return fractional ? new Json.Float(Double.parseDouble(token)) : new Json.Int(Long.parseLong(token)); }
            catch (NumberFormatException e) {
                try { return new Json.Float(Double.parseDouble(token)); } catch (NumberFormatException again) { throw fail("invalid number '" + token + "'"); }
            }
        }

        private void ws() { while (at < text.length() && Character.isWhitespace(text.charAt(at))) at++; }
        private char peek() { return at < text.length() ? text.charAt(at) : '\0'; }
        private ANodeException fail(String message) { return new ANodeException(message + " at " + at); }
    }

    private static ANode parseValues(String text, boolean cql) {
        String trimmed = text.trim();
        String inner = trimmed.startsWith("(") && trimmed.endsWith(")") ? trimmed.substring(1, trimmed.length() - 1) : trimmed;
        MNode node = new MNode();
        List<String> values = splitValues(inner);
        for (int i = 0; i < values.size(); i++) node.insert("col_" + i, cql ? cqlLiteral(values.get(i).trim()) : sqlLiteral(values.get(i).trim()));
        return ANode.of(node);
    }

    /// Splits a comma-separated list, respecting single-quoted strings,
    /// backslash escapes inside them, and nested parentheses.
    static List<String> splitValues(String s) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inString = false, escapeNext = false;
        int depth = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (escapeNext) { current.append(c); escapeNext = false; continue; }
            if (c == '\'') { inString = !inString; current.append(c); }
            else if (c == '(' && !inString) { depth++; current.append(c); }
            else if (c == ')' && !inString) { depth--; current.append(c); }
            else if (c == ',' && !inString && depth == 0) { result.add(current.toString()); current.setLength(0); }
            else if (c == '\\' && inString) { escapeNext = true; current.append(c); }
            else current.append(c);
        }
        if (current.length() > 0) result.add(current.toString());
        return result;
    }

    private static MValue sqlLiteral(String s) {
        String upper = s.toUpperCase(Locale.ROOT);
        if (upper.equals("NULL")) return MValue.NULL;
        if (upper.equals("TRUE")) return new MValue.Bool(true);
        if (upper.equals("FALSE")) return new MValue.Bool(false);
        return quotedOrNumber(s);
    }

    private static MValue cqlLiteral(String s) {
        String lower = s.toLowerCase(Locale.ROOT);
        if (lower.equals("null")) return MValue.NULL;
        if (lower.equals("true")) return new MValue.Bool(true);
        if (lower.equals("false")) return new MValue.Bool(false);
        return quotedOrNumber(s);
    }

    private static MValue quotedOrNumber(String s) {
        if (s.length() >= 2 && s.startsWith("'") && s.endsWith("'")) return new MValue.Text(s.substring(1, s.length() - 1).replace("''", "'"));
        MValue number = number(s);
        return number != null ? number : new MValue.Text(s);
    }

    /// An integer when the text parses as one, else a float when it
    /// parses as one, else `null`.
    static MValue number(String s) {
        try { return new MValue.Int(Long.parseLong(s)); } catch (NumberFormatException notInt) { }
        if (s.matches("[+-]?(\\d+\\.?\\d*|\\.\\d+)([eE][+-]?\\d+)?|[+-]?(inf|infinity|nan)")) {
            try { return new MValue.Float(Double.parseDouble(s.replace("infinity", "Infinity").replace("inf", "Infinity").replace("nan", "NaN").replace("InfinityInfinity", "Infinity"))); }
            catch (NumberFormatException notFloat) { }
        }
        return null;
    }

    private static ANode parseCddlGroup(String text) {
        String trimmed = text.trim();
        String inner = trimmed.startsWith("{") && trimmed.endsWith("}") ? trimmed.substring(1, trimmed.length() - 1) : trimmed;
        MNode node = new MNode();
        for (String line : inner.split(",")) {
            line = line.trim();
            if (line.isEmpty()) continue;
            int colon = line.indexOf(':');
            if (colon < 0) continue;
            String key = stripQuotes(line.substring(0, colon).trim());
            String type = line.substring(colon + 1).trim();
            node.insert(key, switch (type) {
                case "tstr" -> new MValue.Text("");
                case "int" -> new MValue.Int(0);
                case "float", "float16" -> new MValue.Float(0.0);
                case "bool" -> new MValue.Bool(false);
                case "bstr" -> new MValue.Bytes(new byte[0]);
                default -> MValue.NULL;
            });
        }
        return ANode.of(node);
    }

    private static String stripQuotes(String s) {
        int start = 0, end = s.length();
        while (start < end && s.charAt(start) == '"') start++;
        while (end > start && s.charAt(end - 1) == '"') end--;
        return s.substring(start, end);
    }

    private static ANode parseYaml(String text) {
        MNode node = new MNode();
        for (String raw : text.split("\n")) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            int colon = line.indexOf(':');
            if (colon < 0) continue;
            String key = line.substring(0, colon).trim(), value = line.substring(colon + 1).trim();
            node.insert(key, value.isEmpty() ? MValue.NULL : yamlScalarValue(value));
        }
        return ANode.of(node);
    }

    private static MValue yamlScalarValue(String s) {
        if (s.equals("null") || s.equals("~")) return MValue.NULL;
        if (s.equals("true")) return new MValue.Bool(true);
        if (s.equals("false")) return new MValue.Bool(false);
        if (s.length() >= 2 && ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")))) return new MValue.Text(s.substring(1, s.length() - 1));
        MValue number = number(s);
        return number != null ? number : new MValue.Text(s);
    }

    private static ANode parseReadout(String text) {
        MNode node = new MNode();
        for (String raw : text.split("\n")) {
            String line = raw;
            while (line.startsWith("\t")) line = line.substring(1);
            if (line.isEmpty()) continue;
            int sep = line.indexOf(" : ");
            if (sep < 0) continue;
            node.insert(line.substring(0, sep).trim(), readoutValue(line.substring(sep + 3).trim()));
        }
        return ANode.of(node);
    }

    private static MValue readoutValue(String s) {
        if (s.equals("NULL")) return MValue.NULL;
        if (s.length() >= 2 && s.startsWith("'") && s.endsWith("'")) return new MValue.Text(s.substring(1, s.length() - 1));
        MValue number = number(s);
        if (number != null) return number;
        if (s.equals("true")) return new MValue.Bool(true);
        if (s.equals("false")) return new MValue.Bool(false);
        return new MValue.Text(s);
    }

    // -- Tree --

    /// A node as plain Java values, the bridge serde is in the
    /// reference: an MNode becomes an insertion-ordered map of
    /// `String`, `Long`, `Double`, `Boolean`, `null`, lists, and nested
    /// maps, typed exactly as the JSON vernacular types them; a PNode
    /// becomes the map its JSON rendering describes.
    public static java.lang.Object toTree(ANode node) {
        return jsonToTree(node instanceof ANode.M m ? mnodeToJson(m.node()) : pnodeToJson(((ANode.P) node).node()));
    }

    private static java.lang.Object jsonToTree(Json value) {
        if (value instanceof Json.Null) return null;
        if (value instanceof Json.Bool b) return b.value();
        if (value instanceof Json.Int i) return i.value();
        if (value instanceof Json.Float f) return f.value();
        if (value instanceof Json.Str s) return s.value();
        if (value instanceof Json.Array a) { List<java.lang.Object> items = new ArrayList<>(); for (Json item : a.items()) items.add(jsonToTree(item)); return items; }
        Map<String, java.lang.Object> map = new LinkedHashMap<>();
        for (Map.Entry<String, Json> e : ((Json.Object) value).entries()) map.put(e.getKey(), jsonToTree(e.getValue()));
        return map;
    }
}
