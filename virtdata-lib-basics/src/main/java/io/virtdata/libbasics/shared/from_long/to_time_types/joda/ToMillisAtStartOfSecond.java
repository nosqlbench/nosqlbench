package io.virtdata.libbasics.shared.from_long.to_time_types.joda;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.function.LongUnaryOperator;

/**
 * Return the epoch milliseconds at the start of the second for the given
 * epoch milliseconds.
 */
@Categories({Category.datetime})
@ThreadSafeMapper
public class ToMillisAtStartOfSecond implements LongUnaryOperator {
    private final DateTimeZone tz;

    @Example({"ToMillisAtStartOfSecond()","return millisecond epoch time of the start of the second of the provided millisecond epoch time, assuming UTC"})
    public ToMillisAtStartOfSecond() {
        this(DateTimeZone.UTC.getID());
    }

    @Example({"ToMillisAtStartOfSecond('America/Chicago')","return millisecond epoch time of the start of the second of the provided millisecond epoch time, using timezone America/Chicago"})
    public ToMillisAtStartOfSecond(String timezoneId) {
        this.tz = Timezones.forId(timezoneId);
    }

    @Override
    public long applyAsLong(long operand) {
        return new DateTime(operand,tz).withMillisOfSecond(0).getMillis();
    }
}
