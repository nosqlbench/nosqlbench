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

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

/** Ordered catalog locations. Earlier sources win for a duplicate dataset name. */
public final class CatalogSources {
    private final List<URI> locations;
    private CatalogSources(List<URI> locations) { this.locations = List.copyOf(locations); }
    public List<URI> locations() { return locations; }
    public static CatalogSources of(URI... locations) { return new CatalogSources(List.of(locations)); }
    public static CatalogSources defaults() {
        String property = System.getProperty("vectordata.catalog");
        if (property != null && !property.isBlank()) return of(toUri(property));
        String environment = System.getenv("VECTORDATA_CATALOG");
        if (environment != null && !environment.isBlank()) return of(toUri(environment));
        Path home = Optional.ofNullable(System.getenv("VECTORDATA_HOME")).filter(s -> !s.isBlank()).map(Path::of)
            .orElse(Path.of(System.getProperty("user.home"), ".config", "vectordata"));
        Path configured = home.resolve("catalogs.yaml");
        if (Files.isRegularFile(configured)) {
            try {
                Object parsed = new Load(LoadSettings.builder().setLabel(configured.toString()).build()).loadFromString(Files.readString(configured));
                if (parsed instanceof Map<?, ?> sources) {
                    List<URI> locations = sources.values().stream().filter(String.class::isInstance).map(String.class::cast).map(CatalogSources::toUri).toList();
                    if (!locations.isEmpty()) return new CatalogSources(locations);
                }
            } catch (Exception e) { throw new VectorDataException("Cannot read vectordata catalogs " + configured, e); }
        }
        return of(home.resolve("catalog.yaml").toUri());
    }
    private static URI toUri(String value) { return value.contains("://") || value.startsWith("file:") ? URI.create(value) : Path.of(value).toAbsolutePath().toUri(); }
}
