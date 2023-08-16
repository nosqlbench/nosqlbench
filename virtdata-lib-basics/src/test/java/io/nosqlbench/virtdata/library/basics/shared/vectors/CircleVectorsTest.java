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

package io.nosqlbench.virtdata.library.basics.shared.vectors;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CircleVectorsTest {

    @Test
    public void testCircleVectors() {
        try {
            CircleVectors circleVectors = new CircleVectors(10,
                "io.nosqlbench.virtdata.library.basics.shared.vectors.algorithms.GoldenAngle");

            assert (circleVectors.getCircleCount() == 10);
            assert (circleVectors.getAlgorithm() instanceof
                io.nosqlbench.virtdata.library.basics.shared.vectors.algorithms.GoldenAngle);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void whenExceptionThrownForWrongClass() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            CircleVectors circleVectors = new CircleVectors(10,"java.util.Date");
        });

        String expectedMessage = "The class 'java.util.Date' does not implement CircleAlgorithm";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
    @Test
    public void testGoldenAngle() {
        try {
            CircleVectors circleVectors = new CircleVectors(10,
                "io.nosqlbench.virtdata.library.basics.shared.vectors.algorithms.GoldenAngle");

            List<Object> result = circleVectors.apply(1000);
            assert (result.size() == 3);
            assertEquals(0.0f, result.get(0));
            assertEquals(1.0f, result.get(1));
            assertEquals(0.0f, result.get(2));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testLatLonBased() {
        try {
            CircleVectors circleVectors = new CircleVectors(10,
                "io.nosqlbench.virtdata.library.basics.shared.vectors.algorithms.LatLonBased");

            List<Object> result = circleVectors.apply(1000);
            assert (result.size() == 3);
            assertEquals(6.123234E-17f, result.get(0));
            assertEquals(0.0f, result.get(1));
            assertEquals(1.0f, result.get(2));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
