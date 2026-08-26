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
import java.util.List;
import java.util.Locale;

/// A record-coordinate window: a list of half-open intervals, where an
/// empty list means *all* records. This is the same window grammar the
/// `vectordata-rs` dataset-source parser accepts — no second spelling:
///
/// - `1M` — shorthand for `[0..1_000_000)`
/// - `0..1000`, `[0..1000)`, `0..1K`
/// - `[10k..]` — open end, `[..10k)` — open start
/// - `(10k..]` — exclusive start, `[..10k]` — inclusive end
/// - `[0..1K, 5K..6K]` — several intervals
///
/// Numbers accept `_` separators, `K`/`M`/`B`/`G`/`T` count suffixes,
/// ISO `KB`/`MB`/`GB`/`TB` and IEC `KiB`/`MiB`/`GiB`/`TiB` (or bare
/// `ki`/`mi`/`gi`/`ti`) size suffixes, and compound forms like `1g24m`.
///
/// An interval that selects no records is rejected at parse time: `,`
/// separates *intervals* and `..` separates *bounds*, so `0,1000` would
/// otherwise parse as a degenerate `[0..0)` beside `[0..1000)` and then
/// mean different things to a reader and a prefetch.
public record DSWindow(List<Interval> intervals) {

    /// Half-open interval `[minIncl, maxExcl)`. An unbounded end is
    /// represented as [Long#MAX_VALUE] and clamps to the facet.
    public record Interval(long minIncl, long maxExcl) {
        @Override public String toString() { return minIncl + ".." + maxExcl; }
    }

    /// The empty window, meaning every record.
    public static final DSWindow ALL = new DSWindow(List.of());

    public DSWindow { intervals = List.copyOf(intervals); }

    /// Whether no intervals are specified (meaning all records).
    public boolean isEmpty() { return intervals.isEmpty(); }

    @Override public String toString() {
        if (intervals.isEmpty()) return "ALL";
        StringBuilder text = new StringBuilder("[");
        for (int i = 0; i < intervals.size(); i++) { if (i > 0) text.append(", "); text.append(intervals.get(i)); }
        return text.append("]").toString();
    }

    /// Parses a window specification: a single interval or a
    /// comma-separated list, optionally wrapped in one outer `[...]`.
    /// A blank specification is [#ALL]. Malformed input and empty
    /// intervals fail with a [VectorDataException].
    public static DSWindow parse(String spec) {
        String text = spec == null ? "" : spec.trim();
        if (text.isEmpty()) return ALL;
        String inner = text.startsWith("[") && text.endsWith("]") ? text.substring(1, text.length() - 1) : text;
        List<Interval> intervals = new ArrayList<>();
        for (String part : inner.split(",", -1)) intervals.add(parseInterval(part.trim()));
        return new DSWindow(intervals);
    }

    private static Interval parseInterval(String raw) {
        String text = raw.trim();
        boolean leftExclusive = text.startsWith("(");
        boolean rightInclusive = text.endsWith("]");
        String inner = text.startsWith("[") || text.startsWith("(") ? text.substring(1) : text;
        inner = inner.endsWith(")") || inner.endsWith("]") ? inner.substring(0, inner.length() - 1) : inner;
        inner = inner.trim();
        int bounds = inner.indexOf("..");
        if (bounds >= 0) {
            String left = inner.substring(0, bounds).trim();
            String right = inner.substring(bounds + 2).trim();
            long minIncl = left.isEmpty() ? 0 : parseNumberWithSuffix(left);
            long maxExcl = right.isEmpty() ? Long.MAX_VALUE : parseNumberWithSuffix(right);
            try { if (leftExclusive && !left.isEmpty()) minIncl = Math.addExact(minIncl, 1); }
            catch (ArithmeticException e) { throw new VectorDataException("start overflow in interval '" + raw + "'"); }
            try { if (rightInclusive && !right.isEmpty()) maxExcl = Math.addExact(maxExcl, 1); }
            catch (ArithmeticException e) { throw new VectorDataException("end overflow in interval '" + raw + "'"); }
            return checkNonEmpty(new Interval(minIncl, maxExcl), text);
        }
        return checkNonEmpty(new Interval(0, parseNumberWithSuffix(inner)), text);
    }

    /// Rejects an interval that selects nothing, naming the likely
    /// cause: when the offending text carries no `..`, the mistake is
    /// almost always a comma where `..` belongs.
    private static Interval checkNonEmpty(Interval interval, String raw) {
        if (interval.maxExcl() > interval.minIncl()) return interval;
        if (!raw.contains("..")) throw new VectorDataException(
            "interval '" + raw + "' selects no records: a bare number means 0..N, so '" + raw + "' is [0.." + interval.maxExcl()
                + "). Bounds are separated by '..' and intervals by ',' — did you mean a '..' here?");
        throw new VectorDataException(
            "interval '" + raw + "' selects no records: end (" + interval.maxExcl() + ") must exceed start (" + interval.minIncl() + ")");
    }

