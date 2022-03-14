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
import com.datastax.dse.driver.api.core.data.time.DateRangePrecision;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.text.ParseException;
import java.util.function.Function;

/**
 * Parses the DateRange format according to <A HREF="https://lucene.apache.org/solr/guide/6_6/working-with-dates
 * .html#WorkingwithDates-DateRangeFormatting">Date Range Formatting</A>.
 * When possible it is more efficient to use the other DateRange methods since they do not require parsing.
 */
@ThreadSafeMapper
@Categories(Category.datetime)
public class DateRangeParser implements Function<String, DateRange> {

    private final DateRangePrecision precision;

    @Example({"DateRangeParser()}","Convert inputs like '[1970-01-01T00:00:00 TO 1970-01-01T00:00:00]' into " +
        "DateRanges" +
        " "})
    public DateRangeParser(String precision) {
        this.precision = DateRangePrecision.valueOf(precision.toUpperCase());
    }

    @Override
    public DateRange apply(String value) {
        try {
            return DateRange.parse(value);
        } catch (ParseException e) {
            throw new RuntimeException("unable to parse date range input '" + value + "': " + e.getMessage());
        }
    }
}
