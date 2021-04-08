package io.nosqlbench.activitytype.cql.datamappers.functions.double_to_cqlduration;

import com.datastax.driver.core.Duration;

import java.util.function.DoubleFunction;

/**
 * Convert the input double value into a CQL {@link Duration} object,
 * by setting months to zero, and using the fractional part as part
 * of a day, assuming 24-hour days.
 */
public class ToCqlDuration implements DoubleFunction<Duration> {

    private final static double NS_PER_DAY = 1_000_000_000L * 60 * 60 * 24;

    @Override
    public Duration apply(double value) {
        double fraction = value - (long) value;
        return Duration.newInstance(0,(int)value,(long)(fraction*NS_PER_DAY));
    }
}
