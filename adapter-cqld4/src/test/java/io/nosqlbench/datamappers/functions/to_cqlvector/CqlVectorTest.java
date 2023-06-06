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

import io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection.ListSized;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_float.HashRange;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CqlVectorTest {

    @Test
    public void testCqlVector() {
        CqlVector func = new CqlVector(new ListSized(2, new HashRange(0.2f, 5.0f)));
        com.datastax.oss.driver.api.core.data.CqlVector result = func.apply(23L);
        List<Object> values = new ArrayList<>();
        for (Object value : result.getValues()) {
            values.add(value);
        }
        assertThat(values.get(0)).isInstanceOf(Float.class);
    }

}
