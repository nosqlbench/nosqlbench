package io.nosqlbench.datamappers.functions.long_localdate;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.function.LongFunction;

/**
 * Converts epoch millis to a java.time.LocalDate, which takes the place
 * of the previous CQL-specific LocalDate. if a zoneid of 'default' is provided,
 * then the time zone is set by the default for the JVM. If not, then
 * a valid ZoneId is looked up. The no-args version uses GMT.
 */
@ThreadSafeMapper
@Categories({Category.datetime})
public class EpochMillisToCqlLocalDate implements LongFunction<LocalDate> {

    private final ZoneId zoneId;

    public EpochMillisToCqlLocalDate(String zoneid) {
        if (zoneid.equals("default")) {
            this.zoneId = ZoneId.systemDefault();
        } else {
            this.zoneId = ZoneId.of(zoneid);
        }
    }

    @Example({"EpochMillisToJavaLocalDate()", "Yields the LocalDate for the millis in GMT"})
    public EpochMillisToCqlLocalDate() {
        this.zoneId = ZoneId.of("GMT");
    }

    @Override
    public LocalDate apply(long value) {
        return LocalDate.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault());
    }
}
