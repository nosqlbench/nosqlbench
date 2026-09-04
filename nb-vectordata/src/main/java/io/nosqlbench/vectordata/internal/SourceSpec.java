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
package io.nosqlbench.vectordata.internal;

import io.nosqlbench.vectordata.DSWindow;
import io.nosqlbench.vectordata.VectorDataException;

/// A parsed facet source string, in the grammar `vectordata-rs` parses:
/// a path, an optional ordinal window, and an optional declared
/// cardinality, in increasing specificity —
///
/// | Spelling         | Cardinality                      |
/// |------------------|----------------------------------|
/// | `a.u8`           | discovered by opening the file   |
/// | `a.u8=4194304`   | declared                         |
/// | `a.u8[0..1M]`    | implied by the interval          |
/// | `a.u8[0..1M]=1M` | implied *and* declared, checked  |
///
/// The `=<count>` suffix is taken from the end and accepts the same
/// suffixes intervals do, under two restrictions. A source containing
/// `?` is never split on `=`, since that is the key/value separator
/// inside a URL query string — such a source declares its cardinality
/// by window instead. And an `=` whose tail does not parse as a positive
/// count stays in the path: `weird=name.u8` is a filename, not a
/// malformed count, so recognition is positive rather than a rule the
/// path has to escape.
public record SourceSpec(String path, DSWindow window, String windowText, Long declaredCount) {

    /// Parses a source string. Only a suffix that *looks* like a window
    /// and is malformed fails — a plain path always passes through — so
    /// an error here names the broken window rather than turning it
    /// into "no such file".
    public static SourceSpec parse(String raw) {
        String text = raw.trim();
        Long count = null;
        if (text.indexOf('?') < 0) {
            int eq = text.lastIndexOf('=');
            if (eq > 0 && eq < text.length() - 1) {
                Long parsed = tryCount(text.substring(eq + 1));
                if (parsed != null) { count = parsed; text = text.substring(0, eq).trim(); }
            }
        }
        String[] split = splitWindowSuffix(text);
        DSWindow window = split[1] == null ? DSWindow.ALL : DSWindow.parse(split[1]);
        return new SourceSpec(split[0], window, split[1], count);
    }

    /// Renders the spelling back: path, then `[window]`, then `=count`.
    public String render() {
        StringBuilder text = new StringBuilder(path);
        if (windowText != null) text.append('[').append(windowText).append(']');
        if (declaredCount != null) text.append('=').append(declaredCount);
        return text.toString();
    }

    /// This spec with its path replaced — the window and count travel
    /// with it, which is how a relative source becomes an absolute one
    /// without losing what it said.
    public SourceSpec withPath(String replacement) { return new SourceSpec(replacement, window, windowText, declaredCount); }

    private static Long tryCount(String tail) {
        try { long value = DSWindow.parseNumberWithSuffix(tail); return value > 0 ? value : null; }
        catch (VectorDataException notACount) { return null; }
    }

    /// Splits the documented window-suffix sugar off a source string —
    /// `base.fvec[0..1M)` names the file plus a record window, with
    /// either bracket kind on either side. The outer delimiters are
    /// structural: they separate the path from the window and do not
    /// affect interval bound semantics. Returns `{path, windowOrNull}`.
    static String[] splitWindowSuffix(String source) {
        if (!source.endsWith("]") && !source.endsWith(")")) return new String[] {source, null};
        int bracket = source.indexOf('['); int paren = source.indexOf('(');
        int open = bracket < 0 ? paren : paren < 0 ? bracket : Math.min(bracket, paren);
        if (open <= 0) return new String[] {source, null};
        String inner = source.substring(open + 1, source.length() - 1);
        try { DSWindow.parse(inner); }
        catch (VectorDataException malformed) {
            throw new VectorDataException("source '" + source + "' has a malformed window: " + malformed.getMessage());
        }
        return new String[] {source.substring(0, open), inner};
    }
}
