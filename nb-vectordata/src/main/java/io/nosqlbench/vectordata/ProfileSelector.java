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
package io.nosqlbench.vectordata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/// A profile selector: the language after the colon in a dataset spec.
///
/// `dataset:10m` names one profile, as it always has. `dataset:size=10m,
/// predicates=uniform*` names the *set* of profiles whose attributes
/// match, and `or(10m,20m)`, `not(family=uniform)`, `selectivity=1e-3..1e-2`
/// compose atoms over a profile's automatic `profile` tag, its
/// structural fields, and its declared `attributes:`.
///
/// The grammar:
///
/// ```
/// selector  := name | expr
/// expr      := term { "," term }                 -- "," is AND
/// term      := "and" "(" expr ")" | "or" "(" expr ")" | "not" "(" expr ")" | atom | name
/// atom      := key op value
/// key       := ident { "." ident }                -- dotted keys reach into a map-valued attribute
/// op        := "=" | "!=" | "<" | "<=" | ">" | ">="
/// value     := quoted | bare
/// ident     := [A-Za-z0-9_][A-Za-z0-9_.-]*
/// ```
///
/// A value is read by its spelling, in this order: `^…`/`…$` a regular
/// expression over the whole canonical text; `*`, `?`, or `[` a glob;
/// `lo..hi` a half-open numeric interval; a number, possibly
/// count-suffixed under the window grammar (`10m`, `128mi`, `1e-3`);
/// `true`/`false`; anything else, or anything quoted, a literal.
/// Every reading folds case. An absent attribute matches nothing under
/// any operator; a list matches when any element does; a map matches
/// nothing as a whole and is reached with a dotted key. The regular
/// expression dialect is the RE2 subset the reference evaluates:
/// a lookaround or a backreference is refused rather than accepted
/// here and rejected there.
///
/// This is the one parser every surface calls; the surfaces decide what
/// a set means to them — [#resolve] for a set, [#resolveOne] for one.
public final class ProfileSelector {
    private final Expr expr;
    private final String text;

    private ProfileSelector(Expr expr, String text) { this.expr = expr; this.text = text; }

    /// Parses a selector: a bare profile name or an expression. A
    /// malformed selector is a [SelectionException] of kind
    /// [SelectionException.Kind#SYNTAX] naming the position.
    public static ProfileSelector parse(String text) {
        Parser parser = new Parser(text);
        parser.skipWhitespace();
        if (parser.atEnd()) throw parser.error("empty selector");
        Expr expr = parser.parseExpr();
        parser.skipWhitespace();
        if (!parser.atEnd()) throw parser.error("unexpected '" + parser.restChar() + "'");
        return new ProfileSelector(expr, text.trim());
    }

    /// The literal profile name this selector is, if it is one bare
    /// name and nothing else. Surfaces that print a profile name use
    /// it; every other selector is a set.
    public Optional<String> bareName() {
        if (expr instanceof Atom atom && "profile".equals(atom.key()) && atom.op() == Op.EQ
            && atom.value() instanceof Value.Literal literal && text.equalsIgnoreCase(literal.text()))
            return Optional.of(text);
        return Optional.empty();
    }

    /// Whether this selector matches the profile described by `facts`.
    public boolean matches(ProfileFacts facts) { return expr.eval(facts); }

    /// The names of the profiles in `facts` this selector matches, in
    /// the order given.
    public List<String> select(Collection<ProfileFacts> facts) {
        List<String> names = new ArrayList<>();
        for (ProfileFacts profile : facts) if (matches(profile)) names.add(profile.name());
        return names;
    }

    /// The selector as written, trimmed.
    public String text() { return text; }

    @Override public String toString() { return text; }

    /// The profiles a selector names, in the order `facts` lists them.
    /// A `null` selector means `default`, `profile=*` means all, and a
    /// selector matching nothing fails naming what was on offer. An
    /// empty or blank selector is a syntax error, as it is in the
    /// reference: no selector is spelled by giving none.
    public static List<String> resolve(String selector, List<ProfileFacts> facts) {
        if (selector == null) {
            for (ProfileFacts profile : facts) if ("default".equals(profile.name())) return List.of("default");
            List<String> names = new ArrayList<>();
            for (ProfileFacts profile : facts) names.add(profile.name());
            throw SelectionException.noDefault(names);
        }
        ProfileSelector parsed = parse(selector);
        List<String> names = parsed.select(facts);
        if (names.isEmpty()) throw SelectionException.noMatch(parsed.text(), facts);
        return names;
    }

    /// The one profile a selector names, for a surface that takes a
    /// single profile. More than one match fails, never a silent first.
    public static String resolveOne(String selector, List<ProfileFacts> facts) {
        List<String> names = resolve(selector, facts);
        if (names.size() == 1) return names.get(0);
        throw SelectionException.ambiguous(selector == null ? "" : selector.trim(), names);
    }

