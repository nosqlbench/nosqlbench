package io.virtdata.libbasics.shared.from_long.to_time_types.joda;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.function.LongUnaryOperator;

/**
 * Return the epoch milliseconds at the start of the day for the given
 * epoch milliseconds.
 */
@Categories({Category.datetime})
@ThreadSafeMapper
public class ToMillisAtStartOfDay implements LongUnaryOperator {
    private final DateTimeZone tz;

    @Example({"ToMillisAtStartOfDay()","return millisecond epoch time of the start of the day of the provided millisecond epoch time, assuming UTC"})
    public ToMillisAtStartOfDay() {
        this(DateTimeZone.UTC.getID());
    }

    @Example({"ToMillisAtStartOfDay('America/Chicago')","return millisecond epoch time of the start of the day of the provided millisecond epoch time, using timezone America/Chicago"})
    public ToMillisAtStartOfDay(String timezoneId) {
        this.tz = Timezones.forId(timezoneId);
    }

    @Override
    public long applyAsLong(long operand) {
        return new DateTime(operand,tz).withTimeAtStartOfDay().getMillis();
    }
}
