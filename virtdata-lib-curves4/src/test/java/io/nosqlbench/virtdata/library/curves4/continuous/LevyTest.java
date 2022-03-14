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

package io.nosqlbench.virtdata.library.curves4.continuous;

import io.nosqlbench.virtdata.library.curves4.continuous.long_double.Levy;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LevyTest {

    @Test
    public void testLevy() {
        Levy levy = new Levy(2.3d, 1.0d);
        assertThat(levy.applyAsDouble(10L)).isCloseTo(2.9379325000660304, Offset.offset(0.000001d));
    }

}
