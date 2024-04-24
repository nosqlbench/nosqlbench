/*
 * Copyright (c) 2023-2024 nosqlbench
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

package io.nosqlbench.virtdata.lib.vectors.dnn.euclidean;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DNNEuclideanVTest {

    @Test
    public void testBasicVectors() {
        DNN_euclidean_v vf = new DNN_euclidean_v(5, 7);
        assertThat(vf.apply(3L)).isEqualTo(new float[]{3f,4f,5f,6f,7f});
        assertThrows(RuntimeException.class, () -> vf.apply(7));
    }

    @Test
    public void testBasicVectorsScaled() {
        DNN_euclidean_v vf = new DNN_euclidean_v(5, 7, 3.0);
        assertThat(vf.apply(3L)).isEqualTo(new float[]{3f,6f,9f,12f,15f});
        assertThrows(RuntimeException.class, () -> vf.apply(7));
    }


    @Test
    public void testWrappingVectors() {
        DNN_euclidean_v_wrap vf = new DNN_euclidean_v_wrap(5, 7);
        assertThat(vf.apply(3L)).isEqualTo(new float[]{3f,4f,5f,6f,7f});
        assertThat(vf.apply(0L)).isEqualTo(new float[]{0f,1f,2f,3f,4f});
        assertThat(vf.apply(7L)).isEqualTo(new float[]{0f,1f,2f,3f,4f});
    }

    @Test
    public void testContiguousVectors() {
        DNN_euclidean_v_series vf = new DNN_euclidean_v_series(4,10,2);
        assertThat(vf.apply(7L)).isEqualTo(
            new float[][] {
                {7f,8f,9f,10f},
                {8f,9f,10f,11f}
            }
        );

        assertThrows(RuntimeException.class, () -> vf.apply(10));

    }

}
