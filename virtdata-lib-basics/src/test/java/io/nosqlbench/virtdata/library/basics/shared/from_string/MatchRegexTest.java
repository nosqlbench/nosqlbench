package io.nosqlbench.virtdata.library.basics.shared.from_string;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MatchRegexTest {

    @Test
    public void testMatchRegex() {
        MatchRegex mr = new MatchRegex(
                "1","one",
                ".*2","two",
                "3(.+5.*)9", "$0",
                "4(.6..)9", "$1",
                "(75-(.*)-4)","$2",
                ".*(25|6to4).*", "$1",
                "([0-9]+)-([0-9]+)-([0-9]+)","$1 $2 $3"
        );
        assertThat(mr.apply("1")).isEqualTo("one");
        assertThat(mr.apply("this-is-2")).isEqualTo("two");
        assertThat(mr.apply("3456789")).isEqualTo("3456789");
        assertThat(mr.apply("3-45678-9")).isEqualTo("3-45678-9");
        assertThat(mr.apply("456789")).isEqualTo("5678");
        assertThat(mr.apply("75-6-4")).isEqualTo("6");
        assertThat(mr.apply("345")).isEqualTo("345");
        assertThat(mr.apply("abc-25-xyz")).isEqualTo("25");
        assertThat(mr.apply("abc-6to4-xyz")).isEqualTo("6to4");
        assertThat(mr.apply("411-867-5309")).isEqualTo("411 867 5309");

    }
}