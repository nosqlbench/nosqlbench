package io.nosqlbench.activitytype.cql.datamappers.functions.long_localdate;

import com.datastax.driver.core.LocalDate;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

/**
 * Converts epoch millis to a
 * com.datastax.driver.core.{@link LocalDate} object, as
 * the number of milliseconds since January 1st, 1970 GMT.
 */
@ThreadSafeMapper
@Categories({Category.datetime})
public class EpochMillisToCqlLocalDate implements LongFunction<LocalDate> {

    @Example({"EpochMillisToJavaLocalDate()", "Yields the LocalDate for the millis in GMT"})
    public EpochMillisToCqlLocalDate() {
    }

    @Override
    public LocalDate apply(long value) {
        return LocalDate.fromMillisSinceEpoch(value);
    }
}
