package io.nosqlbench.virtdata.library.basics.shared.from_long.to_time_types;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class ToDateTest {

    @Test
    public void testToDateSimple() {
        ToDate f = new ToDate(5);
        assertThat(f.apply(0)).isEqualTo(new Date(0));
        assertThat(f.apply(1)).isEqualTo(new Date(5));
        assertThat(f.apply(100)).isEqualTo(new Date(500));
    }

    @Test
    public void testToDateWithRepeats() {
        ToDate f = new ToDate(86400000,2);
        assertThat(f.apply(0)).isEqualTo(new Date(0));
        assertThat(f.apply(1)).isEqualTo(new Date((86400000/2)));
        assertThat(f.apply(2)).isEqualTo(new Date((2*86400000)/2));
        assertThat(f.apply(3)).isEqualTo(new Date((3*86400000)/2));
    }

}
