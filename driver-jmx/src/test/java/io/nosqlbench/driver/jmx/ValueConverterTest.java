package io.nosqlbench.driver.jmx;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ValueConverterTest {

    @Test
    public void testConvertStringDouble() {
        String s = "3";
        double d = (double) ValueConverter.convert("double", s);
    }

}