package io.nosqlbench.activitytype.cql.datamappers.functions.long_to_cqlduration;

import com.datastax.driver.core.Duration;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

/**
 * Convert the input value into a {@link com.datastax.driver.core.Duration}
 * by reading the input as total nanoseconds, assuming 30-month days.
 */
@ThreadSafeMapper
@Categories({Category.conversion,Category.datetime})
public class ToCqlDurationNanos implements LongFunction<Duration> {

    private final static long NS_PER_S = 1_000_000_000L;
    private final static long NS_PER_DAY = NS_PER_S * 60*60*24;
    private final static long NS_PER_MONTH = NS_PER_DAY * 30;

    @Override
    public Duration apply(long value) {
        long nanos = value % NS_PER_DAY;
        value -= nanos;
        long days = value / NS_PER_DAY;
        value -= days*NS_PER_DAY;
        long months = value / NS_PER_MONTH;
        return Duration.newInstance((int) months,(int) days, nanos);
    }
}
