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
    private TestDataGroup(String name, URI manifest, Map<String, Map<String, Object>> profiles, VectorDataSettings settings) {
        this.name = name; this.manifest = manifest; this.profiles = profiles; this.settings = settings;
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
        return new TestDataGroup(name, origin, profiles, settings);
    }
    public static TestDataGroup load(URI source, VectorDataSettings settings) {
        if (!isYaml(source)) {
            URI canonical = child(source, "dataset.yaml");
            try { return load(canonical, settings); }
            catch (VectorDataException ignored) { return load(child(source, "knn_entries.yaml"), settings); }
        }
        URI manifest = source;
        Map<String, Object> root = YamlData.parse(read(manifest, settings), manifest.toString());
        if (!(root.get("profiles") instanceof Map<?, ?>)) return legacy(root, manifest, settings, directoryName(manifest));
        String name = YamlData.optionalString(root.get("name")); if (name == null) name = basename(manifest);
        Object rawProfiles = root.get("profiles");
        Map<String, Map<String, Object>> profiles = new LinkedHashMap<>();
        if (rawProfiles instanceof Map<?, ?> map) {
            map.forEach((key, value) -> profiles.put(String.valueOf(key), YamlData.map(value, "profile " + key)));
        } else {
            Map<String, Object> fallback = new LinkedHashMap<>(root); fallback.remove("name"); fallback.remove("profiles"); profiles.put("default", fallback);
        }
        if (profiles.isEmpty()) throw new VectorDataException("Manifest contains no profiles: " + manifest);
        return new TestDataGroup(name, manifest, profiles, settings);
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
                    String[] split = splitWindowSuffix(path);
                    resolved.put(facet.getKey(), split[1] == null ? base.resolve(split[0]).toString()
                        : Map.of("source", base.resolve(split[0]).toString(), "window", split[1]));
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
        return new ManifestView(name, selected, facets(selected, new LinkedHashMap<>()), settings);
    }
    private Map<String, FacetDescriptor> facets(String profile, Map<String, FacetDescriptor> inherited) {
        Map<String, Object> definition = profiles.get(profile);
        if (definition == null) throw new VectorDataException("Dataset " + name + " has no profile " + profile);
        // Profiles name only what differs: facets a profile does not
        // declare resolve from `default`. A sized profile like `100k`
        // carries a windowed base and its own neighbor facets and
        // inherits the rest — query vectors included — without an
        // explicit `extends`.
        if (!"default".equals(profile) && profiles.containsKey("default")) inherited.putAll(facets("default", new LinkedHashMap<>()));
        String parent = YamlData.optionalString(definition.get("extends"));
        if (parent != null) {
            if (parent.equals(profile)) throw new VectorDataException("Profile cannot extend itself: " + profile);
            inherited.putAll(facets(parent, new LinkedHashMap<>()));
        }
        for (Map.Entry<String, Object> entry : definition.entrySet()) {
            if (NON_FACETS.contains(entry.getKey())) continue;
            String facetName = canonical(entry.getKey());
            Map<String, Object> values = entry.getValue() instanceof Map<?, ?> ? YamlData.map(entry.getValue(), "facet " + entry.getKey()) : Map.of("source", entry.getValue());
            String source = YamlData.optionalString(values.get("source"));
            if (source == null) source = YamlData.optionalString(values.get("path"));
            if (source == null) continue;
            String window = windowText(values.get("window"));
            String[] split = splitWindowSuffix(source);
            if (split[1] != null) window = split[1];
            inherited.put(facetName, new FacetDescriptor(facetName, manifest.resolve(split[0]), window, Map.copyOf(values)));
        }
        return inherited;
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
    /// Splits the documented window-suffix sugar off a facet source
    /// string — `base.fvec[0..1M)` names the file plus a record window,
    /// with either bracket kind on either side. The outer delimiters
    /// are structural: they separate the path from the window and do
    /// not affect interval bound semantics. Returns
    /// `{path, windowOrNull}`. A source string only fails when it
    /// *looks* like it carries a window suffix and that suffix is
    /// malformed — a plain path always passes through — so an error
    /// here names the broken window rather than turning it into "no
    /// such file".
    private static String[] splitWindowSuffix(String source) {
        if (!source.endsWith("]") && !source.endsWith(")")) return new String[] {source, null};
        int bracket = source.indexOf('['); int paren = source.indexOf('(');
        int open = bracket < 0 ? paren : paren < 0 ? bracket : Math.min(bracket, paren);
        if (open <= 0) return new String[] {source, null};
        String inner = source.substring(open + 1, source.length() - 1);
        try { DSWindow.parse(inner); }
        catch (VectorDataException malformed) {
            throw new VectorDataException("source '" + source + "' has a malformed window: " + malformed.getMessage());
        }
        return new String[] {source.substring(0, open), inner};
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
