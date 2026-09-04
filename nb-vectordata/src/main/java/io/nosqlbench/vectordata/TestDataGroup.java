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

import io.nosqlbench.vectordata.internal.HttpTransport;
import io.nosqlbench.vectordata.internal.ManifestView;
import io.nosqlbench.vectordata.internal.Shards;
import io.nosqlbench.vectordata.internal.SourceSpec;
import io.nosqlbench.vectordata.internal.YamlData;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Dataset manifest and its selectable profiles. */
public final class TestDataGroup {
    private static final Set<String> NON_FACETS = Set.of("extends", "base_count", "query_count", "maxk", "partition", "name", "description", "tags");
    private final String name; private final URI manifest; private final Map<String, Map<String, Object>> profiles; private final VectorDataSettings settings;
    private final Map<String, Object> attributes;
    private TestDataGroup(String name, URI manifest, Map<String, Map<String, Object>> profiles, VectorDataSettings settings,
                          Map<String, Object> attributes) {
        this.name = name; this.manifest = manifest; this.profiles = profiles; this.settings = settings; this.attributes = attributes;
    }
    public static TestDataGroup load(String source) { return load(uri(source), VectorDataSettings.defaults()); }
    /** Builds a group from a legacy knn_entries layout whose sources are already resolved. */
    public static TestDataGroup fromLegacyEntries(String name, URI origin, Map<String, Map<String, Object>> entries, VectorDataSettings settings) {
        Map<String, Map<String, Object>> profiles = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : entries.entrySet()) {
            Map<String, Object> profile = new LinkedHashMap<>();
            for (Map.Entry<String, Object> facet : entry.getValue().entrySet()) {
                profile.put(canonical(facet.getKey()), facet.getValue() instanceof Map<?, ?> ? facet.getValue() : Map.of("source", facet.getValue()));
            }
            profiles.put(entry.getKey(), profile);
        }
        if (profiles.isEmpty()) throw new VectorDataException("Legacy catalog has no profiles for " + name);
        return new TestDataGroup(name, origin, profiles, settings, Map.of());
    }
    public static TestDataGroup load(URI source, VectorDataSettings settings) {
        if (!isYaml(source)) {
            // A directory resolves to its dataset.yaml, then to a legacy
            // knn_entries.yaml — but only when the canonical manifest is
            // absent. One that exists and is refused stays refused: a
            // fallback that swallowed the refusal would report "no such
            // file" for a dataset whose fault has a name.
            URI canonical = child(source, "dataset.yaml");
            String text;
            try { text = read(canonical, settings); }
            catch (VectorDataException absent) { return load(child(source, "knn_entries.yaml"), settings); }
            return parse(canonical, text, settings);
        }
        return parse(source, read(source, settings), settings);
    }
    private static TestDataGroup parse(URI manifest, String text, VectorDataSettings settings) {
        Map<String, Object> root = YamlData.parse(text, manifest.toString());
        if (!(root.get("profiles") instanceof Map<?, ?>)) return legacy(root, manifest, settings, directoryName(manifest));
        // A dataset above this build's version is refused before
        // anything else is read from it: the field exists to turn "no
        // such file" into a diagnosis.
        Integer stated = root.get("format_version") == null ? null : YamlData.integer(root.get("format_version"), "format_version");
        FormatVersion.checkSupported(stated);
        String name = YamlData.optionalString(root.get("name")); if (name == null) name = basename(manifest);
        Object rawProfiles = root.get("profiles");
        Map<String, Map<String, Object>> profiles = new LinkedHashMap<>();
        if (rawProfiles instanceof Map<?, ?> map) {
            map.forEach((key, value) -> profiles.put(String.valueOf(key), YamlData.map(value, "profile " + key)));
        } else {
            Map<String, Object> fallback = new LinkedHashMap<>(root); fallback.remove("name"); fallback.remove("profiles"); profiles.put("default", fallback);
        }
        if (profiles.isEmpty()) throw new VectorDataException("Manifest contains no profiles: " + manifest);
        Map<String, Object> attributes = root.get("attributes") instanceof Map<?, ?> declared
            ? Map.copyOf(YamlData.map(declared, "attributes")) : Map.of();
        TestDataGroup group = new TestDataGroup(name, manifest, profiles, settings, attributes);
        // The version the content requires is derived by folding every
        // declaration, never asserted — and every declaration is
        // checked for self-consistency here, before a facet opens.
        FormatVersion.checkStatedAgainstContent(stated, group.requiredVersion());
        return group;
    }
    private static TestDataGroup legacy(Map<String, Object> root, URI manifest, VectorDataSettings settings, String preferred) {
        String configuredBase = root.get("_defaults") instanceof Map<?, ?> map ? YamlData.optionalString(YamlData.map(map, "_defaults").get("base_url")) : null;
        URI base = configuredBase == null ? manifest.resolve(".") : manifest.resolve(configuredBase.endsWith("/") ? configuredBase : configuredBase + "/");
        Map<String, Map<String, Map<String, Object>>> grouped = new LinkedHashMap<>();
        for (Map.Entry<String, Object> item : root.entrySet()) {
            if (item.getKey().startsWith("_")) continue;
            int colon = item.getKey().indexOf(':'); String dataset = colon < 0 ? item.getKey() : item.getKey().substring(0, colon); String profile = colon < 0 ? "default" : item.getKey().substring(colon + 1);
            Map<String, Object> values = YamlData.map(item.getValue(), "legacy entry " + item.getKey()); Map<String, Object> resolved = new LinkedHashMap<>();
            for (Map.Entry<String, Object> facet : values.entrySet()) {
                if (facet.getValue() instanceof String path) {
                    SourceSpec spec = SourceSpec.parse(path);
                    resolved.put(facet.getKey(), spec.windowText() == null ? base.resolve(spec.path()).toString()
                        : Map.of("source", base.resolve(spec.path()).toString(), "window", spec.windowText()));
                } else resolved.put(facet.getKey(), facet.getValue());
            }
            grouped.computeIfAbsent(dataset, ignored -> new LinkedHashMap<>()).put(profile, resolved);
        }
        if (grouped.isEmpty()) throw new VectorDataException("Legacy entries document has no datasets: " + manifest);
        String selected = grouped.containsKey(preferred) ? preferred : grouped.keySet().iterator().next();
        return fromLegacyEntries(selected, manifest, grouped.get(selected), settings);
    }
    public String name() { return name; }
    public Map<String, Map<String, Object>> profiles() { return Map.copyOf(profiles); }
    public TestDataView profile(String profile) {
        String selected = profile == null || profile.isBlank() ? (profiles.containsKey("default") ? "default" : profiles.keySet().iterator().next()) : profile;
        return new ManifestView(name, selected, facets(selected), settings, attributes);
    }
    /// The format version this manifest's content requires: sharded if
    /// any profile declares a multi-file facet, base otherwise.
    private int requiredVersion() {
        for (Map.Entry<String, Map<String, Object>> profile : profiles.entrySet())
            for (FacetDescriptor facet : declared(profile.getKey(), profile.getValue()).values())
                if (facet.isSeries()) return FormatVersion.SHARDED;
        return FormatVersion.BASE;
    }
    private Map<String, FacetDescriptor> facets(String profile) {
        Map<String, Object> definition = profiles.get(profile);
        if (definition == null) throw new VectorDataException("Dataset " + name + " has no profile " + profile);
        Map<String, FacetDescriptor> inherited = new LinkedHashMap<>();
        // Profiles name only what differs: facets a profile does not
        // declare resolve from `default`. A sized profile like `100k`
        // carries a windowed base and its own neighbor facets and
        // inherits the rest — query vectors included — without an
        // explicit `extends`.
        if (!"default".equals(profile) && profiles.containsKey("default")) inherited.putAll(facets("default"));
        String parent = YamlData.optionalString(definition.get("extends"));
        if (parent != null) {
            if (parent.equals(profile)) throw new VectorDataException("Profile cannot extend itself: " + profile);
            inherited.putAll(facets(parent));
        }
        inherited.putAll(declared(profile, definition));
        return inherited;
    }
    /// The facets a profile declares itself, resolved against the
    /// manifest. Declaration shape is resolved here, once: a string
    /// source is one file; a `NNNN` pattern with `shard_stride` and
    /// `shard_count`, or an array of sources, is a series. Every series
    /// declaration is checked for self-consistency at load, so a
    /// dataset that disagrees with itself is refused before a facet
    /// opens.
    private Map<String, FacetDescriptor> declared(String profile, Map<String, Object> definition) {
        Map<String, FacetDescriptor> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : definition.entrySet()) {
            if (NON_FACETS.contains(entry.getKey())) continue;
            String facetName = canonical(entry.getKey());
            if (entry.getValue() instanceof List<?>)
                throw new VectorDataException("facet '" + facetName + "' in profile " + profile + ": a list of sources belongs under 'source:'");
            Map<String, Object> values = entry.getValue() instanceof Map<?, ?> ? YamlData.map(entry.getValue(), "facet " + entry.getKey()) : Map.of("source", entry.getValue());
            Object rawSource = values.get("source");
            if (rawSource == null) rawSource = values.get("path");
            if (rawSource == null) continue;
            String window = windowText(values.get("window"));
            Long stride = longOrNull(values.get("shard_stride"), "shard_stride");
            Integer count = values.get("shard_count") == null ? null : YamlData.integer(values.get("shard_count"), "shard_count");
            Long recordCount = longOrNull(values.get("record_count"), "record_count");
            boolean isArray = rawSource instanceof List<?>;
            List<String> raw = new ArrayList<>();
            if (isArray) for (Object item : (List<?>) rawSource) raw.add(YamlData.string(item, "facet " + facetName + " source entry"));
            else raw.add(String.valueOf(rawSource));
            boolean layout = stride != null || count != null;
            if (!isArray && !layout && !Shards.hasShardField(raw.get(0))) {
                SourceSpec spec = SourceSpec.parse(raw.get(0));
                if (spec.windowText() != null) window = spec.windowText();
                result.put(facetName, new FacetDescriptor(facetName, manifest.resolve(spec.path()), window, Map.copyOf(values)));
                continue;
            }
            List<String> entries = new ArrayList<>();
            for (String item : raw) {
                SourceSpec spec = SourceSpec.parse(item);
                // A window suffix on a uniform pattern is the facet
                // window: a pattern names no file, so there is no entry
                // for it to bound. Given both ways, it is refused rather
                // than guessed at.
                if (layout && spec.windowText() != null) {
                    if (window != null)
                        throw new VectorDataException("facet '" + facetName + "': the facet window is given twice: on the source pattern and as 'window'");
                    window = spec.windowText();
                    spec = new SourceSpec(spec.path(), DSWindow.ALL, null, spec.declaredCount());
                }
                entries.add(spec.withPath(manifest.resolve(spec.path()).toString()).render());
            }
            Shards.validate(facetName, new Shards.Declaration(raw, isArray, stride, count, recordCount));
            result.put(facetName, new FacetDescriptor(facetName, null, window, Map.copyOf(values),
                new FacetDescriptor.Series(entries, isArray, stride, count, recordCount)));
        }
        return result;
    }
    private static Long longOrNull(Object value, String label) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        try { return Long.parseLong(String.valueOf(value).trim()); }
        catch (NumberFormatException e) { throw new VectorDataException("Invalid integer " + label + ": " + value, e); }
    }
    /// Normalizes the `window:` key to the canonical interval text.
    /// Manifests carry it as the string grammar, but the serializer
    /// that publishes profiles emits the structured form — a list of
    /// `{min_incl, max_excl}` maps — and a bare count means `[0..N)`.
    /// Dropping an unrecognized form silently would read the whole
    /// facet where a window was declared, so it fails instead.
    private static String windowText(Object value) {
        if (value == null) return null;
        if (value instanceof String text) return text;
        if (value instanceof Number count) return "0.." + count;
        if (value instanceof List<?> list) {
            List<String> parts = new ArrayList<>();
            for (Object item : list) parts.add(intervalText(item));
            return String.join(", ", parts);
        }
        if (value instanceof Map<?, ?>) return intervalText(value);
        throw new VectorDataException("Unrecognized window form: " + value);
    }
    private static String intervalText(Object item) {
        if (item instanceof String text) return text;
        if (item instanceof Number count) return "0.." + count;
        if (item instanceof Map<?, ?> raw) {
            Map<String, Object> interval = YamlData.map(raw, "window interval");
            Object min = interval.get("min_incl"); Object max = interval.get("max_excl");
            if (min instanceof Number && max instanceof Number) return min + ".." + max;
        }
        throw new VectorDataException("Unrecognized window interval: " + item);
    }
    private static boolean isYaml(URI source) { String path = source.getPath() == null ? "" : source.getPath().toLowerCase(); return path.endsWith(".yaml") || path.endsWith(".yml"); }
    private static URI child(URI source, String name) { String text = source.toString(); return URI.create(text.endsWith("/") ? text + name : text + "/" + name); }
    private static String directoryName(URI source) { String path = source.getPath(); int end = path.lastIndexOf('/'); String parent = path.substring(0, end); int start = parent.lastIndexOf('/'); return parent.substring(start + 1); }
    private static URI uri(String source) { return source.contains("://") || source.startsWith("file:") ? URI.create(source) : Path.of(source).toAbsolutePath().toUri(); }
    private static String read(URI source, VectorDataSettings settings) {
        try {
            if ("file".equalsIgnoreCase(source.getScheme())) return Files.readString(Path.of(source));
            return new String(new HttpTransport(settings).get(source), java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) { throw new VectorDataException("Cannot read manifest " + source, e); }
    }
    private static String basename(URI uri) { String path = uri.getPath(); String parent = path.substring(0, path.lastIndexOf('/')); return parent.substring(parent.lastIndexOf('/') + 1); }
    private static String canonical(String value) { return FacetNames.canonical(value); }
}
