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

package io.nosqlbench.virtdata.lib.vectors.algorithms;

import java.util.List;

public class GoldenAngle implements CircleAlgorithm {

    private final static double goldenAngle = 137.5;

    @Override
    public List<Object> getVector(long value, long circleCount) {
        double y = 1 - (value / (double) (circleCount - 1)) * 2;
        double radius = Math.sqrt(1 - y * y);
        double theta = goldenAngle * value;
        double x = Math.cos(theta) * radius;
        double z = Math.sin(theta) * radius;

        return List.of((float)x, (float)y, (float)z);
    }

    @Override
    public double getMinimumVectorAngle() {
        return 0;
    }
}
