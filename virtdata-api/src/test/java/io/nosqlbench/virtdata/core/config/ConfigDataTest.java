package io.nosqlbench.virtdata.core.config;

/*
 * Copyright (c) 2022 nosqlbench
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


import io.nosqlbench.nb.api.config.standard.ConfigData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigDataTest {

    @Test
    public void testLayer() {
        ConfigData conf = new ConfigData();
        conf.put("test1", List.of("t","e","s","t","1"));
        Optional<List> test1 = conf.get("test1", List.class);
        assertThat(test1).isPresent();
        assertThat(test1.get()).containsExactly("t","e","s","t","1");
        ConfigData layer2 = conf.layer(Map.of("test1",List.of("another")));
        Optional<List> test2 = layer2.get("test1", List.class);
        assertThat(test2).isPresent();
        assertThat(test2.get()).containsExactly("another");
    }

    @Test
    public void testList() {
        ConfigData conf = new ConfigData();
        conf.put("test1", List.of("t","e","s","t","1"));
        Optional<List<String>> test1 = conf.getList("test1", String.class);
        assertThat(test1).isPresent();
        assertThat(test1.get()).containsExactly("t","e","s","t","1");
    }

}
