package io.nosqlbench.activitytype.cql.datamappers.functions.to_daterange;

import org.junit.jupiter.api.Test;

import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

public class DateRangeFuncTest {

    @Test
    public void testDateRangeFuncs() {
        LongFunction<Long> lf1 = value -> value;
        DateRangeFunc function = new DateRangeFunc("second", lf1, lf1);

        assertThat(function.apply(42L).toString())
            .isEqualTo("[1970-01-01T00:00:00 TO 1970-01-01T00:00:00]");
        assertThat(function.apply(42000L).toString())
            .isEqualTo("[1970-01-01T00:00:42 TO 1970-01-01T00:00:42]");
        assertThat(function.apply(42000000L).toString())
            .isEqualTo("[1970-01-01T11:40:00 TO 1970-01-01T11:40:00]");
        assertThat(function.apply(42000000000L).toString())
            .isEqualTo("[1971-05-02T02:40:00 TO 1971-05-02T02:40:00]");
        assertThat(function.apply(42000000000000L).toString())
            .isEqualTo("[3300-12-05T02:40:00 TO 3300-12-05T02:40:00]");

        LongUnaryOperator lf2 = value -> value;

        function = new DateRangeFunc("second", lf2, lf2);
        assertThat(function.apply(42L).toString())
            .isEqualTo("[1970-01-01T00:00:00 TO 1970-01-01T00:00:00]");
        assertThat(function.apply(42000L).toString())
            .isEqualTo("[1970-01-01T00:00:42 TO 1970-01-01T00:00:42]");
        assertThat(function.apply(42000000L).toString())
            .isEqualTo("[1970-01-01T11:40:00 TO 1970-01-01T11:40:00]");
        assertThat(function.apply(42000000000L).toString())
            .isEqualTo("[1971-05-02T02:40:00 TO 1971-05-02T02:40:00]");
        assertThat(function.apply(42000000000000L).toString())
            .isEqualTo("[3300-12-05T02:40:00 TO 3300-12-05T02:40:00]");

        Function<Long,Long> lf3 = value -> value;

        function = new DateRangeFunc("second", lf3, lf3);
        assertThat(function.apply(42L).toString())
            .isEqualTo("[1970-01-01T00:00:00 TO 1970-01-01T00:00:00]");
        assertThat(function.apply(42000L).toString())
            .isEqualTo("[1970-01-01T00:00:42 TO 1970-01-01T00:00:42]");
        assertThat(function.apply(42000000L).toString())
            .isEqualTo("[1970-01-01T11:40:00 TO 1970-01-01T11:40:00]");
        assertThat(function.apply(42000000000L).toString())
            .isEqualTo("[1971-05-02T02:40:00 TO 1971-05-02T02:40:00]");
        assertThat(function.apply(42000000000000L).toString())
            .isEqualTo("[3300-12-05T02:40:00 TO 3300-12-05T02:40:00]");

    }

}
