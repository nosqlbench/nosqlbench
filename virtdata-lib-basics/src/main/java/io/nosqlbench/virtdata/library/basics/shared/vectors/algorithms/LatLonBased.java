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

package io.nosqlbench.virtdata.library.basics.shared.vectors.algorithms;

import java.util.List;

public class LatLonBased implements CircleAlgorithm {
    private final static double goldenAngle = 137.5;

    @Override
    public List<Object> getVector(long value, long circleCount) {
        double longitude = 2 * Math.PI * value / circleCount;
        double latitude = Math.asin(1 - 2 * (double) value / (circleCount - 1));
        double x = Math.cos(latitude) * Math.cos(longitude);
        double y = Math.cos(latitude) * Math.sin(longitude);
        double z = Math.sin(latitude);

        return List.of((float)x, (float)y, (float)z);
    }

    @Override
    public double getMinimumVectorAngle() {
        return 0;
    }
}
