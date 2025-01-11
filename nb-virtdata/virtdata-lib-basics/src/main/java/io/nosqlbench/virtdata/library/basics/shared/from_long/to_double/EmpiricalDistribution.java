package io.nosqlbench.virtdata.library.basics.shared.from_long.to_double;

import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

/// This distribution is an easy-to use and modify distribution which
/// is simply based on observed or expected frequencies. If you imagine
/// drawing a line across a chart and then being able to use that to
/// model frequencies, that is what this function does.
///
/// Values must be specified as x,y points, alternating. The x points draw a line segment
/// from left 0.0 to right 1.0 on the unit interval, and the y points
/// plot the magnitude. A LERP table with 1000 fixed points, which provides
/// substantial precision for most systems testing purposes.
///
/// It is valid to have y values repeated, which is another way of saying that part
/// of the sampled population will have identical values. x coordinates must be monotonically
/// increasing, while y values may be any valid value, even out of order
@ThreadSafeMapper
@Categories(Category.distributions)
public class EmpiricalDistribution extends Interpolate {

    private static int lutSize = 1000;

    @Example({
        "EmpiricalDistribution(0.0d, 0.0d, 1.0d, 1.0d)",
        "Create a uniform distribution, " + "from (x,y)=0,0 to (x,y) = 1,1"
    })
    @Example({
        "EmpiricalDistribution(0.0d, 0.0d, 0.333d, 0.1d, 1.0d, 1.0d)",
        "Create a distribution where 1/3 of values range from 0.0 to 0"
        + ".1 and 2/3 range from 0.1 to 1.0"
    })
    public EmpiricalDistribution(double... values) {
        super(genTable(values));
    }

    private static double[] genTable(double[] values) {
        if (values.length < 4) {
            throw new BasicError("You must specify at least 2 x,y points, as in 0.0, 0.0, 1.0, 1"
                                 + ".0, which describes a uniform distribution");
        }
        double[] lut = new double[lutSize + 1];
        double[] offsets = new double[values.length >> 1];
        double[] magnitudes = new double[values.length >> 1];
        for (int idx = 0; idx < offsets.length; idx++) {
            offsets[idx] = values[idx << 1];
            magnitudes[idx] = values[(idx << 1) + 1];
        }
        for (int idx = 0; idx < offsets.length - 1; idx++) {
            double offsetBase = offsets[idx];
            int startIdx = (int) (offsetBase * lutSize);
            double unitFraction = (offsets[idx + 1] - offsetBase);
            if (unitFraction < 0.0) {
                throw new BasicError("offsets must be increasing");
            }
            int segmentSize = (int) (unitFraction * lutSize);
            double[] segment = new double[segmentSize + 1];
            double startMagnitude = magnitudes[idx];
            double endMagnitude = magnitudes[idx + 1];
            Interpolate segmentLine = new Interpolate(startMagnitude, endMagnitude);
            for (int ins = 0; ins < segmentSize; ins++) {
                double frac = (double) ins / (double) segment.length;
                frac = frac * (double) Long.MAX_VALUE;
                segment[ins] = segmentLine.applyAsDouble((long) frac);
            }
            segment[segment.length - 1] = endMagnitude;
            System.arraycopy(segment, 0, lut, startIdx, segment.length);
        }
        return lut;
    }
}
