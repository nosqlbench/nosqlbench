/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.nosqlbench.adapter.cqld4.opdispensers;

import io.nosqlbench.virtdata.core.templates.ParsedTemplateString;
import io.nosqlbench.virtdata.core.templates.PreparedFragment;

/// A prepared statement whose text varies by **form**: when a bind
/// point's value is a [PreparedFragment], the fragment's text takes
/// the bind point's place in the statement and its values take the
/// place of that one bind value. The statement is prepared once per
/// distinct form and every later cycle of that form binds through it.
///
/// The form key identifies the statement for the op template it came
/// from, so a template with one fragment bind point is keyed by the
/// fragment's own key — the same string instance each cycle, from a
/// producer that caches its forms — and a cycle allocates no key.
public final class DynamicForms {
    private DynamicForms() { }

    /// Whether any bind value is a fragment.
    public static boolean hasFragment(Object[] values) {
        for (Object value : values) if (value instanceof PreparedFragment) return true;
        return false;
    }

    /// The key of the form these values describe: the one fragment's
    /// key, or the fragments' keys joined when there are several.
    public static String formKey(Object[] values) {
        String key = null;
        StringBuilder joined = null;
        for (Object value : values) {
            if (!(value instanceof PreparedFragment fragment)) continue;
            if (key == null) { key = fragment.formKey(); continue; }
            if (joined == null) joined = new StringBuilder(key);
            joined.append('|').append(fragment.formKey());
        }
        return joined == null ? key : joined.toString();
    }

    /// The statement text of a form: each fragment's text at its bind
    /// point, a `?` at every other.
    public static String text(ParsedTemplateString template, Object[] values) {
        String[] spans = template.getSpans();
        StringBuilder text = new StringBuilder(spans[0]);
        for (int i = 1; i < spans.length; i += 2) {
            Object value = values[(i - 1) / 2];
            text.append(value instanceof PreparedFragment fragment ? fragment.text() : "?");
            text.append(spans[i + 1]);
        }
        return text.toString();
    }

    /// The bind values with each fragment's values spread in its place.
    public static Object[] spread(Object[] values) {
        int count = 0;
        for (Object value : values) count += value instanceof PreparedFragment fragment ? fragment.values().length : 1;
        Object[] flat = new Object[count];
        int at = 0;
        for (Object value : values) {
            if (value instanceof PreparedFragment fragment) { Object[] inner = fragment.values(); System.arraycopy(inner, 0, flat, at, inner.length); at += inner.length; }
            else flat[at++] = value;
        }
        return flat;
    }
}
