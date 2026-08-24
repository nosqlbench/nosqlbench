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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Optional release canary for a dataset published by vectordata-rs.
 * Set {@code vectordata.canary.catalog}, {@code vectordata.canary.dataset},
 * and optionally {@code vectordata.canary.profile} to enable it.
 */
class RustHostedCanaryIntegrationTest {
    @Test void opensConfiguredRustHostedDataset() {
        String catalog = System.getProperty("vectordata.canary.catalog");
        String dataset = System.getProperty("vectordata.canary.dataset");
        assumeTrue(catalog != null && !catalog.isBlank() && dataset != null && !dataset.isBlank(),
            "Configure vectordata.canary.catalog and vectordata.canary.dataset to enable the Rust-hosted canary");
        String profile = System.getProperty("vectordata.canary.profile", "default");
        String cache = System.getProperty("vectordata.canary.cache", System.getProperty("java.io.tmpdir") + "/nb-vectordata-canary-cache");
        VectorDataSettings settings = VectorDataSettings.builder().cacheDirectory(java.nio.file.Path.of(cache)).build();
        TestDataView view = Catalog.of(CatalogSources.of(java.net.URI.create(catalog)), settings).open(dataset, profile);
        assertTrue(view.baseVectors().count() > 0, "canary base vectors must be non-empty");
    }
}
