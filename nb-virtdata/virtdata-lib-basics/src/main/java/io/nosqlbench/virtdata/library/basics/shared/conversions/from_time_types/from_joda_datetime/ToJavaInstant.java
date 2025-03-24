package io.nosqlbench.virtdata.library.basics.shared.conversions.from_time_types.from_joda_datetime;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.joda.time.DateTime;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToJavaInstant implements Function<DateTime, Instant> {
    @Override
    public Instant apply(DateTime jodaDateTime) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(jodaDateTime.getMillis()),
            ZoneId.of(jodaDateTime.getZone().getID())
        );
        return zonedDateTime.toInstant();
    }
}
