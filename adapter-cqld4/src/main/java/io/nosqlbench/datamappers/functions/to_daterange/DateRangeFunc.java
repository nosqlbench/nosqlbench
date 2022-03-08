/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.datamappers.functions.to_daterange;

import com.datastax.dse.driver.api.core.data.time.DateRange;
import com.datastax.dse.driver.api.core.data.time.DateRangeBound;
import com.datastax.dse.driver.api.core.data.time.DateRangePrecision;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VirtDataFunctions;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;

/**
 * Uses the precision and the two functions provided to create a DateRange.
 * You can use any of these precisions to control the bounds
 * around the provided timestamp: millisecond, second, minute, hour, day, month, or year.
 *
 * If the zoneid is not specified, it defaults to "GMT". If the zoneid is set to "default",
 * then the zoneid is set to the default for the JVM. Otherwise, the specified zone is used.
 */
@ThreadSafeMapper
@Categories(Category.datetime)
public class DateRangeFunc implements LongFunction<DateRange> {

    private final DateRangePrecision precision;
    private final LongUnaryOperator lower;
    private final LongUnaryOperator upper;
    private final ZoneId zoneid;

    @Example({
        "StartingEpochMillis('2017-01-01 23:59:59'); DateRangeFunc('second',Identity(),Add(3600000L)",
        "Create 1-minute date ranges starting at 2017-01-01 23:59:59"})
    public DateRangeFunc(String precision, Object lowerFunc, Object upperFunc) {
        this(precision, lowerFunc, upperFunc, "GMT");
    }

    public DateRangeFunc(String precision, Object lowerFunc, Object upperFunc, String zoneid) {
        this.precision = DateRangePrecision.valueOf(precision.toUpperCase());
        this.lower = VirtDataFunctions.adapt(lowerFunc,LongUnaryOperator.class, long.class, false);
        this.upper = VirtDataFunctions.adapt(upperFunc,LongUnaryOperator.class, long.class, false);
        if (zoneid.equals("default")) {
            this.zoneid = ZoneId.systemDefault();
        } else {
            this.zoneid = ZoneId.of(zoneid);
        }
    }

    @Override
    public DateRange apply(long value) {
        ZonedDateTime lowerDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lower.applyAsLong(value)), zoneid);
        DateRangeBound lower = DateRangeBound.lowerBound(lowerDate,precision);
        ZonedDateTime upperDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(upper.applyAsLong(value)), zoneid);
        DateRangeBound upper = DateRangeBound.upperBound(upperDate,precision);
        DateRange dateRange = new DateRange(lower, upper);
        return dateRange;
    }
}
