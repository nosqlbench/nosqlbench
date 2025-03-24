package io.nosqlbench.virtdata.library.basics.shared.conversions.from_time_types.from_joda_datetime;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToDate implements Function<DateTime, Date> {
    @Override
    public Date apply(DateTime jodaDateTime) {
        return Date.from(new ToJavaInstant().apply(jodaDateTime));
    }
}
