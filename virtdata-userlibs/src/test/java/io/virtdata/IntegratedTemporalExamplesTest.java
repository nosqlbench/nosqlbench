/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.virtdata;

import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.VirtData;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This set of tests demonstrates some data mapper construction techniques
 * around temporal types. Each of the tests also asserts specific result
 * values. This demonstrates that the functions act as idempotent operations.
 * They are pure functions, even though not always one-to-one functions.
 */

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
                "HashRange(0,60000); ToEpochTimeUUID('2017-01-01 23:59:59') -> java.util.UUID;",
                UUID.class
        );
        UUID uuid1 = uuidgen.get(1L);
        System.out.println(uuid1);
        assertThat(uuid1).isEqualTo(UUID.fromString("826476e0-ee50-1398-8000-000000000000"));
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
                "Zipf(10,2); ToEpochTimeUUID('2017-01-01 23:59:59') -> java.util.UUID;",
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
                "Mod(10); ToDate() -> java.util.Date;",
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
                "HashRange(0,10000000); ToDate() -> java.util.Date;",
                Date.class
        );
        Date date = dateMapper.get(3L);

        assertThat(date).isEqualTo(new Date(1539133L));
    }

    /**
     * Select an apparently (but not really) random value by hashing
     * the input, and convert that to a {@link org.joda.time.DateTime}
     * object in {@link org.joda.time.DateTimeZone#UTC} form.
     */
    @Test
    public void dateTimeInRangeExample() {
        DataMapper<DateTime> dateTimeMapper = VirtData.getMapper(
                "HashRange(0,10000000); ToDateTime() -> org.joda.time.DateTime;",
                DateTime.class
        );
        DateTime dt = dateTimeMapper.get(6L);
        assertThat(dt).isEqualTo(
                new DateTime(
                        1970,
                        1,
                        1,
                        0,
                        2,
                        27,
                        245,
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
                "StartingEpochMillis('2015-01-01'); ToDateTime()-> org.joda.time.DateTime;",
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
                "Zipf(10,2); StartingEpochMillis('2015-01-01'); ToDateTime()-> org.joda.time.DateTime;",
                DateTime.class
        );
        DateTime dt = dateTimeMapper.get(6L);
        assertThat(dt).isEqualTo(
                new DateTime(2015, 1, 1, 0, 0, 0, 2,
                        DateTimeZone.UTC)
        );

    }

}
