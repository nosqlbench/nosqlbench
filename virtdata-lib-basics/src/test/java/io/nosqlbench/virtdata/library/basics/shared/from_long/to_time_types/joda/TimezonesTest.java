package io.nosqlbench.virtdata.library.basics.shared.from_long.to_time_types.joda;

import org.joda.time.DateTimeZone;
import org.junit.Test;

public class TimezonesTest {

    @Test(expected= RuntimeException.class)
    public void testInvalidId() {
        DateTimeZone sdf = Timezones.forId("not gonna find it");
    }
}