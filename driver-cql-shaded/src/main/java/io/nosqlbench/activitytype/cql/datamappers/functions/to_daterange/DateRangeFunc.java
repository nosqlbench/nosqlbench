package io.nosqlbench.activitytype.cql.datamappers.functions.to_daterange;

import com.datastax.driver.dse.search.DateRange;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

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

    @Example({
        "StartingEpochMillis('2017-01-01 23:59:59'); DateRangeFunc('second',Identity(),Add(3600000L)",
        "Create 1-minute date ranges starting at 2017-01-01 23:59:59"})
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

    public DateRangeFunc(String precision, LongUnaryOperator lower, Function<Long,Long> upper) {
        this.precision = DateRange.DateRangeBound.Precision.valueOf(precision.toUpperCase());
        this.lower = lower;
        this.upper = upper::apply;
    }
    public DateRangeFunc(String precision, LongFunction<Long> lower, LongUnaryOperator upper) {
        this.precision = DateRange.DateRangeBound.Precision.valueOf(precision.toUpperCase());
        this.lower = lower::apply;
        this.upper = upper;
    }
    public DateRangeFunc(String precision, Function<Long,Long> lower, LongFunction<Long> upper) {
        this.precision = DateRange.DateRangeBound.Precision.valueOf(precision.toUpperCase());
        this.lower = lower::apply;
        this.upper = upper::apply;
    }

    public DateRangeFunc(String precision, LongUnaryOperator lower, LongFunction<Long> upper) {
        this.precision = DateRange.DateRangeBound.Precision.valueOf(precision.toUpperCase());
        this.lower = lower;
        this.upper = upper::apply;
    }
    public DateRangeFunc(String precision, LongFunction<Long> lower, Function<Long,Long> upper) {
        this.precision = DateRange.DateRangeBound.Precision.valueOf(precision.toUpperCase());
        this.lower = lower::apply;
        this.upper = upper::apply;
    }
    public DateRangeFunc(String precision, Function<Long,Long> lower, LongUnaryOperator upper) {
        this.precision = DateRange.DateRangeBound.Precision.valueOf(precision.toUpperCase());
        this.lower = lower::apply;
        this.upper = upper;
    }

    @Override
    public DateRange apply(long value) {
        Date lowerDate = new Date(lower.applyAsLong(value));
        DateRange.DateRangeBound lower = DateRange.DateRangeBound.lowerBound(lowerDate,precision);
        Date upperDate = new Date(upper.applyAsLong(value));
        DateRange.DateRangeBound upper = DateRange.DateRangeBound.upperBound(upperDate,precision);
        DateRange dateRange = new DateRange(lower, upper);
        return dateRange;
    }
}
