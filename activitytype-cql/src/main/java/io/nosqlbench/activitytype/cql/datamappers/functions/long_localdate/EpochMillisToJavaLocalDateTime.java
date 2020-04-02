package io.nosqlbench.activitytype.cql.datamappers.functions.long_localdate;

import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.LongFunction;

/**
 * Converts epoch millis to a
 * java.time.{@link LocalDateTime} object, using either the system
 * default timezone or the timezone provided. If the specified ZoneId is not
 * the same as the time base of the epoch millis instant, then conversion
 * errors will occur.
 *
 * Short form ZoneId values like 'CST' can be used, although US Domestic names
 * which specify the daylight savings hours are not supported. The full list of
 * short Ids at @see <a href="https://docs.oracle.com/en/java/javase/12/docs/api/java.base/java/time/ZoneId.html#SHORT_IDS">JavaSE ZoneId Ids</a>
 *
 * Any timezone specifier may be used which can be read by {@link ZoneId#of(String)}
 */
@ThreadSafeMapper
public class EpochMillisToJavaLocalDateTime implements LongFunction<LocalDateTime> {

    ZoneId timezone;

    @Example({"EpochMillisToJavaLocalDateTime()","Yields the LocalDateTime for the system default ZoneId"})
    public EpochMillisToJavaLocalDateTime() {
        this.timezone = ZoneId.systemDefault();
    }

    @Example({"EpochMillisToJavaLocalDateTime('ECT')","Yields the LocalDateTime for the ZoneId entry for 'Europe/Paris'"})
    public EpochMillisToJavaLocalDateTime(String zoneid) {
        this.timezone = ZoneId.of(zoneid);
    }

    @Override
    public LocalDateTime apply(long value) {
        return Instant.ofEpochMilli(value).atZone(timezone).toLocalDateTime();
    }
}
