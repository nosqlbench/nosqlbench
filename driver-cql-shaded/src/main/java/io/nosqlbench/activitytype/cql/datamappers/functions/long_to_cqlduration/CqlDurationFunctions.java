package io.nosqlbench.activitytype.cql.datamappers.functions.long_to_cqlduration;

import com.datastax.driver.core.Duration;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VirtDataConversions;

import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;

/**
 * Map a long value into a CQL Duration object based on a set of field functions.
 */
@ThreadSafeMapper
@Categories({Category.datetime})
public class CqlDurationFunctions implements LongFunction<Duration> {

    private final LongToIntFunction monthsfunc;
    private final LongToIntFunction daysfunc;
    private final LongUnaryOperator nanosfunc;

    /**
     * Create a CQL Duration object from the two provided field functions. The months field is always set to
     * zero in this form.
     * @param monthsFunc A function that will yield the months value
     * @param daysFunc A function that will yield the days value
     * @param nanosFunc A function that will yeild the nanos value
     */
    public CqlDurationFunctions(Object monthsFunc, Object daysFunc, Object nanosFunc) {
        this.monthsfunc = VirtDataConversions.adaptFunction(monthsFunc, LongToIntFunction.class);
        this.daysfunc = VirtDataConversions.adaptFunction(daysFunc, LongToIntFunction.class);
        this.nanosfunc = VirtDataConversions.adaptFunction(nanosFunc, LongUnaryOperator.class);
    }

    /**
     * Create a CQL Duration object from the two provided field functions. The months field is always set to
     * zero in this form.
     * @param daysFunc A function that will yield the days value
     * @param nanosFunc A function that will yeild the nanos value
     */
    public CqlDurationFunctions(Object daysFunc, Object nanosFunc) {
        this.monthsfunc = (v) -> 0;
        this.daysfunc = VirtDataConversions.adaptFunction(daysFunc, LongToIntFunction.class);
        this.nanosfunc = VirtDataConversions.adaptFunction(nanosFunc, LongUnaryOperator.class);
    }


    @Override
    public Duration apply(long value) {
        int months = monthsfunc.applyAsInt(value);
        int days = daysfunc.applyAsInt(value);
        long nanos = nanosfunc.applyAsLong(value);
        return Duration.newInstance(months,days,nanos);
    }
}
