/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.virtdata.library.curves4.discrete.common;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

@Tag("unit")
class GrayZipfSamplerTest {

    @Test
    void usesGrayApproximationAfterFirstTwoRanks() {
        GrayZipfSampler sampler = new GrayZipfSampler(10_000, 0.5d);

        assertThat(sampler.applyAsInt(0.1d)).isEqualTo(111);
        assertThat(sampler.applyAsInt(0.5d)).isEqualTo(2_529);
        assertThat(sampler.applyAsInt(0.9d)).isEqualTo(8_111);
    }

    @Test
    void givesFirstTwoRanksTheirExactWeights() {
        int elements = 1_000;
        double exponent = 0.75d;
        GrayZipfSampler sampler = new GrayZipfSampler(elements, exponent);
        double zetan = generalizedHarmonic(elements, exponent);
        double firstCutoff = 1.0d / zetan;
        double secondCutoff = (1.0d + Math.pow(0.5d, exponent)) / zetan;

        assertThat(sampler.applyAsInt(Math.nextDown(firstCutoff))).isEqualTo(1);
        assertThat(sampler.applyAsInt(firstCutoff)).isEqualTo(2);
        assertThat(sampler.applyAsInt(Math.nextDown(secondCutoff))).isEqualTo(2);
        assertThat(sampler.applyAsInt(secondCutoff)).isEqualTo(3);
    }

    @Test
    void supportsExponentOneAndLargerExistingExponents() {
        GrayZipfSampler exponentOne = new GrayZipfSampler(10_000, 1.0d);
        GrayZipfSampler exponentTwo = new GrayZipfSampler(10_000, 2.0d);

        assertThat(exponentOne.applyAsInt(0.5d)).isEqualTo(66);
        assertThat(exponentOne.applyAsInt(0.9d)).isEqualTo(3_658);
        assertThat(exponentTwo.applyAsInt(0.7d)).isEqualTo(2);
        assertThat(exponentTwo.applyAsInt(0.9d)).isEqualTo(5);
    }

    @Test
    void staysMonotonicAndWithinBounds() {
        for (double exponent : new double[]{0.0d, 0.5d, 0.99d, 1.0d, 2.0d, 5.0d}) {
            GrayZipfSampler sampler = new GrayZipfSampler(10_000, exponent);
            int previous = 1;
            for (int point = 0; point <= 10_000; point++) {
                int sample = sampler.applyAsInt(point / 10_000.0d);
                assertThat(sample).isBetween(1, 10_000).isGreaterThanOrEqualTo(previous);
                previous = sample;
            }
        }
    }

    @Test
    void handlesSmallDomainsAndUnitIntervalBounds() {
        GrayZipfSampler oneElement = new GrayZipfSampler(1, 0.99d);
        GrayZipfSampler twoElements = new GrayZipfSampler(2, 0.99d);
        GrayZipfSampler manyElements = new GrayZipfSampler(10_000, 0.99d);
        GrayZipfSampler maximumUniformDomain = new GrayZipfSampler(Integer.MAX_VALUE, 0.0d);

        assertThat(oneElement.applyAsInt(0.0d)).isEqualTo(1);
        assertThat(oneElement.applyAsInt(1.0d)).isEqualTo(1);
        assertThat(twoElements.applyAsInt(0.0d)).isEqualTo(1);
        assertThat(twoElements.applyAsInt(1.0d)).isEqualTo(2);
        assertThat(manyElements.applyAsInt(0.0d)).isEqualTo(1);
        assertThat(manyElements.applyAsInt(1.0d)).isEqualTo(10_000);
        assertThat(maximumUniformDomain.applyAsInt(1.0d)).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void rejectsInvalidParameters() {
        assertThatIllegalArgumentException().isThrownBy(() -> new GrayZipfSampler(0, 0.5d));
        assertThatIllegalArgumentException().isThrownBy(() -> new GrayZipfSampler(10, -0.5d));
        assertThatIllegalArgumentException().isThrownBy(
                () -> new GrayZipfSampler(10, Double.NaN));
        assertThatIllegalArgumentException().isThrownBy(
                () -> new GrayZipfSampler(10, Double.POSITIVE_INFINITY));
    }

    private static double generalizedHarmonic(int elements, double exponent) {
        double sum = 0.0d;
        for (int rank = elements; rank >= 1; rank--) {
            sum += Math.pow(rank, -exponent);
        }
        return sum;
    }
}
