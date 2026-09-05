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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// A prepared statement whose text varies by form: a fragment at a
/// bind point contributes its text and its values in place, is keyed
/// by its own form key, and a cycle without one is untouched.
@Tag("unit")
class DynamicFormsTest {

    private static final ParsedTemplateString TEMPLATE = new ParsedTemplateString(
        "SELECT key FROM ks.t WHERE {predicate} ORDER BY embedding ANN OF {vec} LIMIT 10",
        Map.of("predicate", "PredicateClause(...)", "vec", "QueryVectors(...)"));

    @Test
    void aFragmentContributesItsTextAndValuesInPlace() {
        PreparedFragment predicate = PreparedFragment.of("(year >= 0 AND topic = '')", "year >= ? AND topic = ?", new Object[] {2020, "tax"});
        Object[] fields = {predicate, "[0.1, 0.2]"};
        assertTrue(DynamicForms.hasFragment(fields));
        assertEquals("SELECT key FROM ks.t WHERE year >= ? AND topic = ? ORDER BY embedding ANN OF ? LIMIT 10", DynamicForms.text(TEMPLATE, fields));
        assertArrayEquals(new Object[] {2020, "tax", "[0.1, 0.2]"}, DynamicForms.spread(fields));
        assertSame(predicate.formKey(), DynamicForms.formKey(fields), "one fragment: its own key, no allocation");
    }

    @Test
    void aCycleWithoutAFragmentIsUntouched() {
        Object[] fields = {"a", 1};
        assertFalse(DynamicForms.hasFragment(fields));
        assertEquals("SELECT key FROM ks.t WHERE ? ORDER BY embedding ANN OF ? LIMIT 10", DynamicForms.text(TEMPLATE, fields));
        assertArrayEquals(fields, DynamicForms.spread(fields));
        assertNull(DynamicForms.formKey(fields));
    }

    @Test
    void severalFragmentsKeyTheirJoinedForms() {
        Object[] fields = {PreparedFragment.of("A", "a = ?", new Object[] {1}), PreparedFragment.of("B", "b IN ?", new Object[] {java.util.List.of(2, 3)})};
        assertEquals("A|B", DynamicForms.formKey(fields));
        assertEquals("SELECT key FROM ks.t WHERE a = ? ORDER BY embedding ANN OF b IN ? LIMIT 10", DynamicForms.text(TEMPLATE, fields));
        assertEquals(2, DynamicForms.spread(fields).length);
    }
}
