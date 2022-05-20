package io.nosqlbench.virtdata.library.basics.shared.from_long.to_time_types.joda;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ToMillisAtStartOfTimeframeTests {

    private final static DateTime all3s = new DateTime(
            2018, 3, 3, 3, 3, 3, 3, DateTimeZone.UTC
    );
    private final static DateTime aGivenWednesday = new DateTime(
            2018, 12, 5, 3, 33, 42, 234, DateTimeZone.UTC
    );


    @Test
    public void testToMillisAtStartOfDay() {
        DateTime expectedDateTime = new DateTime(2018,3,3,0,0,0,0, DateTimeZone.UTC);

        ToMillisAtStartOfDay func = new ToMillisAtStartOfDay();
        long resultMillis = func.applyAsLong(all3s.getMillis());
        DateTime actualDateTime = new DateTime(resultMillis,DateTimeZone.UTC);
        assertThat(actualDateTime).isEqualTo(expectedDateTime);
    }

    @Test
    public void testToMillisAtStartOfHour() {
        DateTime expectedDateTime = new DateTime(2018,3,3,3,0,0,0, DateTimeZone.UTC);

        ToMillisAtStartOfHour func = new ToMillisAtStartOfHour();
        long resultMillis = func.applyAsLong(all3s.getMillis());
        DateTime actualDateTime = new DateTime(resultMillis,DateTimeZone.UTC);
        assertThat(actualDateTime).isEqualTo(expectedDateTime);
    }

    @Test
    public void testToMillisAtStartOfMinute() {
        DateTime expectedDateTime = new DateTime(2018,3,3,3,3,0,0, DateTimeZone.UTC);

        ToMillisAtStartOfMinute func = new ToMillisAtStartOfMinute();
        long resultMillis = func.applyAsLong(all3s.getMillis());
        DateTime actualDateTime = new DateTime(resultMillis,DateTimeZone.UTC);
        assertThat(actualDateTime).isEqualTo(expectedDateTime);
    }

    @Test
    public void testToMillisAtStartOfMonth() {
        DateTime expectedDateTime = new DateTime(2018,3,1,0,0,0,0, DateTimeZone.UTC);

        ToMillisAtStartOfMonth func = new ToMillisAtStartOfMonth();
        long resultMillis = func.applyAsLong(all3s.getMillis());
        DateTime actualDateTime = new DateTime(resultMillis,DateTimeZone.UTC);
        assertThat(actualDateTime).isEqualTo(expectedDateTime);
    }

    @Test
    public void testToMillisAtStartOfNamedWeekDay() {
        DateTime expectedDateTime = new DateTime(2018,12,4,0,0,0,0,DateTimeZone.UTC);

        ToMillisAtStartOfNamedWeekDay func = new ToMillisAtStartOfNamedWeekDay("Tuesday");
        long resultMillis = func.applyAsLong(aGivenWednesday.getMillis());
        DateTime actualDateTime = new DateTime(resultMillis, DateTimeZone.UTC);
        assertThat(actualDateTime).isEqualTo(expectedDateTime);
    }

    @Test
    public void testToMillisAtStartOfNextDay() {
        DateTime expectedDateTime = new DateTime(2018,3,4,0,0,0,0, DateTimeZone.UTC);

        ToMillisAtStartOfNextDay func = new ToMillisAtStartOfNextDay();
        long resultMillis = func.applyAsLong(all3s.getMillis());
        DateTime actualDateTime = new DateTime(resultMillis,DateTimeZone.UTC);
        assertThat(actualDateTime).isEqualTo(expectedDateTime);
    }

    @Test
    public void testToMillisAtStartOfNextNamedWeekDay() {
        DateTime expectedDateTime = new DateTime(2018,12,11,0,0,0,0,DateTimeZone.UTC);

        ToMillisAtStartOfNextNamedWeekDay func = new ToMillisAtStartOfNextNamedWeekDay("Tuesday","UTC");
        long resultMillis = func.applyAsLong(aGivenWednesday.getMillis());
        DateTime actualDateTime = new DateTime(resultMillis, DateTimeZone.UTC);
        assertThat(actualDateTime).isEqualTo(expectedDateTime);
    }

    @Test
    public void testToMillisAtStartOfSecond() {
        DateTime expectedDateTime = new DateTime(2018,3,3,3,3,3,0, DateTimeZone.UTC);

        ToMillisAtStartOfSecond func = new ToMillisAtStartOfSecond();
        long resultMillis = func.applyAsLong(all3s.getMillis());
        DateTime actualDateTime = new DateTime(resultMillis,DateTimeZone.UTC);
        assertThat(actualDateTime).isEqualTo(expectedDateTime);
    }

    @Test
    public void testToMillisAtStartOfYear() {
        DateTime expectedDateTime = new DateTime(2018,1,1,0,0,0,0, DateTimeZone.UTC);

        ToMillisAtStartOfYear func = new ToMillisAtStartOfYear();
        long resultMillis = func.applyAsLong(all3s.getMillis());
        DateTime actualDateTime = new DateTime(resultMillis,DateTimeZone.UTC);
        assertThat(actualDateTime).isEqualTo(expectedDateTime);
    }

}
