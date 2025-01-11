package io.nosqlbench.virtdata.library.basics.shared.from_long.to_double;

import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Hash;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.InterpolateTest;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmpiricalDistributionTest {

    @Test
    @Disabled("performance intensive")
    public void testUniform() {
        EmpiricalDistribution d =
            new EmpiricalDistribution(0.0d, 0.0d, 1.0d, 1.0d);
        DescriptiveStatistics data = InterpolateTest.tabulate(new Hash(), d, 1000000000);
        assertThat(data.getPercentile(0.0001d)).isCloseTo(0.0d, Offset.offset(0.0001));
        assertThat(data.getPercentile(50.0d)).isCloseTo(0.5d, Offset.offset(0.005));
        assertThat(data.getPercentile(100.0d)).isCloseTo(1.0d, Offset.offset(0.001));
    }

    /// convergence to expected value at different number of samples and LERP resolution
    ///
    /// @100000 / 100
    /// p50 = 0.09961336762080965
    /// p55 = 0.4887600943079539
    /// p80 = 0.9486573852803234
    /// @100000 / 1000
    /// p50 = 0.0996064221289679
    /// p55 = 0.4887600943079539
    /// p80 = 0.9497494462965901
    ///
    /// @1000000 / 100
    /// p50 = 0.10105949687725542
    /// p55 = 0.49758658404616063
    /// p80 = 0.9486389093179619
    /// @1000000 / 1000
    /// p50 = 0.10105949687725548
    /// p55 = 0.49758658404616074
    /// p80 = 0.9497305556617565
    ///
    /// @10000000 / 100
    /// p50 = 0.1000117051372746
    /// p55 = 0.4997387848207568
    /// p80 = 0.9487722639153554
    /// @10000000 / 1000
    /// p50 = 0.10001170513727448
    /// p55 = 0.4997387848207569
    /// p80 = 0.9498669032551016
    ///
    /// @100000000 / 100
    /// p50 = 0.0999966957844636
    /// p55 = 0.5001328046490157
    /// p80 = 0.9487758571324978
    /// @100000000 / 1000
    /// p50 = 0.09999663642729828
    /// p55 = 0.5001328046490157
    /// p80 = 0.9498705771180153
    ///
    /// @1000000000 / 100
    /// p50 = 0.09999563860575955
    /// p55 = 0.5000398035892097
    /// p80 = 0.9487774978532897
    /// @1000000000 / 1000
    ///
    ///
    @Test
    @Disabled("performance intensive")
    public void testPieceWise() {
        EmpiricalDistribution d =
            new EmpiricalDistribution(0.0d, 0.0d, 0.5d, 0.1d, 0.6d, 0.9d, 1.0d, 1.0d);
        DescriptiveStatistics data = InterpolateTest.tabulate(new Hash(), d, 1000000000);
        assertThat(data.getPercentile(0.0001d)).isCloseTo(0.0d, Offset.offset(0.01));
        assertThat(data.getPercentile(25.0d)).isCloseTo(0.05d, Offset.offset(0.01));

        // was 0.101059
        double p50 = data.getPercentile(50.0d);
        System.out.println("p50 = " + p50);
        assertThat(p50).isCloseTo(0.1d, Offset.offset(0.005));

        // was 0.4975865
        double p55 = data.getPercentile(55.0d);
        System.out.println("p55 = " + p55);
        assertThat(p55).isCloseTo(0.5d, Offset.offset(0.1));

        // was 0.948638
        double p80 = data.getPercentile(80.0d);
        System.out.println("p80 = " + p80);
        assertThat(p80).isCloseTo(0.95d, Offset.offset(0.005));

        assertThat(data.getPercentile(100.0d)).isCloseTo(1.0d, Offset.offset(0.001));

    }

}
