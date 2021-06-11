package io.nosqlbench.virtdata.library.basics.tests.long_string;

import io.nosqlbench.virtdata.library.basics.shared.from_long.to_string.AlphaNumericString;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class AlphaNumericStringTest {

    @Test
    public void testAlphaNumericStringZeroLength() {
        AlphaNumericString alphaNumeric = new AlphaNumericString(0);
        for (long cycle = 0; cycle < 5; cycle++) {
            String value = alphaNumeric.apply(cycle);
            assertThat(value).isEqualTo("");
        }
    }

    @Test
    public void testAlphaNumericStringBasic() {
        AlphaNumericString alphaNumeric = new AlphaNumericString(20);
        Set<String> seen = new HashSet<>();
        for (long cycle = 0; cycle < 5000; cycle++) {
            String value = alphaNumeric.apply(cycle);
            assertThat(value.length()).isEqualTo(20);
            assertThat(seen).doesNotContain(value);
            seen.add(value);
        }
        for (long cycle = 0; cycle < 5000; cycle++) {
            String value = alphaNumeric.apply(cycle);
            assertThat(value.length()).isEqualTo(20);
            assertThat(seen).contains(value);
        }
    }
}
