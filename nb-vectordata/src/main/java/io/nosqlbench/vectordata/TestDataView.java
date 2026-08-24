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

import java.util.Map;
import java.util.Optional;

/** A selected dataset profile with lazily opened standard and custom facets. */
public interface TestDataView {
    String dataset();
    String profile();
    Map<String, FacetDescriptor> facets();
    Optional<FacetDescriptor> facet(String name);
    VectorReader<float[]> baseVectors();
    VectorReader<float[]> queryVectors();
    VectorReader<int[]> neighborIndices();
    VectorReader<float[]> neighborDistances();
    VectorReader<int[]> metadataResults();
    VectorReader<?> openFacet(String name);
    VvecReader<?> openVariableFacet(String name);
    void prebuffer(PrebufferProgress progress);
}
