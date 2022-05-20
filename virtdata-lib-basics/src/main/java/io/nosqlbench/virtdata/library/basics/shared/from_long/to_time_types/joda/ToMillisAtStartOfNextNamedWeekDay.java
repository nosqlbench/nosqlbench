package io.nosqlbench.virtdata.library.basics.shared.from_long.to_time_types.joda;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Arrays;
import java.util.function.LongUnaryOperator;

/**
 * Return the epoch milliseconds at the start of the next day
 * that falls on the given weekday for the given
 * epoch milliseconds, not including the current day.
 */
@Categories({Category.datetime})
@ThreadSafeMapper
public class ToMillisAtStartOfNextNamedWeekDay implements LongUnaryOperator {
    private final DateTimeZone tz;
    private final int weekdayReference;


    private enum WeekDays {
        Monday(1),
        Tuesday(2),
        Wednesday(3),
        Thursday(4),
        Friday(5),
        Saturday(6),
        Sunday(7);

        private final int ordinal;

        WeekDays(int jodaOrdinal) {
            this.ordinal = jodaOrdinal;
        }

        public static int valueOfOrdinal(String weekdayName) {
            for (WeekDays value : values()) {
                if (value.toString().toLowerCase().equals(weekdayName.toLowerCase())) {
                    return value.ordinal;
                }
            }
            throw new RuntimeException(
                    "Unable to map weekday name " + weekdayName + " to values: " + Arrays.toString(WeekDays.values())
            );
        }
    }

    @Example({"ToMillisAtStartOfNextNamedWeekDay()","return millisecond epoch time of the start of the next Monday (not the day-of) of the provided millisecond epoch time, assuming UTC"})
    public ToMillisAtStartOfNextNamedWeekDay() {
        this("Monday");
    }

    @Example({"ToMillisAtStartOfNextNamedWeekDay('Wednesday')","return millisecond epoch time of the start of the next Wednesday (not the day-of) of the provided millisecond epoch time, assuming UTC"})
    public ToMillisAtStartOfNextNamedWeekDay(String weekday) {
        this(weekday,DateTimeZone.UTC.getID());
    }

    @Example({"ToMillisAtStartOfNextNamedWeekDay('Saturday','America/Chicago'')","return millisecond epoch time of the start of the next Saturday (not the day-of) of the provided millisecond epoch time, using timezone America/Chicago"})
    public ToMillisAtStartOfNextNamedWeekDay(String weekday, String timezoneId) {
        this.weekdayReference= WeekDays.valueOfOrdinal(weekday);
        this.tz = Timezones.forId(timezoneId);
    }

    @Override
    public long applyAsLong(long operand) {
        DateTime ref = new DateTime(operand,tz).withTimeAtStartOfDay();
        DateTime dateTime = ref.withDayOfWeek(weekdayReference);
        return (dateTime.isBefore(ref) ? dateTime.plusDays(7).getMillis() : dateTime.getMillis());
    }
}
