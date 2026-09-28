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

import io.nosqlbench.vectordata.FormatVersion;
import io.nosqlbench.vectordata.VectorDataException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/// Stated parents. From `format_version` 3 every profile other than
/// `default` states `inherits:`, and a stated 3 is a claim that every
/// parent is real and every profile has one meaning: an absent parent,
/// an unknown or self parent, a cycle, and `partition: true` beside
/// `inherits:` are load refusals naming the profiles involved. Versions
/// 1 and 2 keep their fallbacks, and the same conditions are available
/// on them as advisories phrased as what version 3 will refuse. One pure
/// rule, so no two loaders can disagree.
public final class ProfileParents {
    private ProfileParents() { }

    /// What the rule reads of a profile: its name, whether it is a
    /// partition, and the parent it states, `null` for none.
    public record Facts(String name, boolean partition, String inherits) { }

    /// Refuses a version-3 dataset whose parents are not all stated and
    /// real. Below 3 nothing is refused.
    public static void check(int version, List<Facts> profiles) {
        if (version < FormatVersion.TAGGED) return;
        List<String> faults = faults(profiles, true);
        if (!faults.isEmpty()) throw new VectorDataException(String.join("; ", faults));
    }

    /// The same conditions on a dataset below version 3, phrased as
    /// what version 3 will refuse; empty at 3, where the loader refuses.
    public static List<String> advisories(int version, List<Facts> profiles) {
        if (version >= FormatVersion.TAGGED) return List.of();
        List<String> notes = new ArrayList<>();
        for (String fault : faults(profiles, false)) notes.add(fault + " (refused from format_version 3)");
        return notes;
    }

    private static List<String> faults(List<Facts> profiles, boolean strict) {
        List<Facts> sorted = new ArrayList<>(profiles);
        sorted.sort(Comparator.comparing(Facts::name));
        Set<String> names = new HashSet<>();
        for (Facts profile : profiles) names.add(profile.name());
        List<String> faults = new ArrayList<>();
        for (Facts profile : sorted) {
            if ("default".equals(profile.name())) {
                if (profile.inherits() != null) faults.add("profile `default` is the base layer and names no parent");
                continue;
            }
            String parent = profile.inherits();
            if (profile.partition()) {
                if (parent != null) faults.add("profile `" + profile.name() + "` states both `partition: true` and `inherits:`; a partition builds on nothing");
            } else if (parent == null) {
                faults.add("profile `" + profile.name() + "` names no parent; state `inherits: default` or the layer it builds on");
            } else if (parent.equals(profile.name())) {
                faults.add("profile `" + profile.name() + "` names itself as its parent");
            } else if (!names.contains(parent)) {
                faults.add("profile `" + profile.name() + "` names an unknown parent `" + parent + "`");
            }
        }
        // A cycle is reported once, by its sorted members.
        Set<List<String>> cycles = new TreeSet<>(Comparator.comparing(List::toString));
        for (Facts profile : sorted) {
            if (profile.partition() || "default".equals(profile.name())) continue;
            List<String> seen = new ArrayList<>(List.of(profile.name()));
            String current = profile.name();
            while (true) {
                Facts at = find(profiles, current);
                if (at == null || at.partition() || "default".equals(at.name())) break;
                String parent = at.inherits();
                if (parent == null) { if (strict) break; parent = "default"; }
                if (parent.equals(profile.name())) {
                    List<String> members = new ArrayList<>(seen);
                    members.sort(Comparator.naturalOrder());
                    cycles.add(members);
                    break;
                }
                if (seen.contains(parent) || !names.contains(parent)) break;
                seen.add(parent);
                current = parent;
            }
        }
        for (List<String> members : cycles) {
            List<String> quoted = new ArrayList<>();
            for (String member : members) quoted.add("`" + member + "`");
            faults.add("profiles " + String.join(", ", quoted) + " close an inheritance cycle");
        }
        return faults;
    }

    private static Facts find(List<Facts> profiles, String name) {
        for (Facts profile : profiles) if (profile.name().equals(name)) return profile;
        return null;
    }
}
