package io.nosqlbench.datamappers.functions.double_to_cqlduration;

import com.datastax.oss.driver.api.core.data.CqlDuration;
import io.nosqlbench.datamappers.functions.long_to_cqlduration.CqlDurationFunctions;
import io.nosqlbench.datamappers.functions.long_to_cqlduration.ToCqlDurationNanos;
import org.junit.jupiter.api.Test;

import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

public class CqlDurationTests {

    @Test
    public void testFractionalCqlDuration() {
        ToCqlDuration cd = new ToCqlDuration();
        // only precise enough on the unit interval for this type of test
        CqlDuration oneDayPlusOneHour = cd.apply(1.0d + (1d/24D));
        assertThat(oneDayPlusOneHour).isEqualTo(CqlDuration.newInstance(0,1,1_000_000_000L*60*60));
    }

    @Test
    public void testLongToCqlDuration() {
        ToCqlDurationNanos toNanos = new ToCqlDurationNanos();
//        assertThat(toNanos.apply(1_000_000_000l * 2)).isEqualTo(Duration.newInstance(0,0,1_000_000_000*2));
        assertThat(toNanos.apply(1_000_000_000L*86401L)).isEqualTo(CqlDuration.newInstance(0,1,1_000_000_000));
    }

    @Test
    public void testFunctionCqlDuration() {
        CqlDurationFunctions composed = new CqlDurationFunctions(
            (LongToIntFunction) m -> (int) (2 * m),
            (LongToIntFunction) d -> (int) (d * 2),
            (LongUnaryOperator) n -> n * 10
        );
        CqlDuration d2y10mo34d170ns = composed.apply(17);
        assertThat(d2y10mo34d170ns).isEqualTo(
            CqlDuration.newInstance(34,34,170));
    }


}
