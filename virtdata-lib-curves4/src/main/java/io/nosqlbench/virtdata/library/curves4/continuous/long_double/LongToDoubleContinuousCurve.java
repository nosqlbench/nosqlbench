package io.nosqlbench.virtdata.library.curves4.continuous.long_double;

import io.nosqlbench.virtdata.library.curves4.continuous.common.InterpolatingLongDoubleSampler;
import io.nosqlbench.virtdata.library.curves4.continuous.common.RealDistributionICDSource;
import io.nosqlbench.virtdata.library.curves4.continuous.common.RealLongDoubleSampler;
import org.apache.commons.statistics.distribution.ContinuousDistribution;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.DoubleUnaryOperator;
import java.util.function.LongToDoubleFunction;

/*
 * Generate samples according to the specified probability density.
 *
 * The input value consists of a long between 0L and Long.MAX_VALUE.
 * This value is scaled to the unit interval (0.0, 1.0) as
 * an index into a sampling function. The method used is
 * inverse cumulative density sampling.
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
 * <H3>Clamping</H3>
 *
 * Some of the provided distributions may yield extreme values which
 * are out of the useful range for many tests. If you want these values
 * to be clamped to be within 2^63 as a maximum, then the default
 * clamping behavior will do this. If you want to have higher values
 * up to and including IEEE Infinity, then use the 'noclamp' option.
 *
 * You can add optional modifiers after the distribution parameters.
 * You can add one of 'hash' or 'map' but not both. If neither of these is
 * added, 'hash' is implied as a default.
 * You can add one of 'interpolate' or 'compute' but not both. If neither
 * of these is added, 'interpolate' is implied as a default.
 * You can add one of 'clamp' or 'noclamp' but not both. if neither of
 * these is added, 'clamp' is implied as a default.
 *
 * At times, it might be useful to add 'hash', 'interpolate', or 'clamp'
 * to your specifiers as a form of verbosity or explicit specification.
 */

/**
 */
public class LongToDoubleContinuousCurve implements LongToDoubleFunction {

    private final ContinuousDistribution distribution;
    private final LongToDoubleFunction function;

    public final static String COMPUTE="compute";
    public final static String INTERPOLATE="interpolate";

    public final static String MAP="map";
    public final static String HASH="hash";

    public final static String CLAMP="clamp";
    public final static String NOCLAMP="noclamp";

    public final static String INFINITE ="infinite";
    public final static String FINITE = "finite";

    private final static HashSet<String> validModifiers = new HashSet<String>() {{
        add(COMPUTE);
        add(INTERPOLATE);
        add(MAP);
        add(HASH);
        add(CLAMP);
        add(NOCLAMP);
        add(INFINITE);
        add(FINITE);
    }};


    public LongToDoubleContinuousCurve(ContinuousDistribution distribution, String... modslist) {
        this.distribution = distribution;
        HashSet<String> mods = new HashSet<>(Arrays.asList(modslist));

        DoubleUnaryOperator icdSource = new RealDistributionICDSource(distribution);

        if (mods.contains(HASH) && mods.contains(MAP)) {
            throw new RuntimeException("mods must not contain both "+HASH+" and "+MAP+".");
        }
        if (mods.contains(INTERPOLATE) && mods.contains(COMPUTE)) {
            throw new RuntimeException("mods must not contain both "+INTERPOLATE+" and "+COMPUTE+".");
        }
        if (mods.contains(CLAMP) && mods.contains(NOCLAMP)) {
            throw new RuntimeException("mods must not contain both "+CLAMP+" and "+NOCLAMP+".");
        }

        if (mods.contains(INFINITE) && mods.contains(FINITE)) {
            throw new RuntimeException("mods must not contain both "+ INFINITE +" and "+FINITE+".");
        }

        for (String s : modslist) {
            if (!validModifiers.contains(s)) {
                throw new RuntimeException("modifier '" + s + "' is not a valid modifier. Use one of " + validModifiers + " instead.");
            }
        }

        boolean hash = ( mods.contains(HASH) || !mods.contains(MAP));
        boolean interpolate = ( mods.contains(INTERPOLATE) || !mods.contains(COMPUTE));
        boolean clamp = ( mods.contains(CLAMP) || !mods.contains(NOCLAMP));
        boolean finite = ( mods.contains(FINITE) || !mods.contains(INFINITE));

        function = interpolate ?
                new InterpolatingLongDoubleSampler(icdSource, 1000, hash, clamp, Long.MIN_VALUE, Long.MAX_VALUE, finite)
                :
                new RealLongDoubleSampler(icdSource, hash, clamp, (double) Long.MAX_VALUE);

    }

    @Override
    public double applyAsDouble(long value) {
        return function.applyAsDouble(value);
    }
}
