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
import io.nosqlbench.vectordata.internal.YamlData;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Catalog resolver for canonical `catalog.yaml` and legacy `knn_entries.yaml` forms. */
public final class Catalog {
    private final Map<String, CatalogEntry> entries; private final VectorDataSettings settings;
    private Catalog(Map<String, CatalogEntry> entries, VectorDataSettings settings) { this.entries = Map.copyOf(entries); this.settings = settings; }
    public static Catalog of(CatalogSources sources) { return of(sources, VectorDataSettings.defaults()); }
    public static Catalog of(CatalogSources sources, VectorDataSettings settings) {
        Map<String, CatalogEntry> entries = new LinkedHashMap<>();
        for (URI source : sources.locations()) load(source, settings, entries);
        return new Catalog(entries, settings);
    }
    public Map<String, CatalogEntry> entries() { return entries; }
    public TestDataView open(String dataset, String profile) {
        CatalogEntry entry = entries.get(dataset);
        if (entry == null) throw new VectorDataException("Dataset is not in configured catalogs: " + dataset);
        if ("knn_entries.yaml".equals(entry.datasetType())) {
            @SuppressWarnings("unchecked") Map<String, Map<String, Object>> profiles = (Map<String, Map<String, Object>>) entry.attributes().get("profiles");
            return TestDataGroup.fromLegacyEntries(dataset, entry.manifest(), profiles, settings).profile(profile);
        }
        return TestDataGroup.load(entry.manifest(), settings).profile(profile);
    }
    public TestDataView openProfile(String datasetAndProfile) {
        int separator = datasetAndProfile.lastIndexOf(':');
        return separator < 0 ? open(datasetAndProfile, null) : open(datasetAndProfile.substring(0, separator), datasetAndProfile.substring(separator + 1));
    }
    private static void load(URI source, VectorDataSettings settings, Map<String, CatalogEntry> output) {
        if (!isCatalogFile(source)) { loadDirectory(source, settings, output); return; }
        parse(source, read(source, settings), output);
    }
    private static void loadDirectory(URI source, VectorDataSettings settings, Map<String, CatalogEntry> output) {
        for (String candidate : List.of("catalog.json", "catalog.yaml", "knn_entries.yaml")) {
            URI file = child(source, candidate);
            try { parse(file, read(file, settings), output); return; }
            catch (VectorDataException ignored) { }
        }
        if ("file".equalsIgnoreCase(source.getScheme())) return;
        throw new VectorDataException("No catalog.json, catalog.yaml, or knn_entries.yaml at " + source);
    }
    private static void parse(URI source, String text, Map<String, CatalogEntry> output) {
        Object document = YamlData.parseValue(text, source.toString());
        if (document instanceof List<?> list) { canonical(source, list, output); return; }
        Map<String, Object> root = YamlData.map(document, source.toString());
        Object candidate = root.get("datasets"); if (candidate == null) candidate = root.get("entries");
        if (candidate instanceof List<?> list) canonical(source, list, output); else legacy(source, root, output);
    }
    private static void canonical(URI source, List<?> list, Map<String, CatalogEntry> output) {
        for (Object item : list) {
            Map<String, Object> entry = YamlData.map(item, "catalog entry");
            String name = YamlData.string(entry.get("name"), "catalog entry name");
            String path = YamlData.string(entry.get("path"), "catalog entry path");
            output.putIfAbsent(name, new CatalogEntry(name, source.resolve(path), YamlData.optionalString(entry.get("dataset_type")), Map.copyOf(entry)));
        }
    }
    private static void legacy(URI source, Map<String, Object> root, Map<String, CatalogEntry> output) {
        String base = root.get("_defaults") instanceof Map<?, ?> map ? YamlData.optionalString(YamlData.map(map, "_defaults").get("base_url")) : null;
        URI baseUri = base == null ? source.resolve(".") : source.resolve(base.endsWith("/") ? base : base + "/");
        Map<String, Map<String, Map<String, Object>>> datasets = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : root.entrySet()) {
            if (entry.getKey().startsWith("_")) continue;
            String dataset = entry.getKey(); int colon = dataset.indexOf(':'); String profile = colon >= 0 ? dataset.substring(colon + 1) : "default"; if (colon >= 0) dataset = dataset.substring(0, colon);
            Object value = entry.getValue();
            Map<String, Object> map = value instanceof Map<?, ?> ? YamlData.map(value, "legacy catalog entry") : Map.of();
            Map<String, Object> resolved = new LinkedHashMap<>();
            for (Map.Entry<String, Object> facet : map.entrySet()) {
                if (facet.getValue() instanceof String path) resolved.put(facet.getKey(), baseUri.resolve(path).toString()); else resolved.put(facet.getKey(), facet.getValue());
            }
            datasets.computeIfAbsent(dataset, ignored -> new LinkedHashMap<>()).put(profile, resolved);
        }
        for (Map.Entry<String, Map<String, Map<String, Object>>> dataset : datasets.entrySet()) {
            output.putIfAbsent(dataset.getKey(), new CatalogEntry(dataset.getKey(), source, "knn_entries.yaml", Map.of("profiles", Map.copyOf(dataset.getValue()))));
        }
    }
    private static String read(URI source, VectorDataSettings settings) {
        try {
            if ("file".equalsIgnoreCase(source.getScheme())) return Files.readString(Path.of(source));
            return new String(new HttpTransport(settings).get(source), StandardCharsets.UTF_8);
        } catch (IOException e) { throw new VectorDataException("Cannot read catalog " + source, e); }
    }
    private static boolean isCatalogFile(URI source) { String path = source.getPath() == null ? "" : source.getPath().toLowerCase(); return path.endsWith(".json") || path.endsWith(".yaml") || path.endsWith(".yml"); }
    private static URI child(URI source, String name) { String text = source.toString(); return URI.create(text.endsWith("/") ? text + name : text + "/" + name); }
}
