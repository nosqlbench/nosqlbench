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

import java.util.function.LongUnaryOperator;

/**
 * Return the epoch milliseconds at the start of the minute for the given
 * epoch milliseconds.
 */
@Categories({Category.datetime})
@ThreadSafeMapper
public class ToMillisAtStartOfMinute implements LongUnaryOperator {
    private final DateTimeZone tz;

    @Example({"ToMillisAtStartOfMinute()","return millisecond epoch time of the start of the minute of the provided millisecond epoch time, assuming UTC"})
    public ToMillisAtStartOfMinute() {
        this(DateTimeZone.UTC.getID());
    }

    @Example({"ToMillisAtStartOfMinute('America/Chicago')","return millisecond epoch time of the start of the minute of the provided millisecond epoch time, using timezone America/Chicago"})
    public ToMillisAtStartOfMinute(String timezoneId) {
        this.tz = Timezones.forId(timezoneId);
    }

    @Override
    public long applyAsLong(long operand) {
        return new DateTime(operand,tz).withMillisOfSecond(0).withSecondOfMinute(0).getMillis();
    }
}
