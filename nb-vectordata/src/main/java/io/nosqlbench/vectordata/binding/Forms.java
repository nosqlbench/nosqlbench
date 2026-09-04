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

import io.nosqlbench.vectordata.records.RecordException;
import io.nosqlbench.vectordata.records.RecordFacet;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/// The forms a facet offers, read from its `forms` namespace.
///
/// Absent from every dataset written before forms existed, and absent
/// means one implicit form rather than none: every such dataset must
/// keep binding unchanged. Absence is not an empty set.
public final class Forms {
    private static final Logger LOG = Logger.getLogger(Forms.class.getName());

    /// The slab namespace enumerating the op-template forms a facet offers.
    public static final String NAMESPACE = "forms";

    private Forms() { }

    /// The forms a facet offers: those its `forms` namespace declares,
    /// or the implicit one when it declares none.
    public static List<Form> of(RecordFacet facet) {
        try {
            RecordFacet ns = facet.namespace(NAMESPACE);
            long count = ns.count();
            if (count == 0) return List.of(Form.implicit());
            List<Form> out = new ArrayList<>();
            for (long o = 0; o < count; o++) {
                byte[] bytes = ns.recordBytes(o);
                // A form record this build cannot parse is skipped rather
                // than failing the facet: the forms it *can* read are
                // still usable, and refusing all of them over one would
                // make a facet unreadable for describing a capability it
                // also has.
                try { out.add(Form.fromJson(new String(bytes, StandardCharsets.UTF_8))); }
                catch (RuntimeException e) { LOG.warning("facet '" + facet.name() + "': form record " + o + " not understood, skipping: " + e.getMessage()); }
            }
            if (out.isEmpty()) out.add(Form.implicit());
            return out;
        } catch (RecordException e) {
            throw BindException.facet(e.getMessage());
        }
    }

    /// Selects a form by name.
    ///
    /// A name this facet does not offer is refused naming what it does —
    /// the same rule as a wrong-door reader error, for the same reason.
    /// The implicit form answers to its own name but is not listed as
    /// a choice, so a caller can tell "undeclared" from "declared and
    /// unmatched".
    public static Form byName(RecordFacet facet, String name) {
        List<Form> forms = of(facet);
        for (Form form : forms) if (form.name().equals(name)) return form;
        List<String> available = new ArrayList<>();
        for (Form form : forms) if (!form.name().equals(Form.IMPLICIT)) available.add(form.name());
        throw BindException.noSuchForm(name, available);
    }
}
