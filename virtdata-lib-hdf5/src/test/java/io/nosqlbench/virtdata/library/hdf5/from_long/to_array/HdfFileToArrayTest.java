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
 *
 */

package io.nosqlbench.virtdata.library.hdf5.from_long.to_array;

import org.junit.jupiter.api.Test;

public class HdfFileToArrayTest {

    @Test
    public void testHdfFileToVector() {
        final float[][] results = new float[][]{
            {0.0f,1.0f,2.0f,3.0f,4.0f,5.0f,6.0f},
            {2.0f,1.6666666f,2.4f,3.2857144f,4.2222223f,5.181818f,6.1538463f},
            {4.0f,2.3333333f,2.8f,3.5714285f,4.4444447f,5.3636365f,6.3076925f},
            {6.0f,3.0f,3.2f,3.857143f,4.6666665f,5.5454545f,6.4615383f}
        };

        HdfFileToVectorArray hdfFileToVector = new HdfFileToVectorArray(
            "src/test/resources/h5ex_t_float.h5",
            "/DS1");

        float[] read;
        for (int i = 0; i < 4; i++) {
            read = hdfFileToVector.apply(i);
            for (int j = 0; j < 7; j++) {
                assert (read[j] == results[i][j]);
            }
        }
    }
}