    // ── Values ──────────────────────────────────────────────────────────

    /// A comparison operator.
    enum Op {
        EQ("="), NE("!="), LT("<"), LE("<="), GT(">"), GE(">=");
        final String text;
        Op(String text) { this.text = text; }
        boolean isComparison() { return this == LT || this == LE || this == GT || this == GE; }
    }

    /// How a value was read from its spelling.
    sealed interface Value {
        record Regex(Pattern pattern) implements Value { }
        record Glob(String pattern) implements Value { }
        record Interval(double lo, double hi) implements Value { }
        record Number(double value) implements Value { }
        record Bool(boolean value) implements Value { }
        record Literal(String text) implements Value { }
    }

    private static final Pattern FLOAT = Pattern.compile("[+-]?(?:\\d+\\.?\\d*|\\.\\d+)(?:[eE][+-]?\\d+)?");
    /// Constructs outside the RE2 subset: lookaround, atomic groups,
    /// and backreferences by number or name.
    private static final Pattern OUTSIDE_RE2 = Pattern.compile("\\(\\?(?:=|!|<=|<!|>)|\\\\(?:[1-9]|k<)");

    /// A number as a selector spells one: a float, or a count with a
    /// suffix under the window grammar (`10m`, `128mi`, `100k`),
    /// whichever case the suffix is written in. `null` when the text is
    /// not a number.
    public static Double parseNumber(String text) {
        String s = text == null ? "" : text.trim();
        if (s.isEmpty()) return null;
        if (FLOAT.matcher(s).matches()) {
            try { return Double.parseDouble(s); } catch (NumberFormatException ignored) { }
        }
        int digitsEnd = 0;
        while (digitsEnd < s.length() && (Character.isDigit(s.charAt(digitsEnd)) || s.charAt(digitsEnd) == '.' || s.charAt(digitsEnd) == '_')) digitsEnd++;
        String unit = s.substring(digitsEnd);
        // `128mi` → `128Mi`, `10m` → `10M`: the grammar's mixed cases.
        String mixed = unit.isEmpty() ? s
            : s.substring(0, digitsEnd) + Character.toUpperCase(unit.charAt(0)) + unit.substring(1).toLowerCase(Locale.ROOT);
        for (String candidate : List.of(s, s.toLowerCase(Locale.ROOT), s.toUpperCase(Locale.ROOT), mixed)) {
            try { return (double) DSWindow.parseNumberWithSuffix(candidate); } catch (VectorDataException ignored) { }
        }
        return null;
    }

    /// Reads a bare value by its spelling, in the order the reference
    /// fixes.
    private static Value readBareValue(String raw, Op op) {
        if (raw.startsWith("^") || raw.endsWith("$")) {
            if (OUTSIDE_RE2.matcher(raw).find())
                throw new IllegalArgumentException("bad regular expression '" + raw + "': lookaround and backreferences are outside the RE2 subset");
            try { return new Value.Regex(Pattern.compile(raw, Pattern.CASE_INSENSITIVE)); }
            catch (PatternSyntaxException e) { throw new IllegalArgumentException("bad regular expression '" + raw + "': " + e.getDescription()); }
        }
        if (raw.contains("*") || raw.contains("?") || raw.contains("[")) return new Value.Glob(raw.toLowerCase(Locale.ROOT));
        int dots = raw.indexOf("..");
        if (dots >= 0) {
            Double lo = parseNumber(raw.substring(0, dots)); Double hi = parseNumber(raw.substring(dots + 2));
            if (lo != null && hi != null) {
                if (op.isComparison()) throw new IllegalArgumentException("an interval '" + raw + "' takes = or !=, not a comparison");
                return new Value.Interval(lo, hi);
            }
        }
        Double number = parseNumber(raw);
        if (number != null) return new Value.Number(number);
        if (op.isComparison()) throw new IllegalArgumentException("'" + raw + "' is not a number, and " + op.text + " compares numbers");
        String lower = raw.toLowerCase(Locale.ROOT);
        return switch (lower) {
            case "true" -> new Value.Bool(true);
            case "false" -> new Value.Bool(false);
            default -> new Value.Literal(lower);
        };
    }

    // ── Expressions ─────────────────────────────────────────────────────

