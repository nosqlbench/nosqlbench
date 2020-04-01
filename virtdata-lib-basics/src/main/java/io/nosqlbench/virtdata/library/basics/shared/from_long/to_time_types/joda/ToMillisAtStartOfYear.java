package io.nosqlbench.virtdata.library.basics.shared.from_long.to_time_types.joda;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.function.LongUnaryOperator;

/**
 * Return the epoch milliseconds at the start of the year for the given
 * epoch milliseconds.
 */
@Categories({Category.datetime})
@ThreadSafeMapper
public class ToMillisAtStartOfYear implements LongUnaryOperator {
    private final DateTimeZone tz;

    @Example({"ToMillisAtStartOfYear()","return millisecond epoch time of the start of the year of the provided millisecond epoch time, assuming UTC"})
    public ToMillisAtStartOfYear() {
        this(DateTimeZone.UTC.getID());
    }

    @Example({"ToMillisAtStartOfYear('America/Chicago')","return millisecond epoch time of the start of the year of the provided millisecond epoch time, using timezone America/Chicago"})
    public ToMillisAtStartOfYear(String timezoneId) {
        this.tz = Timezones.forId(timezoneId);
    }

    @Override
    public long applyAsLong(long operand) {
        return new DateTime(operand,tz).withTimeAtStartOfDay().withDayOfMonth(1).withMonthOfYear(1).getMillis();
    }
}
