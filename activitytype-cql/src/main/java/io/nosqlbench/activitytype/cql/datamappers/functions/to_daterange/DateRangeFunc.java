package io.nosqlbench.activitytype.cql.datamappers.functions.to_daterange;

import com.datastax.driver.dse.search.DateRange;
import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.Example;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.Date;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;

/**
 * Uses the precision and the two functions provided to create a DateRange.
 * You can use any of these precisions to control the bounds
 * around the provided timestamp: millisecond, second, minute, hour, day, month, or year.
 */
@ThreadSafeMapper
@Categories(Category.datetime)
public class DateRangeFunc implements LongFunction<DateRange> {

    private final DateRange.DateRangeBound.Precision precision;
    private final LongUnaryOperator lower;
    private final LongUnaryOperator upper;

    public DateRangeFunc(String precision, LongUnaryOperator lower, LongUnaryOperator upper) {
        this.precision = DateRange.DateRangeBound.Precision.valueOf(precision.toUpperCase());
        this.lower = lower;
        this.upper = upper;
    }
    public DateRangeFunc(String precision, LongFunction<Long> lower, LongFunction<Long> upper) {
        this.precision = DateRange.DateRangeBound.Precision.valueOf(precision.toUpperCase());
        this.lower = lower::apply;
        this.upper = upper::apply;
    }
    public DateRangeFunc(String precision, Function<Long,Long> lower, Function<Long,Long> upper) {
        this.precision = DateRange.DateRangeBound.Precision.valueOf(precision.toUpperCase());
        this.lower = lower::apply;
        this.upper = upper::apply;
    }



    @Override
    public DateRange apply(long value) {
        Date lowerDate = new Date(lower.applyAsLong(value));
        DateRange.DateRangeBound lower = DateRange.DateRangeBound.lowerBound(lowerDate,precision);
        Date upperDate = new Date(upper.applyAsLong(value));
        DateRange.DateRangeBound upper = DateRange.DateRangeBound.upperBound(lowerDate,precision);
        DateRange dateRange = new DateRange(lower, upper);
        return dateRange;
    }
}
