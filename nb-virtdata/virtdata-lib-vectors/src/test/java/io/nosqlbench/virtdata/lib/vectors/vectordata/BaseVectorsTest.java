package io.nosqlbench.virtdata.lib.vectors.vectordata;

/*
 * Copyright (c) nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.vectordata.VectorDataSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
public class BaseVectorsTest {

    @TempDir Path temporary;

    @Test
    public void testFixtureDataset() throws Exception {
        Path dataset = Files.createDirectories(temporary.resolve("example"));
        ByteBuffer values = ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN);
        values.putInt(2).putFloat(1f).putFloat(2f).putInt(2).putFloat(3f).putFloat(4f);
        Files.write(dataset.resolve("base.fvec"), values.array());
        Files.writeString(dataset.resolve("dataset.yaml"), """
            name: example
            profiles:
              demo:
                base: base.fvec
            """);
        Path catalog = temporary.resolve("catalog.yaml");
        Files.writeString(catalog, """
            datasets:
              - name: example
                path: example/dataset.yaml
            """);
        String prior = System.getProperty("vectordata.catalog");
        System.setProperty("vectordata.catalog", catalog.toString());
        try {
            BaseVectors vectors = new BaseVectors("example:demo", true,
                VectorDataSettings.builder().cacheDirectory(temporary.resolve("cache")).build());
            assertArrayEquals(new float[] {3f, 4f}, vectors.apply(1));
        } finally {
            if (prior == null) System.clearProperty("vectordata.catalog"); else System.setProperty("vectordata.catalog", prior);
        }
    }
}
