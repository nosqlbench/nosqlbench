package io.nosqlbench.activitytype.cql.datamappers.functions.to_daterange;

import com.datastax.driver.dse.search.DateRange;
import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.Example;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.Date;
import java.util.function.Function;
import java.util.function.LongFunction;

/**
 * Takes an input as a reference point in epoch time, and converts it to a DateRange,
 * with the bounds set to the lower and upper timestamps which align to the
 * specified precision. You can use any of these precisions to control the bounds
 * around the provided timestamp:
 * <ul>
 *     <LI>millisecond</LI>
 *     <LI>second</LI>
 *     <LI>minute</LI>
 *     <LI>hour</LI>
 *     <li>day</li>
 *     <li>month</li>
 *     <li>year</li>
 * </ul>
 */
@ThreadSafeMapper
@Categories(Category.datetime)
public class DateRangeDuring implements LongFunction<DateRange> {

    private final com.datastax.driver.dse.search.DateRange.DateRangeBound.Precision precision;

    @Example({"DateRangeDuring('millisecond')}","Convert the incoming millisecond to an equivalent DateRange"})
    @Example({"DateRangeDuring('minute')}","Convert the incoming millisecond to a DateRange for the minute in which the " +
        "millisecond falls"})
    public DateRangeDuring(String precision) {
        this.precision = com.datastax.driver.dse.search.DateRange.DateRangeBound.Precision.valueOf(precision.toUpperCase());
    }

    @Override
    public DateRange apply(long value) {
        Date date = new Date(value);
        com.datastax.driver.dse.search.DateRange.DateRangeBound lower = com.datastax.driver.dse.search.DateRange.DateRangeBound.lowerBound(date, precision);
        com.datastax.driver.dse.search.DateRange.DateRangeBound upper = com.datastax.driver.dse.search.DateRange.DateRangeBound.upperBound(date, precision);
        com.datastax.driver.dse.search.DateRange dateRange = new com.datastax.driver.dse.search.DateRange(lower, upper);
        return dateRange;
    }
}
