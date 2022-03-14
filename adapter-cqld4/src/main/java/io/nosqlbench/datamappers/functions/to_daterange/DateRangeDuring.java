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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.LongFunction;

/**
 * Takes an input as a reference point in epoch time, and converts it to a DateRange,
 * with the bounds set to the lower and upper timestamps which align to the
 * specified precision. You can use any of these precisions to control the bounds
 * around the provided timestamp: millisecond, second, minute, hour, day, month, or year.
 *
 * If the zoneid is not specified, it defaults to "GMT". If the zoneid is set to "default",
 * then the zoneid is set to the default for the JVM. Otherwise, the specified zone is used.
 */
@ThreadSafeMapper
@Categories(Category.datetime)
public class DateRangeDuring implements LongFunction<DateRange> {

    private final DateRangePrecision precision;
    private final ZoneId zoneid;

    @Example({"DateRangeDuring('millisecond')}","Convert the incoming millisecond to an equivalent DateRange"})
    @Example({"DateRangeDuring('minute')}","Convert the incoming millisecond to a DateRange for the minute in which the " +
        "millisecond falls"})
    public DateRangeDuring(String precision) {
        this(precision,"GMT");
    }

    public DateRangeDuring(String precision, String zoneid) {
        this.precision = DateRangePrecision.valueOf(precision.toUpperCase());
        if (zoneid.equals("default")) {
            this.zoneid = ZoneId.systemDefault();
        } else {
            this.zoneid = ZoneId.of(zoneid);
        }
    }



    @Override
    public DateRange apply(long value) {
        ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(value), zoneid);
        DateRangeBound lower = DateRangeBound.lowerBound(date, precision);
        DateRangeBound upper = DateRangeBound.upperBound(date, precision);
        DateRange dateRange = new DateRange(lower, upper);
        return dateRange;
    }
}
