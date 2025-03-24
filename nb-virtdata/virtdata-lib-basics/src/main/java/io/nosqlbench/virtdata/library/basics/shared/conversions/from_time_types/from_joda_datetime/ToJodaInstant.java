package io.nosqlbench.virtdata.library.basics.shared.conversions.from_time_types.from_joda_datetime;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.joda.time.DateTime;
import org.joda.time.Instant;

import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToJodaInstant implements Function<DateTime, Instant> {
    @Override
    public Instant apply(DateTime jodaDateTime) {
        return jodaDateTime.toInstant();
    }
}
