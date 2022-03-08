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

import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.config.standard.Param;
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
}
