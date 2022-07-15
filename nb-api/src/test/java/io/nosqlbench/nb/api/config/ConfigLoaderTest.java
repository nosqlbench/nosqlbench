/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.nb.api.config;

import io.nosqlbench.api.config.standard.ConfigLoader;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class ConfigLoaderTest {

    @Test
    public void testSingleParams() {
        ConfigLoader cl = new ConfigLoader();
        List<Map> cfg1 = cl.load("a=b c=234", Map.class);
        assertThat(cfg1).contains(Map.of("a", "b", "c", "234"));
    }

    @Test
    public void testSingleJsonObject() {
        ConfigLoader cl = new ConfigLoader();
        List<Map> cfg1 = cl.load("{a:'b', c:'234'}", Map.class);
        assertThat(cfg1).contains(Map.of("a", "b", "c", "234"));
    }

    @Test
    public void testJsonArray() {
        ConfigLoader cl = new ConfigLoader();
        List<Map> cfg1 = cl.load("[{a:'b', c:'234'}]", Map.class);
        assertThat(cfg1).contains(Map.of("a", "b", "c", "234"));

    }

    @Test
    public void testImportSingle() {
        ConfigLoader cl = new ConfigLoader();
        List<Map> imported = cl.load("IMPORT{importable-config.json}", Map.class);
        assertThat(imported).contains(Map.of("a", "B", "b", "C", "c", 123.0, "d", 45.6));
    }

    @Test
    public void testEmpty() {
        ConfigLoader cl = new ConfigLoader();
        List<Map> cfg1 = cl.load("", Map.class);
        assertThat(cfg1).isNull();

    }
}
