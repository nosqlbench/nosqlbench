/*
 * Copyright (c) 2022-2023 nosqlbench
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

import io.nosqlbench.api.config.standard.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigModelTest {

    @Test
    public void testMultipleParams() {
        ConfigModel cm = ConfigModel.of(ConfigModelTest.class,
            Param.defaultTo(List.of("a","b"),"value").setRequired(false),
            Param.required("c",int.class));
        NBConfiguration cfg = cm.apply(Map.of("c", 232));
        assertThat(cfg.getOptional("a")).isEmpty();
        assertThat(cfg.get("c",int.class)).isEqualTo(232);
    }

    @Test
    public void testBoxingSupport() {
        NBConfigModel model = ConfigModel.of(ConfigModelTest.class)
            .add(Param.defaultTo("val",5))
            .asReadOnly();
        NBConfiguration config = model.apply(Map.of("val", 7));
        Integer val1 = config.getOrDefault("val", 8);
        assertThat(val1).isEqualTo(7);

        int val2 = config.getOrDefault("val", 9);
        assertThat(val2).isEqualTo(7);

        Integer val3 = config.get("val");
        assertThat(val3).isEqualTo(7);

        int val4 = config.get("val");
        assertThat(val4).isEqualTo(7);

    }
}