    sealed interface Expr {
        boolean eval(ProfileFacts facts);
    }
    record And(List<Expr> terms) implements Expr {
        public boolean eval(ProfileFacts facts) { for (Expr term : terms) if (!term.eval(facts)) return false; return true; }
    }
    record Or(List<Expr> terms) implements Expr {
        public boolean eval(ProfileFacts facts) { for (Expr term : terms) if (term.eval(facts)) return true; return false; }
    }
    record Not(List<Expr> terms) implements Expr {
        public boolean eval(ProfileFacts facts) { return !new And(terms).eval(facts); }
    }
    record Atom(String key, Op op, Value value) implements Expr {
        public boolean eval(ProfileFacts facts) {
            // Absent matches nothing under any operator.
            Object found = lookup(facts, key);
            if (found == null) return false;
            // A list matches when any element does; a map as a whole
            // matches nothing.
            if (found instanceof List<?> items) { for (Object item : items) if (scalarMatches(item)) return true; return false; }
            if (found instanceof Map<?, ?>) return false;
            return scalarMatches(found);
        }

        private boolean scalarMatches(Object scalar) {
            boolean hit = switch (value) {
                case Value.Number number -> {
                    Double x = numericValue(scalar);
                    if (op == Op.EQ || op == Op.NE) yield x != null && x == number.value();
                    if (x == null) yield false;
                    yield switch (op) {
                        case LT -> x < number.value(); case LE -> x <= number.value();
                        case GT -> x > number.value(); case GE -> x >= number.value();
                        default -> false;
                    };
                }
                case Value.Interval interval -> {
                    if (op.isComparison()) yield false;
                    Double x = numericValue(scalar);
                    yield x != null && x >= interval.lo() && x < interval.hi();
                }
                case Value.Bool flag -> scalar instanceof Boolean actual ? actual == flag.value()
                    : ProfileFacts.canonicalText(scalar) != null && ProfileFacts.canonicalText(scalar).equalsIgnoreCase(String.valueOf(flag.value()));
                case Value.Regex regex -> { String t = ProfileFacts.canonicalText(scalar); yield t != null && regex.pattern().matcher(t).find(); }
                case Value.Glob glob -> { String t = ProfileFacts.canonicalText(scalar); yield t != null && globMatch(glob.pattern(), t.toLowerCase(Locale.ROOT)); }
                case Value.Literal literal -> { String t = ProfileFacts.canonicalText(scalar); yield t != null && t.equalsIgnoreCase(literal.text()); }
            };
            // A comparison against a pattern, boolean, or literal is a
            // parse error already; only `=` and `!=` reach here for them.
            if (op.isComparison()) return value instanceof Value.Number && hit;
            return op == Op.EQ ? hit : !hit;
        }
    }

    /// Looks a key up: structural keys first, then the attribute map
    /// with a dotted path into map-valued attributes, keys folding case.
    private static Object lookup(ProfileFacts facts, String key) {
        switch (key) {
            case "profile": return facts.name();
            case "base_count": return facts.baseCount();
            case "maxk": return facts.maxk();
            case "partition": return facts.partition();
            case "inherits": return facts.inherits();
            default: break;
        }
        String[] parts = key.split("\\.", -1);
        Object current = null;
        for (Map.Entry<String, Object> attribute : facts.attributes().entrySet())
            if (attribute.getKey().equalsIgnoreCase(parts[0])) { current = attribute.getValue(); break; }
        for (int i = 1; i < parts.length && current != null; i++) {
            if (!(current instanceof Map<?, ?> map)) return null;
            Object next = null;
            for (Map.Entry<?, ?> entry : map.entrySet())
                if (String.valueOf(entry.getKey()).equalsIgnoreCase(parts[i])) { next = entry.getValue(); break; }
            current = next;
        }
        return current;
    }

    /// A scalar's numeric value: a YAML number, or a string that reads
    /// as a number under the count rule (`size: 10m`).
    private static Double numericValue(Object scalar) {
        if (scalar instanceof Number number) return number.doubleValue();
        if (scalar instanceof String text) return parseNumber(text);
        return null;
    }

    /// Glob over the whole text: `*` any run, `?` one character, `[...]`
    /// a class with ranges and a leading `!` or `^` for negation.
    public static boolean globMatch(String pattern, String text) {
        return glob(pattern.toCharArray(), 0, text.toCharArray(), 0);
    }

    private static boolean glob(char[] p, int pi, char[] t, int ti) {
        if (pi >= p.length) return ti >= t.length;
        char c = p[pi];
        if (c == '*') { for (int i = ti; i <= t.length; i++) if (glob(p, pi + 1, t, i)) return true; return false; }
        if (c == '?') return ti < t.length && glob(p, pi + 1, t, ti + 1);
        if (c == '[') {
            int close = -1;
            for (int i = pi; i < p.length; i++) if (p[i] == ']') { close = i; break; }
            if (close < 0) return ti < t.length && t[ti] == '[' && glob(p, pi + 1, t, ti + 1);
            if (ti >= t.length) return false;
            int start = pi + 1; boolean negate = false;
            if (start < close && (p[start] == '!' || p[start] == '^')) { negate = true; start++; }
            boolean hit = false;
            for (int i = start; i < close; ) {
                if (i + 2 < close && p[i + 1] == '-') { if (p[i] <= t[ti] && t[ti] <= p[i + 2]) hit = true; i += 3; }
                else { if (p[i] == t[ti]) hit = true; i++; }
            }
            return hit != negate && glob(p, close + 1, t, ti + 1);
        }
        return ti < t.length && t[ti] == c && glob(p, pi + 1, t, ti + 1);
    }

