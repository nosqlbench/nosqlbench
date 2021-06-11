package io.nosqlbench.virtdata.library.basics.shared.from_long.to_time_types.joda;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class TimezonesTest {

    @Test
    public void testInvalidId() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> Timezones.forId("not gonna find it"));
    }
}