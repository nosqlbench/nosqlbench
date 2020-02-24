package io.nosqlbench.virtdata.library.basics.shared.from_long.to_time_types.joda;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.Example;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.function.LongUnaryOperator;

/**
 * Return the epoch milliseconds at the start of the hour for the given
 * epoch milliseconds.
 */
@Categories({Category.datetime})
@ThreadSafeMapper
public class ToMillisAtStartOfHour implements LongUnaryOperator {
    private final DateTimeZone tz;

    @Example({"ToMillisAtStartOfHour()","return millisecond epoch time of the start of the hour of the provided millisecond epoch time, assuming UTC"})
    public ToMillisAtStartOfHour() {
        this(DateTimeZone.UTC.getID());
    }

    @Example({"ToMillisAtStartOfHour('America/Chicago')","return millisecond epoch time of the start of the hour of the provided millisecond epoch time, using timezone America/Chicago"})
    public ToMillisAtStartOfHour(String timezoneId) {
        this.tz = Timezones.forId(timezoneId);
    }

    @Override
    public long applyAsLong(long operand) {
        return new DateTime(operand,tz).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).getMillis();
    }
}
