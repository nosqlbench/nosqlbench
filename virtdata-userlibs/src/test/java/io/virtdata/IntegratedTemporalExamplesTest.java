package io.virtdata;

import io.nosqlbench.virtdata.api.DataMapper;
import io.nosqlbench.virtdata.api.VirtData;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This set of tests demonstrates some data mapper construction techniques
 * around temporal types. Each of the tests also asserts specific result
 * values. This demonstrates that the functions act as idempotent operations.
 * They are pure functions, even though not always one-to-one functions.
 */
@Test(singleThreaded = true)
public class IntegratedTemporalExamplesTest {

    /**
     * Generate a random selection of values in timeuuid format,
     * starting at 2017-01-01 23:59:59, inclusive, and covering
     * 60000 possible different values.
     * All values should be within the minute after the base time,
     * given that 60000 millis is 60 seconds.
     */
    @Test
    public void timeuuidRangeExample() {
        DataMapper<UUID> uuidgen = VirtData.getMapper(
                "compose HashRange(0,60000); ToEpochTimeUUID('2017-01-01 23:59:59') -> java.util.UUID;",
                UUID.class
        );
        UUID uuid1 = uuidgen.get(1L);
        System.out.println(uuid1);
        assertThat(uuid1).isEqualTo(UUID.fromString("998ccf20-ee50-1398-8000-000000000000"));
    }

    /**
     * Generate a selection of UUID values just like above, except with a skew in the
     * distribution. The cardinality of values produced over many inputs will be
     * determined by the cardinality of the statistical distribution chosen.
     * Although both functions in the example are pure functions in terms of not
     * having side-effects, the distribution function is not a one-to-one function
     * by design, although the time mapping function is. That simply means that differing
     * values for the distribution function may produce the same result, although any
     * given value will always produce the same result.
     */
    @Test
    public void timeuuidSkewExample() {
        DataMapper<UUID> uuidgen = VirtData.getMapper(
                "compose zipf(10,2); ToEpochTimeUUID('2017-01-01 23:59:59') -> java.util.UUID;",
                UUID.class
        );
        UUID uuid1 = uuidgen.get(1L);
        System.out.println(uuid1);
        assertThat(uuid1).isEqualTo(UUID.fromString("7a4637a0-ee50-1398-8000-000000000000"));
    }

    /**
     * Cycle through the values from 0 to 9 repeatedly, and use them
     * as the instant for a new {@link java.util.Date} object.
     */
    @Test
    public void cyclingDateRangeExample() {
        DataMapper<Date> dateMapper = VirtData.getMapper(
                "compose Mod(10); ToDate() -> java.util.Date;",
                Date.class
        );
        Date date1 = dateMapper.get(3L);
        assertThat(date1).isEqualTo(new Date(3L));
        Date date2 = dateMapper.get(13L);
        assertThat(date2).isEqualTo(new Date(3L));
    }

    /**
     * Select an apparently (but not really) random value by hashing
     * the input, and convert that to a raw {@link java.util.Date} object.
     */
    @Test
    public void dateInRangeExample() {
        DataMapper<Date> dateMapper = VirtData.getMapper(
                "compose HashRange(0,10000000); ToDate() -> java.util.Date;",
                Date.class
        );
        Date date = dateMapper.get(3L);

        assertThat(date).isEqualTo(new Date(2527683L));
    }

    /**
     * Select an apparently (but not really) random value by hashing
     * the input, and convert that to a {@link org.joda.time.DateTime}
     * object in {@link org.joda.time.DateTimeZone#UTC} form.
     */
    @Test
    public void dateTimeInRangeExample() {
        DataMapper<DateTime> dateTimeMapper = VirtData.getMapper(
                "compose HashRange(0,10000000); ToDateTime() -> org.joda.time.DateTime;",
                DateTime.class
        );
        DateTime dt = dateTimeMapper.get(6L);
        assertThat(dt).isEqualTo(
                new DateTime(
                        1970,
                        1,
                        1,
                        2,
                        43,
                        10,
                        106,
                        DateTimeZone.UTC)
        );
    }

    /**
     * Advance the input value to a known point in time, and then convert
     * it to a {@link org.joda.time.DateTime} object in {@link org.joda.time.DateTimeZone#UTC}.
     */
    @Test
    public void manualOffsetDateTimeExample() {
        DataMapper<DateTime> dateTimeMapper = VirtData.getMapper(
                "compose StartingEpochMillis('2015-01-01'); ToDateTime()-> org.joda.time.DateTime;",
                DateTime.class
        );
        DateTime dt = dateTimeMapper.get(6L);
        assertThat(dt).isEqualTo(
                new DateTime(2015, 1, 1, 0, 0, 0, 6,
                        DateTimeZone.UTC)
        );

    }

    /**
     * Draw a random (but not really) sample from a known discrete distribution,
     * then advance it to a known point in time, then convert that it to a
     * {@link org.joda.time.DateTime} in {@link org.joda.time.DateTimeZone#UTC}
     */
    @Test
    public void manualOffsetAndSkewedDateTimeExample() {
        DataMapper<DateTime> dateTimeMapper = VirtData.getMapper(
                "compose zipf(10,2); StartingEpochMillis('2015-01-01'); ToDateTime()-> org.joda.time.DateTime;",
                DateTime.class
        );
        DateTime dt = dateTimeMapper.get(6L);
        assertThat(dt).isEqualTo(
                new DateTime(2015, 1, 1, 0, 0, 0, 2,
                        DateTimeZone.UTC)
        );

    }

}
