/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.virtdata.library.basics.shared.vectors.dnn;

import io.nosqlbench.virtdata.library.basics.shared.vectors.dnn.angular.DNN_angular1_neighbors;
import io.nosqlbench.virtdata.library.basics.shared.vectors.dnn.angular.DNN_angular1_v;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class DNN_Macro_Tests {

    @Test
    public void loopBackPrecisionTest() {
        DNN_angular1_neighbors dnnan = new DNN_angular1_neighbors(3, 1_000_000, 7);
        int[] indices = dnnan.apply(500);
        System.out.println("neighbor indices:\n"+Arrays.toString(indices));

        DNN_angular1_v dnnav = new DNN_angular1_v(10,1_000_000,7);
        for (int index : indices) {
            float[] v = dnnav.apply(index);
            System.out.println("n["+index+"]: " + Arrays.toString(v));
        }


    }

}
