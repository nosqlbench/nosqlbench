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

import io.nosqlbench.vectordata.FacetDescriptor;
import io.nosqlbench.vectordata.PrebufferProgress;
import io.nosqlbench.vectordata.TestDataView;
import io.nosqlbench.vectordata.VectorDataException;
import io.nosqlbench.vectordata.VectorDataSettings;
import io.nosqlbench.vectordata.VectorReader;
import io.nosqlbench.vectordata.VectorReaders;
import io.nosqlbench.vectordata.VvecReader;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Default TestDataView implementation backed by resolved manifest descriptors. */
public final class ManifestView implements TestDataView {
    private final String dataset, profile; private final Map<String, FacetDescriptor> facets; private final VectorDataSettings settings;
    public ManifestView(String dataset, String profile, Map<String, FacetDescriptor> facets, VectorDataSettings settings) {
        this.dataset = dataset; this.profile = profile; this.facets = Map.copyOf(new LinkedHashMap<>(facets)); this.settings = settings;
    }
    @Override public String dataset() { return dataset; }
    @Override public String profile() { return profile; }
    @Override public Map<String, FacetDescriptor> facets() { return facets; }
    @Override public Optional<FacetDescriptor> facet(String name) { return Optional.ofNullable(facets.get(canonical(name))); }
    @Override public VectorReader<float[]> baseVectors() { return fixed("base", VectorReaders::f32); }
    @Override public VectorReader<float[]> queryVectors() { return fixed("query", VectorReaders::f32); }
    @Override public VectorReader<int[]> neighborIndices() { return fixed("neighbor_indices", VectorReaders::i32); }
    @Override public VectorReader<float[]> neighborDistances() { return fixed("neighbor_distances", VectorReaders::f32); }
    @Override public VectorReader<int[]> metadataResults() { return fixed("metadata_results", VectorReaders::i32); }
    @Override public VectorReader<?> openFacet(String name) { FacetDescriptor facet = require(name); return window(VectorReaders.open(facet.source(), settings, dataset), facet); }
    @Override public VvecReader<?> openVariableFacet(String name) { FacetDescriptor facet = require(name); return VectorReaders.openVvec(facet.source(), settings, dataset); }
    @Override public void prebuffer(PrebufferProgress progress) { for (String name : facets.keySet()) openFacet(name).prebuffer(progress); }
    private FacetDescriptor require(String name) { return facet(name).orElseThrow(() -> new VectorDataException("Profile " + profile + " lacks facet " + name)); }
    private <T> VectorReader<T> fixed(String name, FixedFactory<T> factory) { FacetDescriptor facet = require(name); return window(factory.open(facet.source(), settings, dataset), facet); }
    private static <T> VectorReader<T> window(VectorReader<T> reader, FacetDescriptor facet) { return facet.window() == null || facet.window().isBlank() ? reader : new WindowedVectorReader<>(reader, facet.window()); }
    private static String canonical(String name) {
        return switch (name) { case "metadata_indices", "predicate_results" -> "metadata_results"; case "filtered" -> "prefiltered"; default -> name; };
    }
    @FunctionalInterface private interface FixedFactory<T> { VectorReader<T> open(URI source, VectorDataSettings settings, String identity); }
}
