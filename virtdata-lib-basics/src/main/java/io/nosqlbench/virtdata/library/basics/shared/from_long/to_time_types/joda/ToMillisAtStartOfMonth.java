package io.nosqlbench.virtdata.library.basics.shared.from_long.to_time_types.joda;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.Example;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.function.LongUnaryOperator;

/**
 * Return the epoch milliseconds at the start of the month for the given
 * epoch milliseconds.
 */
@Categories({Category.datetime})
@ThreadSafeMapper
public class ToMillisAtStartOfMonth implements LongUnaryOperator {
    private final DateTimeZone tz;

    @Example({"ToMillisAtStartOfMonth()","return millisecond epoch time of the start of the month of the provided millisecond epoch time, assuming UTC"})
    public ToMillisAtStartOfMonth() {
        this(DateTimeZone.UTC.getID());
    }

    @Example({"ToMillisAtStartOfMonth('America/Chicago')","return millisecond epoch time of the start of the month of the provided millisecond epoch time, using timezone America/Chicago"})
    public ToMillisAtStartOfMonth(String timezoneId) {
        this.tz = Timezones.forId(timezoneId);
    }

    @Override
    public long applyAsLong(long operand) {
        return new DateTime(operand,tz).withTimeAtStartOfDay().withDayOfMonth(1).getMillis();
    }
}
