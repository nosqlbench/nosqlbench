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

package io.nosqlbench.converters.cql.exporters;

import io.nosqlbench.cqlgen.core.CGColumnRebinder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CGWorkloadExporterTest {

    @Test
    public void testQuantizer() {
        assertThat(CGColumnRebinder.quantizeModuloByMagnitude(23L,1)).isEqualTo(20L);
        assertThat(CGColumnRebinder.quantizeModuloByMagnitude(234234343L,2)).isEqualTo(230000000L);
        assertThat(CGColumnRebinder.quantizeModuloByMagnitude(275234343L,3)).isEqualTo(275000000L);
        assertThat(CGColumnRebinder.quantizeModuloByMagnitude(275734343L,3)).isEqualTo(276000000L);
    }

}
