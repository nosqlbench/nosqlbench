/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.virtdata.library.basics.shared.from_double.to_double;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.util.function.LongToDoubleFunction;

import static org.assertj.core.api.Assertions.assertThat;

public class SumFunctionsTest {

    @Test
    public void sumFunctionsTest() {
        LongToDoubleFunction f1 = d -> d*3.0d;
        LongToDoubleFunction f2 = d -> d+5.0d;
        SumFunctions ff = new SumFunctions(f1,f2);
        assertThat(ff.applyAsDouble(15L)).isEqualTo(65.0d, Offset.offset(0.0002d));
    }

}
