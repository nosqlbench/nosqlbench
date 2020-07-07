package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NumberNameToStringTest {

    @Test
    public void testLargeNumbers() {
        NumberNameToString fmt = new NumberNameToString();
        assertThat(fmt.apply(Integer.MAX_VALUE))
            .isEqualTo(
                "two billion one hundred and forty seven million four hundred and eighty three thousand six hundred and forty seven"
            );
        assertThat(fmt.apply(999999999))
            .isEqualTo(
                "nine hundred and ninety nine million nine hundred and ninety nine thousand nine hundred and ninety nine"
            );
        assertThat(fmt.apply(1000000000L)).isEqualTo("one billion");
        assertThat(fmt.apply(-1000000000L)).isEqualTo("negative one billion");
        assertThat(fmt.apply(10000000000L)).isEqualTo("ten billion");
        // 9, 223,372,036, 854,775,807
        assertThat(fmt.apply(Long.MAX_VALUE)).isEqualTo("nine quintillion two hundred and twenty three quadrillion three hundred and seventy two trillion and thirty six billion eight hundred and fifty four million seven hundred and seventy five thousand eight hundred and seven");
    }

}
