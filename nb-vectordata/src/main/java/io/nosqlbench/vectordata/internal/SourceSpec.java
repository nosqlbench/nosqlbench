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
/// a path, an optional slab namespace, an optional ordinal window, and
/// an optional declared cardinality —
///
/// | Spelling               | Meaning                                  |
/// |------------------------|------------------------------------------|
/// | `a.u8`                 | a file; cardinality discovered by opening |
/// | `a.u8=4194304`         | cardinality declared                     |
/// | `a.u8[0..1M]`          | a slice; cardinality implied             |
/// | `a.u8[0..1M]=1M`       | implied *and* declared, checked          |
/// | `m.slab:content`       | a namespace within a slab                |
/// | `m.slab:ns:[0..1K]`    | namespace and window                     |
///
/// The `=<count>` suffix is taken from the end and accepts the same
/// suffixes intervals do, under two restrictions. A source containing
/// `?` is never split on `=`, since that is the key/value separator
/// inside a URL query string — such a source declares its cardinality
/// by window instead. And an `=` whose tail does not parse as a positive
/// count stays in the path: `weird=name.u8` is a filename, not a
/// malformed count, so recognition is positive rather than a rule the
/// path has to escape. A namespace is the text after the last `:` when
/// the path before it carries an extension and the text after names no
/// directory, so a URL scheme or a drive letter is never mistaken for
/// one.
public record SourceSpec(String path, String namespace, DSWindow window, String windowText, Long declaredCount) {

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
        String pathPart = split[0];
        // `m.slab:ns:[0..1K]` — the colon before the window is a separator, not a namespace.
        if (split[1] != null && pathPart.endsWith(":")) pathPart = pathPart.substring(0, pathPart.length() - 1);
        String[] located = splitNamespace(pathPart);
        DSWindow window = split[1] == null ? DSWindow.ALL : DSWindow.parse(split[1]);
        return new SourceSpec(located[0], located[1], window, split[1], count);
    }

    /// Renders the spelling back: path, then `:namespace`, then
    /// `[window]`, then `=count`.
    public String render() {
        StringBuilder text = new StringBuilder(path);
        if (namespace != null) text.append(':').append(namespace);
        if (windowText != null) text.append('[').append(windowText).append(']');
        if (declaredCount != null) text.append('=').append(declaredCount);
        return text.toString();
    }

    /// The path with its namespace, the form a slab is addressed by.
    public String locator() { return namespace == null ? path : path + ":" + namespace; }

    /// This spec with its path replaced — the namespace, window, and
    /// count travel with it, which is how a relative source becomes an
    /// absolute one without losing what it said.
    public SourceSpec withPath(String replacement) { return new SourceSpec(replacement, namespace, window, windowText, declaredCount); }

    /// This spec with a namespace, for a source that named none and
    /// whose declaration supplies one beside it.
    public SourceSpec withNamespace(String replacement) { return new SourceSpec(path, replacement, window, windowText, declaredCount); }

    private static Long tryCount(String tail) {
        try { long value = DSWindow.parseNumberWithSuffix(tail); return value > 0 ? value : null; }
        catch (VectorDataException notACount) { return null; }
    }

    /// Splits `path:namespace` — the text after the last `:` is a
    /// namespace when the text before it contains a `.` and the text
    /// after is non-empty and names no directory. Returns
    /// `{path, namespaceOrNull}`.
    static String[] splitNamespace(String source) {
        int colon = source.lastIndexOf(':');
        if (colon > 0) {
            String before = source.substring(0, colon), after = source.substring(colon + 1);
            if (before.contains(".") && !after.isEmpty() && !after.contains("/") && !after.contains("\\")) return new String[] {before, after};
        }
        return new String[] {source, null};
    }

    /// A locator without its namespace: what names the file.
    public static String stripNamespace(String locator) { return splitNamespace(locator)[0]; }

    /// A locator's namespace, or `null` for the default.
    public static String namespaceOf(String locator) { return splitNamespace(locator)[1]; }

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
