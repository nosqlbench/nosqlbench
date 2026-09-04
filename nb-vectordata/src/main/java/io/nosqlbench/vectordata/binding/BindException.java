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
package io.nosqlbench.vectordata.binding;

import io.nosqlbench.vectordata.VectorDataException;

import java.util.List;

/// What went wrong preparing or binding. The [Kind] is matchable, and
/// each carries what a caller needs to act, not only text to print.
public class BindException extends VectorDataException {
    public enum Kind {
        /// The facet could not be read.
        FACET,
        /// No record was available to learn the layout from.
        NO_LAYOUT,
        /// A record could not be walked, or is not what the binder was compiled for.
        RECORD,
        /// A parameter names a field the facet does not have.
        NO_SUCH_FIELD,
        /// A form was asked for that this facet does not offer.
        NO_SUCH_FORM
    }

    private final Kind kind;
    private final String name;
    private final List<String> available;

    private BindException(Kind kind, String message, String name, List<String> available) {
        super(message);
        this.kind = kind; this.name = name; this.available = List.copyOf(available);
    }

    public Kind kind() { return kind; }
    /// The field or form asked for, for `NO_SUCH_FIELD` and `NO_SUCH_FORM`;
    /// the facet, for `NO_LAYOUT`.
    public String name() { return name; }
    /// What the facet does have, in order, for `NO_SUCH_FIELD` and
    /// `NO_SUCH_FORM` — empty for a form when only the implicit one exists.
    public List<String> available() { return available; }

    public static BindException facet(String message) { return new BindException(Kind.FACET, message, null, List.of()); }
    public static BindException noLayout(String facet) { return new BindException(Kind.NO_LAYOUT, facet + ": no record to learn the field layout from", facet, List.of()); }
    public static BindException record(String message) { return new BindException(Kind.RECORD, message, null, List.of()); }
    public static BindException noSuchField(String field, List<String> available) {
        return new BindException(Kind.NO_SUCH_FIELD, "no field '" + field + "' in this facet; it has: " + String.join(", ", available), field, available);
    }
    public static BindException noSuchForm(String form, List<String> available) {
        String offers = available.isEmpty() ? "(only its implicit form)" : String.join(", ", available);
        return new BindException(Kind.NO_SUCH_FORM, "this facet offers no form '" + form + "'; it offers: " + offers, form, available);
    }
}
