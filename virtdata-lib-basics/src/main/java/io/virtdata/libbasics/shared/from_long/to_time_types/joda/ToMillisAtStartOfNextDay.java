package io.virtdata.libbasics.shared.from_long.to_time_types.joda;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.function.LongUnaryOperator;

/**
 * Return the epoch milliseconds at the start of the day after the day for the given
 * epoch milliseconds.
 */
@Categories({Category.datetime})
@ThreadSafeMapper
public class ToMillisAtStartOfNextDay implements LongUnaryOperator {
    DateTimeZone tz = DateTimeZone.UTC;

    @Example({"ToMillisAtStartOfNextDay()","return millisecond epoch time of the start of next day (not including day-of) of the provided millisecond epoch time, assuming UTC"})
    public ToMillisAtStartOfNextDay() {
    }

    @Example({"ToMillisAtStartOfNextDay('America/Chicago')","return millisecond epoch time of the start of the next day (not including day-of) of the provided millisecond epoch time, using timezone America/Chicago"})
    public ToMillisAtStartOfNextDay(String timezoneId) {
        this.tz = Timezones.forId(timezoneId);
    }


    @Override
    public long applyAsLong(long operand) {
        return new DateTime(operand,tz).plusDays(1).withTimeAtStartOfDay().getMillis();
    }
}
