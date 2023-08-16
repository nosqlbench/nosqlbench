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

package io.nosqlbench.virtdata.library.basics.shared.vectors.primitive;

public class GoldenAngleDoubleVectors extends VectorSequence {
    private final static double goldenAngle = 137.5;

    public GoldenAngleDoubleVectors(long vectorCount) {
        super(vectorCount);
    }

    @Override
    public double[] apply(long value) {
        double y = 1 - (value / (double) (cardinality - 1)) * 2;
        double radius = Math.sqrt(1 - y * y);
        double theta = goldenAngle * value;
        return new double[] {Math.cos(theta) * radius, y, Math.sin(theta) * radius};
    }

    @Override
    public long getDimensions() {
        return 3;
    }
}
