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
import java.util.function.BiFunction;

public class DNN_angular_distance_double implements BiFunction<double[],double[],Double> {
    private final static MathContext mc = new MathContext(256, RoundingMode.HALF_EVEN);
    @Override
    public Double apply(double[] v1, double[] v2) {
        double dot = dot(v1, v2);
        double normv1 = norm(v1);
        double normv2 = norm(v2);
        double norm = normv1*normv2;
        double cos_theta = dot/norm;
        return cos_theta;
    }

    public static double dot(double[] vectorA, double[] vectorB) {
        double dotProduct = 0.0d;
        for (int i = 0; i < vectorA.length; i++) {
            double product = vectorA[i] * vectorB[i];
            dotProduct += product;
        }
        return dotProduct;
    }

    public static double norm(double[] v) {
        double sum= 0.0d;
        for (double dim : v) {
            sum+=(dim*dim);
        }
        double norm = Math.sqrt(sum);
        return norm;
    }
}
