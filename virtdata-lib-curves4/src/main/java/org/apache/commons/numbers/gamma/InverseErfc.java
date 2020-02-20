/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.numbers.gamma;

/**
 * Inverse of the <a href="http://mathworld.wolfram.com/Erfc.html">complementary error function</a>.
 * <p>
 * This implementation is described in the paper:
 * <a href="http://people.maths.ox.ac.uk/gilesm/files/gems_erfinv.pdf">Approximating
 * the erfinv function</a> by Mike Giles, Oxford-Man Institute of Quantitative Finance,
 * which was published in GPU Computing Gems, volume 2, 2010.
 * The source code is available <a href="http://gpucomputing.net/?q=node/1828">here</a>.
 * </p>
 */
public class InverseErfc {
    /**
     * Returns the inverse complementary error function.
     *
     * @param x Value.
     * @return t such that {@code x =} {@link Erfc#value(double) Erfc.value(t)}.
     */
    public static double value(double x) {
        return InverseErf.value(1 - x);
    }
}
