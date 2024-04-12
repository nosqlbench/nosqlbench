/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.datamappers.functions.to_cqlvector;

import com.datastax.oss.driver.api.core.data.CqlVector;
import io.nosqlbench.datamappers.functions.to_cqlvector.from_number_list.ToCqlVector;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ToCqlVectorTest {


    @Test
    public void testDoubleListToCqlVector() {
        ToCqlVector toCqlVector = new ToCqlVector();
        assertThat(toCqlVector.apply(List.of(123.d,456.d))).isInstanceOf(CqlVector.class);
    }

    @Test
    public void testFloatListToCqlVector() {
        ToCqlVector toCqlVector = new ToCqlVector();
        assertThat(toCqlVector.apply(List.of(123.f,456.f))).isInstanceOf(CqlVector.class);
    }

}
