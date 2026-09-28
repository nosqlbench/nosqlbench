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

import io.nosqlbench.vectordata.VectorDataException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class ProfileParentsTest {
    private static ProfileParents.Facts f(String name, boolean partition, String inherits) { return new ProfileParents.Facts(name, partition, inherits); }

    /// Version 3 refuses an unstated, unknown, self or cyclic parent and
    /// a partition that names one; a partition alone is accepted, and
    /// the same shapes pass below 3.
    @Test void versionThreeStatesEveryParent() {
        List<ProfileParents.Facts> ok = List.of(f("default", false, null), f("10m", false, "default"), f("part", true, null), f("set", false, "10m"));
        assertDoesNotThrow(() -> ProfileParents.check(3, ok));

        List<ProfileParents.Facts> unstated = List.of(f("default", false, null), f("10m-mixed", false, null));
        String e = assertThrows(VectorDataException.class, () -> ProfileParents.check(3, unstated)).getMessage();
        assertTrue(e.contains("`10m-mixed` names no parent") && e.contains("inherits: default"), e);
        assertDoesNotThrow(() -> ProfileParents.check(2, unstated), "below 3 the fallback stands");

        String both = assertThrows(VectorDataException.class, () -> ProfileParents.check(3, List.of(f("default", false, null), f("p", true, "default")))).getMessage();
        assertTrue(both.contains("both `partition: true` and `inherits:`"), both);
        String unknown = assertThrows(VectorDataException.class, () -> ProfileParents.check(3, List.of(f("default", false, null), f("a", false, "nope")))).getMessage();
        assertTrue(unknown.contains("unknown parent `nope`"), unknown);
        String selfish = assertThrows(VectorDataException.class, () -> ProfileParents.check(3, List.of(f("default", false, null), f("a", false, "a")))).getMessage();
        assertTrue(selfish.contains("names itself"), selfish);
        String cyclic = assertThrows(VectorDataException.class, () -> ProfileParents.check(3, List.of(f("default", false, null), f("a", false, "b"), f("b", false, "a")))).getMessage();
        assertTrue(cyclic.contains("`a`, `b` close an inheritance cycle"), cyclic);
        assertEquals(1, cyclic.split("cycle", -1).length - 1, "one cycle, reported once: " + cyclic);
        String base = assertThrows(VectorDataException.class, () -> ProfileParents.check(3, List.of(f("default", false, "x")))).getMessage();
        assertTrue(base.contains("`default` is the base layer"), base);
    }

    /// Below 3 the conditions are advisories naming the version that
    /// will refuse them; at 3 there are none, the loader refuses.
    @Test void advisoriesPrecedeTheRefusal() {
        List<ProfileParents.Facts> rows = List.of(f("default", false, null), f("10m", false, null), f("a", false, "nope"));
        List<String> notes = ProfileParents.advisories(2, rows);
        assertEquals(2, notes.size(), notes.toString());
        assertTrue(notes.stream().allMatch(n -> n.endsWith("(refused from format_version 3)")), notes.toString());
        assertTrue(ProfileParents.advisories(3, rows).isEmpty());
    }
}