    // ── Parsing ─────────────────────────────────────────────────────────

    private static final class Parser {
        private final String s; private int pos;
        Parser(String s) { this.s = s == null ? "" : s; }
        boolean atEnd() { return pos >= s.length(); }
        char restChar() { return atEnd() ? '\0' : s.charAt(pos); }
        SelectionException error(String message) { return SelectionException.syntax(pos, message); }
        void skipWhitespace() { while (!atEnd() && Character.isWhitespace(s.charAt(pos))) pos++; }
        boolean eat(String literal) { if (s.startsWith(literal, pos)) { pos += literal.length(); return true; } return false; }
        private static boolean isIdentChar(char c) { return (c < 128 && Character.isLetterOrDigit(c)) || c == '_' || c == '.' || c == '-'; }
        String ident() {
            int start = pos;
            while (!atEnd() && isIdentChar(s.charAt(pos))) pos++;
            return pos > start ? s.substring(start, pos) : null;
        }

        /// `expr := term { "," term }` — a comma is AND.
        Expr parseExpr() {
            List<Expr> terms = new ArrayList<>();
            terms.add(parseTerm());
            while (true) {
                skipWhitespace();
                if (eat(",")) { skipWhitespace(); terms.add(parseTerm()); } else break;
            }
            return terms.size() == 1 ? terms.get(0) : new And(terms);
        }

        /// `term := and(...) | or(...) | not(...) | atom | name`
        Expr parseTerm() {
            skipWhitespace();
            int start = pos;
            String word = ident();
            if (word == null) throw error("expected a profile name or a `key op value` atom");
            skipWhitespace();
            // A junction is a keyword immediately followed by `(`.
            String lower = word.toLowerCase(Locale.ROOT);
            if (!atEnd() && s.charAt(pos) == '(' && (lower.equals("and") || lower.equals("or") || lower.equals("not"))) {
                pos++;
                skipWhitespace();
                Expr inner = parseExpr();
                List<Expr> terms = inner instanceof And and ? and.terms() : List.of(inner);
                skipWhitespace();
                if (!eat(")")) throw error("expected ')' to close `" + lower + "(`");
                return switch (lower) { case "and" -> new And(terms); case "or" -> new Or(terms); default -> new Not(terms); };
            }
            // An atom carries an operator; a bare name is `profile=<name>`.
            Op op = eat("!=") ? Op.NE : eat("<=") ? Op.LE : eat(">=") ? Op.GE : eat("=") ? Op.EQ : eat("<") ? Op.LT : eat(">") ? Op.GT : null;
            if (op == null) {
                skipWhitespace();
                if (!(atEnd() || s.charAt(pos) == ',' || s.charAt(pos) == ')')) {
                    pos = start + word.length();
                    throw error("expected an operator after '" + word + "' (one of = != < <= > >=)");
                }
                return new Atom("profile", Op.EQ, new Value.Literal(word.toLowerCase(Locale.ROOT)));
            }
            skipWhitespace();
            Value value = parseValue(op);
            return new Atom(word.toLowerCase(Locale.ROOT), op, value);
        }

        /// `value := quoted | bare` — bare runs to the next `,` or `)`.
        Value parseValue(Op op) {
            int start = pos;
            if (!atEnd() && (s.charAt(pos) == '\'' || s.charAt(pos) == '"')) {
                char quote = s.charAt(pos); pos++;
                int close = s.indexOf(quote, pos);
                if (close < 0) throw error("unterminated quoted value");
                String body = s.substring(pos, close); pos = close + 1;
                return new Value.Literal(body.toLowerCase(Locale.ROOT));
            }
            int end = pos;
            while (end < s.length() && s.charAt(end) != ',' && s.charAt(end) != ')') end++;
            String raw = s.substring(start, end).trim();
            if (raw.isEmpty()) throw error("expected a value");
            // `size==10m` is a slip, not a value of `=10m`; a value that
            // means an operator character literally is quoted.
            char first = raw.charAt(0);
            if (first == '=' || first == '<' || first == '>' || first == '!')
                throw error("a value cannot begin with '" + first + "'; quote it to mean it literally");
            pos = end;
            try { return readBareValue(raw, op); }
            catch (IllegalArgumentException e) { throw SelectionException.syntax(start, e.getMessage()); }
        }
    }
}
