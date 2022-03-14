/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.virtdata.library.curves4.discrete.int_int;

import io.nosqlbench.virtdata.library.curves4.discrete.common.DiscreteIntIntSampler;
import io.nosqlbench.virtdata.library.curves4.discrete.common.IntegerDistributionICDSource;
import io.nosqlbench.virtdata.library.curves4.discrete.common.InterpolatingIntIntSampler;
import org.apache.commons.statistics.distribution.DiscreteDistribution;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.DoubleToIntFunction;
import java.util.function.IntUnaryOperator;

/*
 * Generate samples according to the specified probability density.
 *
 * The input value consists of a long between 0L and Long.MAX_VALUE.
 * This value is scaled to the unit interval (0.0, 1.0) as
 * an index into a sampling function based on inverse cumulative
 * density sampling.
 *
 * <H3>Sampling Mode</H3>
 *
 * The curve can be sampled in either map or hash mode. Map mode
 * simply indexes into the probability curve in the order that
 * it would appear on a density plot. Hash mode applies a
 * murmur3 hash to the input value before scaling from the
 * range of longs to the unit interval, thus providing a pseudo-random
 * sample of a value from the curve. This is usually what you want,
 * so hash mode is the default.  To enable map mode, simply provide
 * "map" as one of the modifiers as explained below.
 *
 * <H3>Interpolation</H3>
 *
 * The curve can be computed from the sampling function for each value
 * generated, or it can be provided via interpolation with a lookup table.
 * Using interpolation makes all the generator functions perform the
 * same. This is almost always what you want, so interpolation is
 * enabled by default. In order to compute the value for every sample
 * instead, simply provide "compute" as one of the modifiers as explained
 * below.
 *
 * You can add optional modifiers after the distribution parameters.
 * You can add one of 'hash' or 'map' but not both. If neither of these is
 * added, 'hash' is implied as a default.
 * You can add one of 'interpolate' or 'compute' but not both. If neither
 * of these is added, 'interpolate' is implied as a default.
 *
 * At times, it might be useful to add 'hash', 'interpolate' to your
 * specifiers as a form of verbosity or explicit specification.
 */

/**
 */
public class IntToIntDiscreteCurve implements IntUnaryOperator {

    private final DiscreteDistribution distribution;
    private final IntUnaryOperator function;

    public final static String COMPUTE="compute";
    public final static String INTERPOLATE="interpolate";
    public final static String MAP="map";
    public final static String HASH="hash";
    private final static HashSet<String> validModifiers = new HashSet<String>() {{
        add(COMPUTE);
        add(INTERPOLATE);
        add(MAP);
        add(HASH);
    }};


    public IntToIntDiscreteCurve(DiscreteDistribution distribution, String... modslist) {
        this.distribution = distribution;
        HashSet<String> mods = new HashSet<>(Arrays.asList(modslist));

        DoubleToIntFunction icdSource = new IntegerDistributionICDSource(distribution);

        if (mods.contains(HASH) && mods.contains(MAP)) {
            throw new RuntimeException("mods must not contain both "+HASH+" and "+MAP+".");
        }
        if (mods.contains(INTERPOLATE) && mods.contains(COMPUTE)) {
            throw new RuntimeException("mods must not contain both "+INTERPOLATE+" and "+COMPUTE+".");
        }
        for (String s : modslist) {
            if (!validModifiers.contains(s)) {
                throw new RuntimeException("modifier '" + s + "' is not a valid modifier. Use one of " + validModifiers + " instead.");
            }
        }

        boolean hash = ( mods.contains(HASH) || !mods.contains(MAP));
        boolean interpolate = ( mods.contains(INTERPOLATE) || !mods.contains(COMPUTE));

        function = interpolate ?
                new InterpolatingIntIntSampler(icdSource, 1000, hash)
                :
                new DiscreteIntIntSampler(icdSource, hash);
    }

    @Override
    public int applyAsInt(int operand) {
        return function.applyAsInt(operand);
    }
}