    /// Parses a count with the `vectordata-rs` suffix grammar; see the
    /// class documentation for the accepted forms.
    public static long parseNumberWithSuffix(String spec) {
        String text = spec.replace("_", "");
        if (text.isEmpty()) throw new VectorDataException("empty number");
        Long compound = tryParseCompound(text);
        if (compound != null) return compound;
        String number = text;
        long multiplier = 1;
        if (text.endsWith("TiB")) { number = strip(text, 3); multiplier = 1L << 40; }
        else if (text.endsWith("GiB")) { number = strip(text, 3); multiplier = 1L << 30; }
        else if (text.endsWith("MiB")) { number = strip(text, 3); multiplier = 1L << 20; }
        else if (text.endsWith("KiB")) { number = strip(text, 3); multiplier = 1L << 10; }
        else if (text.endsWith("TB") || text.endsWith("tb")) { number = strip(text, 2); multiplier = 1_000_000_000_000L; }
        else if (text.endsWith("GB") || text.endsWith("gb")) { number = strip(text, 2); multiplier = 1_000_000_000L; }
        else if (text.endsWith("MB") || text.endsWith("mb")) { number = strip(text, 2); multiplier = 1_000_000L; }
        else if (text.endsWith("KB") || text.endsWith("kb")) { number = strip(text, 2); multiplier = 1_000L; }
        else if (text.endsWith("ti") || text.endsWith("Ti")) { number = strip(text, 2); multiplier = 1L << 40; }
        else if (text.endsWith("gi") || text.endsWith("Gi")) { number = strip(text, 2); multiplier = 1L << 30; }
        else if (text.endsWith("mi") || text.endsWith("Mi")) { number = strip(text, 2); multiplier = 1L << 20; }
        else if (text.endsWith("ki") || text.endsWith("Ki")) { number = strip(text, 2); multiplier = 1L << 10; }
        else if (!text.isEmpty()) {
            char last = text.charAt(text.length() - 1);
            switch (last) {
                case 'K', 'k' -> { number = strip(text, 1); multiplier = 1_000L; }
                case 'M', 'm' -> { number = strip(text, 1); multiplier = 1_000_000L; }
                case 'B', 'b', 'G', 'g' -> { number = strip(text, 1); multiplier = 1_000_000_000L; }
                case 'T', 't' -> { number = strip(text, 1); multiplier = 1_000_000_000_000L; }
                default -> { }
            }
        }
        try { return Math.multiplyExact(Long.parseLong(number), multiplier); }
        catch (NumberFormatException | ArithmeticException e) { throw new VectorDataException("invalid number '" + number + "': " + e.getMessage()); }
    }

    private static String strip(String text, int suffix) { return text.substring(0, text.length() - suffix); }

    /// Parses a compound suffix string like `1g24m` — digit/suffix
    /// terms summed together — or returns `null` when the text is not
    /// compound-shaped.
    private static Long tryParseCompound(String text) {
        if (text.length() < 4) return null;
        boolean hasCompound = false;
        for (int i = 1; i < text.length(); i++) {
            if (Character.isLetter(text.charAt(i - 1)) && Character.isDigit(text.charAt(i))) { hasCompound = true; break; }
        }
        if (!hasCompound) return null;
        List<String> terms = new ArrayList<>();
        int start = 0;
        for (int i = 1; i < text.length(); i++) {
            if (Character.isDigit(text.charAt(i)) && Character.isLetter(text.charAt(i - 1))) { terms.add(text.substring(start, i)); start = i; }
        }
        terms.add(text.substring(start));
        if (terms.size() < 2) return null;
        long total = 0;
        for (String term : terms) {
            if (term.isEmpty() || Character.isDigit(term.charAt(term.length() - 1))) return null;
            int suffixStart = 0;
            while (suffixStart < term.length() && !Character.isLetter(term.charAt(suffixStart))) suffixStart++;
            long value;
            try { value = Long.parseLong(term.substring(0, suffixStart)); } catch (NumberFormatException e) { return null; }
            long multiplier = switch (term.substring(suffixStart).toLowerCase(Locale.ROOT)) {
                case "ti" -> 1L << 40; case "gi" -> 1L << 30; case "mi" -> 1L << 20; case "ki" -> 1L << 10;
                case "t" -> 1_000_000_000_000L; case "g", "b" -> 1_000_000_000L; case "m" -> 1_000_000L; case "k" -> 1_000L;
                default -> 0;
            };
            if (multiplier == 0) return null;
            total += value * multiplier;
        }
        return total;
    }
}
