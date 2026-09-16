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

/// A dataset spec split into its head and its selector:
/// `<dataset>` or `<dataset>:<selector>`, where the head is a catalog
/// name, a path, or a URL exactly as written, and the selector is a
/// [ProfileSelector] or `null` when the spec names none — `default`
/// on every surface.
///
/// The head is found by its **shape**, and the selector begins at the
/// first colon after it: a URL's `://` and port belong to the head, as
/// does a Windows drive letter, and a catalog name holds no colon. So
/// `https://host:8080/ds` has no selector, `https://host/ds:10m`
/// selects `10m`, and `tessera:profile=^a:b$` selects on the regex.
/// A malformed selector is a selector error naming its position in
/// the spec, never a dataset that could not be found.
public record DatasetSpec(String head, ProfileSelector selector) {

    /// Splits a spec by the head's shape without parsing the selector —
    /// what completion needs while a selector is still being typed.
    /// The second element is everything after the head's colon, possibly
    /// empty, or `null` when there is no colon.
    public static String[] splitHead(String spec) {
        int at = headLength(spec);
        return at < 0 ? new String[] {spec, null} : new String[] {spec.substring(0, at), spec.substring(at + 1)};
    }

    /// Splits by the head's shape and parses what follows.
    public static DatasetSpec parse(String spec) {
        String text = spec == null ? "" : spec.trim();
        int at = headLength(text);
        String head = at < 0 ? text : text.substring(0, at);
        String tail = at < 0 ? "" : text.substring(at + 1);
        if (tail.isBlank()) return new DatasetSpec(head, null);
        try { return new DatasetSpec(head, ProfileSelector.parse(tail)); }
        catch (SelectionException e) { throw e.shifted(head.length() + 1); }
    }

    /// Whether the spec names a selector.
    public boolean hasSelector() { return selector != null; }

    /// The selector text to hand a surface: what was written, or `null`
    /// for `default`.
    public String selectorText() { return selector == null ? null : selector.text(); }

    /// Where the head of a spec ends: the index of the colon that starts
    /// the selector, or `-1` when there is no selector.
    static int headLength(String spec) {
        // URL: scheme "://" authority [/path]
        int schemeEnd = spec.indexOf("://");
        if (schemeEnd > 0 && isScheme(spec.substring(0, schemeEnd))) {
            int after = schemeEnd + 3;
            int slash = spec.indexOf('/', after);
            int pathStart = slash < 0 ? spec.length() : slash;
            // A colon inside the authority is a port; the selector can
            // only start in the path.
            int colon = spec.indexOf(':', pathStart);
            return colon;
        }
        // Windows drive letter: "X:\" or "X:/"
        if (spec.length() >= 3 && Character.isLetter(spec.charAt(0)) && spec.charAt(0) < 128 && spec.charAt(1) == ':'
            && (spec.charAt(2) == '\\' || spec.charAt(2) == '/')) {
            return spec.indexOf(':', 3);
        }
        return spec.indexOf(':');
    }

    private static boolean isScheme(String text) {
        if (text.isEmpty() || !Character.isLetter(text.charAt(0)) || text.charAt(0) >= 128) return false;
        for (char c : text.toCharArray()) if (!((c < 128 && Character.isLetterOrDigit(c)) || c == '+' || c == '-' || c == '.')) return false;
        return true;
    }
}
