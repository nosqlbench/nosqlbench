package io.nosqlbench.virtdata.library.curves4.discrete.long_long;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ZipfTest {

    @Test
    @Disabled
    public void testZipfMatrix() {

        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 10; j++) {
                int elements = (int) Math.pow(10,j);
                double exponent = (double) i+1;
                System.out.println("i:" + i + " j:" + j + " elements:" + elements + " exp:" + exponent);
                long startAt = System.nanoTime();
                Zipf z = new Zipf(elements, exponent, "compute", "map");
                for (int k = 0; k < 100; k++) {
                    double frac = ((double)k/100.0d);
                    long unitvalue = (long) (frac * Long.MAX_VALUE);
                    long l = z.applyAsLong(unitvalue);
                    System.out.println("i:" + i + " j:" + j + " K:" + k + " frac:" + frac + " unit:" + unitvalue);
                }
                long endAt = System.nanoTime();
                System.out.println(((double)(endAt-startAt))/1000000000d);

            }
        }
    }

}
