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

package io.nosqlbench.virtdata.lib.vectors.dnn.angular;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class DNN_angular_distance_decimalTest {

    @Test
    public void testBigDecimalDotProduct() {
        DNN_angular_v_decimal v_dec = new DNN_angular_v_decimal(10, 100_000, 100);
        DnnAngular1V v_fp = new DnnAngular1V(10, 100_000, 100);

        var v1d = v_dec.apply(90000);
        var v1f = v_fp.apply(90000);

        System.out.println("v1d:"+ Arrays.toString(v1d));
        System.out.println("v1f:"+ Arrays.toString(v1f));

        var v2d = v_dec.apply(90001);
        var v2f = v_fp.apply(90001);

        System.out.println("v2d:"+ Arrays.toString(v2d));
        System.out.println("v2f:"+ Arrays.toString(v2f));

        var v3d = v_dec.apply(90002);
        var v3f = v_fp.apply(90003);
        System.out.println("v3d:"+ Arrays.toString(v3d));
        System.out.println("v3f:"+ Arrays.toString(v3f));

        DNN_angular_distance_decimal ddiffer = new DNN_angular_distance_decimal();
        DNN_angular_distance_float fdiffer = new DNN_angular_distance_float();

        var diff_1_2 = ddiffer.apply(v1d,v2d);
        System.out.println("diff(1->2)" + diff_1_2);
        var fdiff_1_2 = fdiffer.apply(v1f,v2f);

        var diff_1_3 = ddiffer.apply(v1d,v3d);
        System.out.println("diff(1->3)" + diff_1_3);

        var diff_2_3 = ddiffer.apply(v2d,v3d);
        System.out.println("diff(2->3)" + diff_2_3);


    }

    @Test
    public void testBigDecimalNorm() {

    }

}
