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

package io.nosqlbench.virtdata.library.ivecfvec;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class IVecReaderTest {

    @Test
    public void testReadIvec() {
        IVecReader ir = new IVecReader("src/test/resources/ivecfvec/test_ada_002_10000_indices_query_10000.ivec");
        for (int i = 0; i < 10; i++) {
            int[] indices = ir.apply(0);
            for (int j = 0; j < indices.length; j++) {
                assertThat(indices[j]).isGreaterThanOrEqualTo(0);
                assertThat(indices[j]).isLessThanOrEqualTo(10000);
            }
        }
    }

    @Test
    public void testReadFvec() {
        FVecReader ir = new FVecReader("src/test/resources/ivecfvec/test_ada_002_10000_distances_count.fvec");
        for (int i = 0; i < 10; i++) {
            float[] dist = ir.apply(i);
            for (int j = 1; j < dist.length; j++) {
                assertThat(dist[j]).isGreaterThanOrEqualTo(dist[j-1]);
            }
        }
    }

    @Test
    public void testReadFvecSpecificDims() {
        FVecReader ir = new FVecReader(
            "src/test/resources/ivecfvec/test_ada_002_10000_base_vectors.fvec",
            1536,0);
        float[] vec0 = ir.apply(0);
        assertThat(vec0.length).isEqualTo(1536);
    }

}
