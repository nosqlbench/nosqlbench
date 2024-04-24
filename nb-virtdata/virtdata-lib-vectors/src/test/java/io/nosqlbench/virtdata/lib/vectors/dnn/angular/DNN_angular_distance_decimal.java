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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.LongFunction;

public class DNN_angular_distance_decimal implements BiFunction<BigDecimal[],BigDecimal[],BigDecimal> {
    private final static MathContext mc = new MathContext(256, RoundingMode.HALF_EVEN);
    @Override
    public BigDecimal apply(BigDecimal[] v1, BigDecimal[] v2) {
        BigDecimal dot = dot(v1, v2);
        BigDecimal normv1 = norm(v1);
        BigDecimal normv2 = norm(v2);
        BigDecimal norm = normv1.multiply(normv2);
        BigDecimal cos_theta = dot.divide(norm,mc);
        return cos_theta;
    }

    public static BigDecimal dot(BigDecimal[] vectorA, BigDecimal[] vectorB) {
        BigDecimal dotProduct = BigDecimal.ZERO;
        for (int i = 0; i < vectorA.length; i++) {
            BigDecimal product = vectorA[i].multiply(vectorB[i]);
            dotProduct = dotProduct.add(product);
        }
        return dotProduct;
    }

    public static BigDecimal norm(BigDecimal[] v) {
        BigDecimal sum= BigDecimal.ZERO;
        for (BigDecimal dim : v) {
            sum=sum.add(dim.multiply(dim));
        }
        BigDecimal norm = sum.sqrt(mc);
        return norm;
    }
}
