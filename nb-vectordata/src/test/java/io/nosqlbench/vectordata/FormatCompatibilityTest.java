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

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/** Verifies the checked-in catalog and binary xvec format fixture through the public API. */
@Tag("unit")
class FormatCompatibilityTest {
    @TempDir Path temporary;
    @Test void readsCheckedInFormatFixtureThroughCatalogApi() throws Exception {
        URI catalogUri = getClass().getClassLoader().getResource("rust-v1/catalog.yaml").toURI();
        VectorDataSettings settings = VectorDataSettings.builder().cacheDirectory(temporary.resolve("cache")).build();
        TestDataView view = Catalog.of(CatalogSources.of(catalogUri), settings).open("rust-v1-demo", "default");
        assertArrayEquals(new float[] {1f, 2f}, view.baseVectors().get(0));
        assertArrayEquals(new float[] {3f, 4f}, view.baseVectors().get(1));
        assertArrayEquals(new float[] {5f, 6f}, view.queryVectors().get(0));
        assertArrayEquals(new int[] {7, 8}, view.neighborIndices().get(0));
    }
}
