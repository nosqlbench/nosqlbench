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

package io.nosqlbench.virtdata.core.templates;

import java.util.Arrays;

/// A bind value that is part statement and part parameters: text with
/// positional markers, the values that fill them, and the identity of
/// the **form** the text has.
///
/// A binding that produces fragments reifies a structure that changes
/// per cycle — a predicate whose shape differs from one query to the
/// next — into statement text an adapter can prepare, while the values
/// still bind as parameters. Two fragments with the same [#formKey]
/// have the same [#text], so an adapter prepares each form once and
/// binds every later fragment of that form through the statement it
/// already holds. The key is whatever identifies the form to its
/// producer; for a predicate it is the node's fingerprint.
public interface PreparedFragment {

    /// The identity of this fragment's form. Equal keys mean equal text.
    String formKey();

    /// The statement text of the form, with a `?` per value.
    String text();

    /// The values for the markers, in marker order.
    Object[] values();

    /// A fragment of its three parts.
    static PreparedFragment of(String formKey, String text, Object[] values) { return new Of(formKey, text, values); }

    /// The plain fragment.
    record Of(String formKey, String text, Object[] values) implements PreparedFragment {
        @Override public String toString() { return "Fragment[" + text + " <- " + Arrays.toString(values) + "]"; }
    }
}
