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

import io.nosqlbench.vectordata.internal.YamlData;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// A dataset entry resolved from a catalog source. `attributes` is the
/// entry as listed; a canonical entry's `layout` carries the dataset's
/// `format_version`, `profile_tags`, and declared `profiles`, which is
/// what a consumer reads **before fetching** — to refuse a dataset
/// above its version, or to resolve a selector against the profiles on
/// offer, without downloading the manifest.
public record CatalogEntry(String name, URI manifest, String datasetType, Map<String, Object> attributes) {

    /// The entry's declared layout, or an empty map when it lists none.
    public Map<String, Object> layout() {
        return attributes.get("layout") instanceof Map<?, ?> layout ? YamlData.map(layout, "layout") : Map.of();
    }

    /// The `format_version` the listing states for the dataset, or
    /// [FormatVersion#BASE] when it states none — absent means 1, like
    /// everywhere else.
    public int formatVersion() {
        Object stated = layout().get("format_version");
        return stated == null ? FormatVersion.BASE : YamlData.integer(stated, "format_version");
    }

    /// The listed profile tag schema, naming tags in order; empty when
    /// none is listed.
    public Map<String, Object> profileTags() {
        return layout().get("profile_tags") instanceof Map<?, ?> tags ? Collections.unmodifiableMap(YamlData.map(tags, "profile_tags")) : Map.of();
    }

    /// The profiles the listing declares, by name, as declared.
    public Map<String, Map<String, Object>> profiles() {
        Object listed = layout().get("profiles");
        if (listed == null) listed = attributes.get("profiles");
        Map<String, Map<String, Object>> profiles = new LinkedHashMap<>();
        if (listed instanceof Map<?, ?> map)
            map.forEach((key, value) -> profiles.put(String.valueOf(key), value instanceof Map<?, ?> ? YamlData.map(value, "profile " + key) : Map.of()));
        return profiles;
    }

    /// The listed profile names, size-ordered as [TestDataGroup#profileNames] orders them.
    public List<String> profileNames() {
        Map<String, Map<String, Object>> profiles = profiles();
        List<String> names = new ArrayList<>(profiles.keySet());
        names.sort((a, b) -> {
            int bySize = Long.compare(TestDataGroup.sortKey(a, declaredStructure(profiles, a).baseCount()),
                TestDataGroup.sortKey(b, declaredStructure(profiles, b).baseCount()));
            return bySize != 0 ? bySize : TestDataGroup.naturalCompare(a, b);
        });
        return names;
    }

    /// Everything a selector can read of every listed profile, with
    /// `base_count` and `maxk` read through the `inherits` chain the
    /// way the loader resolves them, so a catalog entry selects the
    /// same profiles a loaded dataset does. Attributes are the
    /// profile's own, in declaration order.
    public List<ProfileFacts> profileFacts() {
        Map<String, Map<String, Object>> profiles = profiles();
        List<ProfileFacts> facts = new ArrayList<>();
        for (String name : profileNames()) {
            Map<String, Object> profile = profiles.get(name);
            Structure structure = declaredStructure(profiles, name);
            Map<String, Object> own = profile.get("attributes") instanceof Map<?, ?> declared ? YamlData.map(declared, "attributes") : Map.of();
            facts.add(new ProfileFacts(name, structure.baseCount(), structure.maxk(), partition(profile), inherits(profile), own));
        }
        return facts;
    }

    /// The profiles a selector names in this entry, size-ordered;
    /// `null` is `default`. A catalog entry is what a consumer reads
    /// before fetching, so a selector resolves here the same way it
    /// does against the loaded dataset.
    public List<String> select(String selector) { return ProfileSelector.resolve(selector, profileFacts()); }

    /// The one profile a selector names in this entry.
    public String selectOne(String selector) { return ProfileSelector.resolveOne(selector, profileFacts()); }

    /// `base_count` and `maxk` of a declared profile as the loader would
    /// see them: its own, else its parent's. `maxk` crosses any parent;
    /// `base_count` crosses only a named parent other than `default`,
    /// because inheriting from `default` is a size step and the child's
    /// count is its own. A partition profile is self-contained.
    private record Structure(Long baseCount, Integer maxk) { }
    private static Structure declaredStructure(Map<String, Map<String, Object>> profiles, String name) {
        Map<String, Object> own = profiles.get(name);
        if (own == null) return new Structure(null, null);
        Long baseCount = longOrNull(own.get("base_count"));
        Integer maxk = own.get("maxk") == null ? null : YamlData.integer(own.get("maxk"), "maxk");
        String current = name;
        // A chain is at most one hop per profile; a cycle stops here.
        for (int hops = 0; hops < profiles.size(); hops++) {
            Map<String, Object> profile = profiles.get(current);
            if (profile == null || partition(profile) || "default".equals(current)) break;
            String stated = inherits(profile);
            String parent = stated != null && !stated.equals(current) && profiles.containsKey(stated) ? stated : "default";
            Map<String, Object> above = profiles.get(parent);
            if (above == null) break;
            if (maxk == null && above.get("maxk") != null) maxk = YamlData.integer(above.get("maxk"), "maxk");
            if (!"default".equals(parent) && baseCount == null) baseCount = longOrNull(above.get("base_count"));
            current = parent;
        }
        return new Structure(baseCount, maxk);
    }

    private static boolean partition(Map<String, Object> profile) {
        Object partition = profile.get("partition");
        return Boolean.TRUE.equals(partition) || "true".equalsIgnoreCase(String.valueOf(partition));
    }

    private static String inherits(Map<String, Object> profile) {
        Object raw = profile.containsKey("inherits") ? profile.get("inherits") : profile.get("extends");
        return raw == null ? null : String.valueOf(raw);
    }

    private static Long longOrNull(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        try { return Long.parseLong(String.valueOf(value).trim()); } catch (NumberFormatException e) { return null; }
    }
}
